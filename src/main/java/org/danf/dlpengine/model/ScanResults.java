package org.danf.dlpengine.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

@Value
@Builder
@JsonInclude(Include.NON_NULL)
public class ScanResults {

    List<ScanResult> results;
    // maps scanner name to error
    Map<String, String> errors;
}
