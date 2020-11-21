package org.danf.dlpengine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
public class ScanResult {

    SensitiveDataType type;
    // count of the amount of sensitive data instances matched by the pattern
    int count;
    //Rank based on found context keywords, if any.
    int contextRank;
}
