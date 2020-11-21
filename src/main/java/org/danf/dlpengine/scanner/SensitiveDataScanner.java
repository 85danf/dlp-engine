package org.danf.dlpengine.scanner;

import org.danf.dlpengine.model.ScanResult;
import org.danf.dlpengine.model.SensitiveDataType;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract DLP scanner, implementations should:
 * - Declare a {@link SensitiveDataType} describing the data it scans for.
 * - Declare a {@link Pattern} to match the sensitive data it scans for.
 * - Declare a {@link Pattern} to match any context keywords that might accompany the sensitive data. it is possible not to define any.
 */
public abstract class SensitiveDataScanner {

    @Nullable
    private final Pattern contextKeywords;
    private final Pattern sensitiveDataRegex;
    private final SensitiveDataType dataType;

    public SensitiveDataScanner(List<String> contextKeywords, Pattern sensitiveDataRegex, SensitiveDataType dataType) {
        if (CollectionUtils.isEmpty(contextKeywords)) {
                this.contextKeywords = null;
        } else {
            this.contextKeywords = Pattern.compile(String.join("|", contextKeywords), Pattern.CASE_INSENSITIVE);
        }
        this.sensitiveDataRegex = sensitiveDataRegex;
        this.dataType = dataType;
    }

    /**
     * Scans for the existence of sensitive data based on the {@link #sensitiveDataRegex} this scanner was instantiated with.
     * An optional set of keywords may be given via {@link #contextKeywords}, if given any one of the keywords must match in the given input for the match
     * to be considered positive.
     *
     * @param input A string to scan for sensitive data.
     * @return {@link ScanResult} containing the type of sensitive data and the number of occurrences found in the input.
     */
    public ScanResult scan(String input) {
        var sensitiveDataMatchesCount = countMatches(input);
        var contextRank = calculateContextRank(input);

        return ScanResult.builder()
                .type(dataType)
                .count(sensitiveDataMatchesCount)
                .contextRank(contextRank)
                .build();
    }

    private int countMatches(String input) {
        int count = 0;
        var matcher = sensitiveDataRegex.matcher(input);
        while (matcher.find()) {
            if (isValidMatch(matcher)) {
                count++;
            }
        }
        return count;
    }

    /**
     * @param input text to search for keywords in.
     * @return The context rank of the input.
     * <p>
     * IMPLEMENTATION NOTES:
     * This is a very naive approach of simply checking for the existence of any of the keywords, as opposed to trying split the input the sentences and
     * attempting to match keywords to the current match's sentence up to the previous '.' or '\n' (or any other such heuristic).
     * For the purpose of this exam I assumed this was enough.
     * <p>
     * Secondly the 'rank' in this case is simply the count of matching keywords in the input, it can be made more robust by including some
     * logic about the number of matches, where they were found etc.
     */
    protected int calculateContextRank(String input) {
        int rank = 0;
        if (contextKeywords != null) {
            var contextKeyWordsMatcher = contextKeywords.matcher(input);
            while (contextKeyWordsMatcher.find()) {
                rank++;
            }
        }
        return rank;
    }

    /**
     * @param matcher A matcher that has found a single match of the given pattern, capture groups may be used to validate the match.
     * @return true if the match is valid
     */
    protected abstract boolean isValidMatch(Matcher matcher);

    /**
     * Used to map errors in responses
     */
    public abstract String scannerName();
}
