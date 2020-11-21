package org.danf.dlpengine.rest;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.danf.dlpengine.model.ScanRequest;
import org.danf.dlpengine.model.ScanResults;
import org.danf.dlpengine.service.SensitiveDataScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Optional;

/**
 * A controller for exposing this service's functionality via REST API.
 */
@Slf4j
@RestController
@RequestMapping(path = "/api/v1")
public class ScanController {

    @Value("${engine.limit.max-input-length}")
    private int MAX_INPUT_LENGTH;

    @Resource
    private HttpServletRequest httpRequest;

    private final SensitiveDataScanService scanService;

    @Autowired
    public ScanController(SensitiveDataScanService scanService) {
        this.scanService = scanService;
    }

    @PostMapping(
            value = "/scan",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ScanResults scanString(@RequestBody ScanRequest scanRequest) {
        validateScanRequest(scanRequest);

        // Can also add trace information to the request to enable cross-service request tracing, but I don't want to log the input itself for fear of leaking
        // sensitive information into production logs.
        log.info("Scanning incoming request from '{}'", httpRequest.getRemoteAddr());
        ScanResults results;
        if (Strings.isNotBlank(scanRequest.getText())) {
            results = scanService.scan(scanRequest.getText());
        } else {
            results = scanService.scanFile(scanRequest.getFilePath());
        }
        // Spring automagically discovers Jackson2 on the classpath and lets it handle response serialization
        return results;
    }

    /**
     * Validates input is ok (can be also handled with @Valid annotations and a model for the input)
     */
    private void validateScanRequest(final ScanRequest scanRequest) {
        final String filePath = Optional.ofNullable(scanRequest.getFilePath()).orElse("");
        final String text = Optional.ofNullable(scanRequest.getText()).orElse("");
        if (Strings.isBlank(text) && Strings.isBlank(filePath)) {
            log.warn("Incoming request from '{}' with empty input and no file path", httpRequest.getRemoteAddr());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either the 'text' or the 'filePath' field must be specified.");
        } else if (Strings.isNotBlank(text) && Strings.isNotBlank(filePath)) {
            log.warn("Incoming request from '{}' with both input and file path", httpRequest.getRemoteAddr());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Either the 'text' or the 'filePath' field can be specified, but not both.");
        } else if (text.length() > MAX_INPUT_LENGTH) {
            log.warn("Denying incoming request from '{}' with input length {}", httpRequest.getRemoteAddr(), text.length());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Input exceeds max allowed body size (" + MAX_INPUT_LENGTH + " characters).");
        }

        // Absolute paths only please.
        if (Strings.isNotBlank(filePath) && !(new File(filePath).exists())) {
            log.warn("Incoming request from '{}' pointing to non-existing file at path '{}'", httpRequest.getRemoteAddr(), filePath);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found at path " + filePath);
        }
    }
}
