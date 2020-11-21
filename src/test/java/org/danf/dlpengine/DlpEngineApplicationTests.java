package org.danf.dlpengine;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.internal.bytebuddy.utility.RandomString;
import org.danf.dlpengine.model.ScanRequest;
import org.danf.dlpengine.model.ScanResults;
import org.danf.dlpengine.rest.ScanController;
import org.danf.dlpengine.service.SensitiveDataScanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.danf.dlpengine.common.TestUtils.EMPTY_SCAN_RESULT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(ScanController.class)
class DlpEngineApplicationTests {

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String SCAN_TEXT_ENDPOINT = "/api/v1/scan/text";
    private static final String SCAN_FILE_ENDPOINT = "/api/v1/scan/file";
    private final ScanResults EMPTY_SCAN_RESULTS = ScanResults.builder().results(List.of(EMPTY_SCAN_RESULT)).build();
    private final ScanResults ERROR_SCAN_RESULTS = ScanResults.builder().errors(Map.of("Some Scanner", "Some Error")).build();
    private String EMPTY_SCAN_RESULTS_JSON;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SensitiveDataScanService service;

    @BeforeEach
    public void setup() throws JsonProcessingException {
        EMPTY_SCAN_RESULTS_JSON = mapper.writeValueAsString(EMPTY_SCAN_RESULTS);
    }

    @Test
    void testScanTextEndpoint() throws Exception {
        var request = makeRequest(SCAN_TEXT_ENDPOINT, ScanRequest.builder().text("some text").build());
        when(service.scan(Mockito.anyString())).thenReturn(EMPTY_SCAN_RESULTS);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(EMPTY_SCAN_RESULTS_JSON));
    }

    @Test
    void testScanFileEndpoint() throws Exception {
        var filePath = getClass().getResource("/text_with_iban.txt").getPath();
        var request = makeRequest(SCAN_FILE_ENDPOINT, ScanRequest.builder().filePath(filePath).build());
        when(service.scanFile(anyString())).thenReturn(EMPTY_SCAN_RESULTS);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(EMPTY_SCAN_RESULTS_JSON));
    }

    @Test
    public void testRestEndpointEmptyRequestValidation() throws Exception {
        var badEmptyRequest = makeRequest(SCAN_TEXT_ENDPOINT, ScanRequest.builder().build());
        mockMvc.perform(badEmptyRequest)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage()).contains("The 'text' field must be specified."));

        badEmptyRequest = makeRequest(SCAN_FILE_ENDPOINT, ScanRequest.builder().build());
        mockMvc.perform(badEmptyRequest)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage()).contains("The 'filePath' field must be specified."));
    }

    @Test
    public void testRestEndpointRequestSizeValidation() throws Exception {
        var badLengthRequest = makeRequest(SCAN_TEXT_ENDPOINT, ScanRequest.builder().text(RandomString.make(4050)).build());
        mockMvc.perform(badLengthRequest)
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage()).contains("Input exceeds max allowed body size"));
    }

    @Test
    public void testRestEndpointFileValidation() throws Exception {
        var badLengthRequest = makeRequest(SCAN_FILE_ENDPOINT, ScanRequest.builder().filePath("nope-nope-nope").build());
        mockMvc.perform(badLengthRequest)
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(result -> assertThat(result.getResponse().getErrorMessage()).contains("File not found at path"));
    }

    @Test
    public void testErrorsResponse() throws Exception {
        var errorResponse = mapper.writeValueAsString(ERROR_SCAN_RESULTS);
        var request = makeRequest(SCAN_TEXT_ENDPOINT, ScanRequest.builder().text("some text").build());
        when(service.scan(Mockito.anyString())).thenReturn(ERROR_SCAN_RESULTS);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(errorResponse));
    }


    private MockHttpServletRequestBuilder makeRequest(String endpoint, ScanRequest request) throws JsonProcessingException {
        return post(endpoint)
                .contentType(APPLICATION_JSON_VALUE)
                .accept(APPLICATION_JSON_VALUE)
                .content(mapper.writeValueAsString(request));
    }
}
