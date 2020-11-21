package org.danf.dlpengine.scanner;

import org.danf.dlpengine.common.TestUtils;
import org.danf.dlpengine.model.SensitiveDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.danf.dlpengine.scanner.IbanScanner.IBAN_PATTERN;

public class IbanScannerTest {

    @Test
    public void testUnstructuredIbanExamples() throws IOException {
        var scanner = new IbanScanner();
        String input = TestUtils.getResourceAsString(getClass(), "/text_with_iban.txt");
        var result = scanner.scan(input);
        assertThat(result.getType()).isEqualTo(SensitiveDataType.IBAN);
        assertThat(result.getCount()).isEqualTo(4);
        // There are no context keywords defined for IBAN
        assertThat(result.getContextRank()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "UE44 5001 0517 5407 3249 0517 0517 0517 313",
            "UE44 5001 0517 5407 3249 0517 0517 0517 31",
            "UE44 5001 0517 5407 3249 0517 0517 0517 3",
            "UE44 5001 0517 5407 3249 0517 0517 0517313",
            "UE44 5001 0517 5407 3249 0517 05170517313",
            "UE44 5001 0517 5407 3249 051705170517313",
            "UE44 5001 0517 5407 3249051705170517313",
            "UE44 5001 0517 54073249051705170517313",
            "UE44 5001 051754073249051705170517313",
            "UE44 5001051754073249051705170517313",
            "UE445001051754073249051705170517313",
            "UE445001 0517 5407 3249 051705170517313",
            "UE4450010517 5407 3249 051705170517313",
            "UE44500105175407 3249 051705170517313",
            "UE44 5001 0517 5407 3249 0517 0517 313",
            "UE44 5001 0517 5407 3249 0517 313",
            "UE44 5001 0517 5407 3249  313",
            "UE44 5001 0517 5407 313",
            "UE44 5001 0517 313",
            "UE44 5001  313",
            "DE44 5001 0517 5407 3249 31",
            "GB29 NWBK 6016 1331 9268 19",
            "SA0380000000608010167519"
    })
    public void testIbanRegexPositives(String input) {
        assertThat(IBAN_PATTERN.matcher(input).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1E44 5001 0517 5407 3249 0517 0517 0517 313",
            "UE44 313",
            "I am rubber, you are glue.",
            "GB29 MURR AY 16 1331 9268 19",
            "GB29 MURRAY 16 1331 9268 19",
            "1B29 NWBK 6016 1331 9268 19",
            "!B29 NWBK 6016 1331 9268 19",
    })
    public void testIbanRegexNegatives(String input) {
        assertThat(IBAN_PATTERN.matcher(input).matches()).isFalse();
    }
}
