package org.danf.dlpengine.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.danf.dlpengine.model.ScanResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils {

    public static final ScanResult EMPTY_SCAN_RESULT = ScanResult.builder().build();
    public static final ScanResult ONE_SCAN_RESULT = ScanResult.builder().count(1).build();

    public static String getResourceAsString(Class<?> clazz, String resourcePath) throws IOException {
        return IOUtils.toString(clazz.getResource(resourcePath), StandardCharsets.UTF_8);
    }
}
