package org.danf.dlpengine.scanner;

import org.danf.dlpengine.model.SensitiveDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.danf.dlpengine.scanner.SocialSecurityNumberScanner.SOCIAL_SECURITY_NUMBER_KEYWORDS;
import static org.danf.dlpengine.scanner.SocialSecurityNumberScanner.SOCIAL_SECURITY_NUMBER_PATTERN;

public class SocialSecurityNumberScannerTest {

    SocialSecurityNumberScanner scanner = new SocialSecurityNumberScanner();
    Pattern contextKeywordsPattern = Pattern.compile(String.join("|", SOCIAL_SECURITY_NUMBER_KEYWORDS), Pattern.CASE_INSENSITIVE);

    @Test
    public void testPatternWithKeywords() {
        // With context rank
        validateSingleResponse("My social security number is 123-45-6789", 1, 1);
        validateSingleResponse("123-45-6789 \n Soc Sec . ", 1, 1);
        validateSingleResponse("123 45 6789 bla bla unrelated. lala \n SSN", 1, 1);
        validateSingleResponse("SSNS \n 123456789", 1, 1);
        validateSingleResponse("SSN# \n 123456789 \n 123 45 6789 and then SS#", 2, 2);
        validateSingleResponse("123-45-6789 \n . . . 123 45 6789 \n social security#.   \n 123456789", 3, 1);
        validateSingleResponse("123-45-6789 \n Social Security .\n Social Security# ...  SSN\n SS# ", 1, 4);
    }

    @Test
    public void testPatternWithoutKeywords() {
        validateSingleResponse("123-45-6789 \n Soc Seh . ", 1, 0);
        validateSingleResponse("security number is 123-45-6789", 1, 0);
        validateSingleResponse("123 45 6789 bla bla unrelated. lala \n NOPE", 1, 0);
        validateSingleResponse("not matching keywords \n 123456789", 1, 0);
        validateSingleResponse("also no \n 123456789 \n 123 45 6789 and then really no", 2, 0);
        validateSingleResponse("S S # \n 123-45-6789 \n . . . 123 45 6789 \n social #.   \n 123456789", 3, 0);
    }

    private void validateSingleResponse(String input, int matches, int contextRank) {
        var result = scanner.scan(input);
        assertThat(result.getType()).isEqualTo(SensitiveDataType.SSN);
        assertThat(result.getCount()).isEqualTo(matches);
        assertThat(result.getContextRank()).isEqualTo(contextRank);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123-45-6789",
            "123456789",
            "123 45 6789"
    })
    public void testIbanRegexPositives(String input) {
        assertThat(SOCIAL_SECURITY_NUMBER_PATTERN.matcher(input).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123 456789",
            "1 2 3 4 5 6 7 8 9",
            "1 23456789",
            "12 3456789",
            "123 456789",
            "1234 56789",
            "12 345 6789",
            "123 456 789"
    })
    public void testIbanRegexNegatives(String input) {
        assertThat(SOCIAL_SECURITY_NUMBER_PATTERN.matcher(input).matches()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "SSNS",
            "SS#",
            "SSN#",
            "SSN",
            "SSID",
            "Soc Sec",
            "Soc SeC",
            "Social Security#",
            "Social Security",
            "SoCiAl SeCuRiTy",
            // Regex is case insensitive so these should match as well
            "ssn",
            "ssns",
            "ssn#",
            "ssid",
            "soc sec",
            "social security",
    })
    public void testKeywordsRegexPositives(String input) {
        assertThat(contextKeywordsPattern.matcher(input).matches()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "s s #",
            "social securit",
            "social securit#",
            "soc sef",
            "Soc Sef",
            "S SN",
            "SSI D",
            "practically anything else"
    })
    public void testKeywordsRegexNegatives(String input) {
        assertThat(contextKeywordsPattern.matcher(input).matches()).isFalse();
    }
}
