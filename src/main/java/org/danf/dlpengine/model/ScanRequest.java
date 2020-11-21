package org.danf.dlpengine.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScanRequest {

    String text = "";
    String filePath = "";

}
