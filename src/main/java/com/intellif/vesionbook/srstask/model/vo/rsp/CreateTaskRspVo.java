package com.intellif.vesionbook.srstask.model.vo.rsp;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTaskRspVo {
    private String rtmpOutput;
    private String httpFlvOutput;
    private String hlsOutput;
    private String webrtcOutput;
    private String clientId;
}
