package org.danf.dlpengine.service;


import org.danf.dlpengine.scanner.SensitiveDataScanner;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.danf.dlpengine.common.TestUtils.EMPTY_SCAN_RESULT;
import static org.danf.dlpengine.common.TestUtils.ONE_SCAN_RESULT;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensitiveDataScanServiceTest {

    private static final String MOCK_SCANNER_NAME = "Mock Scanner";

    @Mock
    SensitiveDataScanner mockScanner;

    private SensitiveDataScanService service;
    private AutoCloseable openMocks;

    @BeforeEach
    public void setup() {
        openMocks = MockitoAnnotations.openMocks(this);
        service = new SensitiveDataScanService(List.of(mockScanner));
    }

    @AfterEach
    public void clean() throws Exception {
        openMocks.close();
    }

    @Test
    public void testEmptyScanResult() {
        when(mockScanner.scan(anyString())).thenReturn(EMPTY_SCAN_RESULT);
        var actualScanResults = service.scan("");
        assertThat(actualScanResults.getResults()).hasSize(0);
        verify(mockScanner, times(1)).scan(anyString());
    }

    @Test
    public void testScanResult() {
        when(mockScanner.scan(anyString())).thenReturn(ONE_SCAN_RESULT);
        var actualScanResult = service.scan("");
        assertThat(actualScanResult.getResults()).hasSize(1);
        assertThat(actualScanResult.getResults().get(0)).isEqualTo(ONE_SCAN_RESULT);
        verify(mockScanner, times(1)).scan(anyString());
    }

    @Test
    public void testExistingFileScan() throws URISyntaxException {
        when(mockScanner.scan(anyString())).thenReturn(EMPTY_SCAN_RESULT);
        var existingFile = new File(getClass().getResource("/text_with_iban.txt").toURI());
        //Sanity
        assertThat(existingFile).exists();
        var actualScanResult = service.scanFile(existingFile.getAbsolutePath());
        assertThat(actualScanResult.getResults()).hasSize(0);
        verify(mockScanner, times(1)).scan(anyString());
    }

    @Test()
    public void testNonExistingFileScan() {
        assertThatThrownBy(() -> service.scanFile("/tmp/nope/nope/" + System.currentTimeMillis()))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Failed to read file at given path.");
        verify(mockScanner, never()).scan(anyString());
    }

    @Test
    public void testScannerError() {
        when(mockScanner.scannerName()).thenReturn(MOCK_SCANNER_NAME);
        when(mockScanner.scan(anyString())).thenThrow(new RuntimeException("OOPS!"));
        var actualScanResults = service.scan("");
        assertThat(actualScanResults.getResults()).hasSize(0);
        assertThat(actualScanResults.getErrors()).hasSize(1);
        assertThat(actualScanResults.getErrors().get(MOCK_SCANNER_NAME)).isEqualTo("OOPS!");
        verify(mockScanner, times(1)).scan(anyString());
    }
}
