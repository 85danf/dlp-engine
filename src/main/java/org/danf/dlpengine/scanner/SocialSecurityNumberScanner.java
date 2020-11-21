package org.danf.dlpengine.scanner;

import org.danf.dlpengine.model.SensitiveDataType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Scans for the presence of numbers matching a Social Security Number pattern (denoted by {@link #SOCIAL_SECURITY_NUMBER_PATTERN}
 * Additionally, any one of the related context keywords (denoted by {@link #SOCIAL_SECURITY_NUMBER_KEYWORDS} may influence the result's rank.
 *
 * The patterns to look for SSNs can be any of:
 * - ddddddddd
 * - ddd-dd-dddd
 * - ddd dd dddd
 */
@Component
public class SocialSecurityNumberScanner extends SensitiveDataScanner {

    protected final static Pattern SOCIAL_SECURITY_NUMBER_PATTERN = Pattern.compile("\\d{3}[ -]\\d{2}[ -]\\d{4}|\\d{9}");
    protected final static List<String> SOCIAL_SECURITY_NUMBER_KEYWORDS = List.of("SSNS", "SS#", "SSN#", "SSN", "SSID", "Soc Sec", "Social Security", "Social Security#");

    public SocialSecurityNumberScanner() {
        super(SOCIAL_SECURITY_NUMBER_KEYWORDS, SOCIAL_SECURITY_NUMBER_PATTERN, SensitiveDataType.SSN);
    }

    @Override
    protected boolean isValidMatch(Matcher matcher) {
        // Social Security Number has no validation
        return true;
    }

    @Override
    public String scannerName() {
        return "Social Security Number Scanner";
    }
}
