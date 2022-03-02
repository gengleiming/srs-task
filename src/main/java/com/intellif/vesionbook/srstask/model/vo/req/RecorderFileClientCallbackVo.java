package com.intellif.vesionbook.srstask.model.vo.req;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RecorderFileClientCallbackVo {
    private String taskId;
    private String url;
    private String filePath;
    private String objectName;
}
