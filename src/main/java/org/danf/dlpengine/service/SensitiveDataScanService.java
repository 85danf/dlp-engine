package org.danf.dlpengine.service;

import lombok.extern.slf4j.Slf4j;
import org.danf.dlpengine.model.ScanResult;
import org.danf.dlpengine.model.ScanResults;
import org.danf.dlpengine.scanner.SensitiveDataScanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Provides methods to invoke scanning an in input and returns an aggregation of results from all scanner implementations.
 */
@Slf4j
@Service
public class SensitiveDataScanService {

    private final List<SensitiveDataScanner> scanners;

    @Autowired
    public SensitiveDataScanService(List<SensitiveDataScanner> scanners) {
        this.scanners = scanners;
    }

    /**
     * Assumption: REST controller already validated file at path exists.
     * That being said, it might get deleted between validation and open for read so best to double-check.
     */
    public ScanResults scanFile(String filePath) {
        String input;
        try {
            // Should add sanity test that file is not bigger then a few K
            input = Files.readString(Path.of(filePath));
        } catch (IOException ioe) {
            log.error("Failed to read file at path '{}' : '{}'", filePath, ioe);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read file at given path.");
        }
        return scan(input);
    }

    public ScanResults scan(String input) {
        final var errors = new HashMap<String, String>();
        var results = scanners.stream()
                .map(scanner -> scanInput(scanner, input, errors))
                .filter(Objects::nonNull)
                .filter(scanResult -> scanResult.getCount() > 0) // Don't return empty results
                .collect(Collectors.toList());

        return ScanResults.builder()
                .results(results)
                .errors(errors)
                .build();
    }

    private ScanResult scanInput(SensitiveDataScanner scanner, String input, HashMap<String, String> errors) {
        ScanResult scanResult = null;
        try {
            scanResult = scanner.scan(input);
        } catch (Exception e) {
            log.error("Caught error from scanner '{}' while attempting to scan input.", scanner.scannerName(), e);
            errors.put(scanner.scannerName(), e.getMessage());
        }
        return scanResult;
    }
}
