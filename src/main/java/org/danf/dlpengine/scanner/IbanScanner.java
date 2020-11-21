package org.danf.dlpengine.scanner;

import nl.garvelink.iban.Modulo97;
import org.danf.dlpengine.model.SensitiveDataType;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Scans for the presence of IBAN numbers in any given input.
 * IBAN numbers conform to the following format:
 * - Two-letter country code
 * - Two check digits (followed by an optional space)
 * - 1-7 groups of four letters or digits (can be separated by spaces)
 * - 1-3 letters or digits
 *
 * As a side not I have also found apache's {@see commons-validator} which has an IBAN validator which almost answers the
 * requirements of this exam, as well as {@see https://github.com/arturmkrtchyan/iban4j} which is a bit better, but has a lot of open issues.
 * Although its totally possible to just write my own implementation I settled for {@see https://github.com/barend/java-iban} which is lean simple and seems
 * well-maintained. It also seems to be quite updated with the supported countries list
 *
 * It is my understanding this exam's purpose is not really the validation logic itself thus I allowed myself to use it although its apparent it does not follow
 * the exam's definition of IBAN to the letter (but it does follow the wikipedia definition)
 */
@Component
public class IbanScanner extends SensitiveDataScanner {

    /**
     * This pattern matches the above requirement.
     * Capture group 1 holds the entire captured IBAN string, to be used in the validation.
     */
    protected final static Pattern IBAN_PATTERN = Pattern.compile("([a-zA-z]{2}\\d{2} ?(:?\\w{4} ?){1,7} ?(:?\\w{1,3})?)", Pattern.CASE_INSENSITIVE);

    public IbanScanner() {
        super(Collections.emptyList(), IBAN_PATTERN, SensitiveDataType.IBAN);
    }

    @Override
    public boolean isValidMatch(Matcher matcher) {
        // This verifies the IBAN checksum, as per https://en.wikipedia.org/wiki/International_Bank_Account_Number#Modulo_operation_on_IBAN
        return Modulo97.verifyCheckDigits(matcher.group(1));
    }

    @Override
    public String scannerName() {
        return "IBAN scanner";
    }
}
