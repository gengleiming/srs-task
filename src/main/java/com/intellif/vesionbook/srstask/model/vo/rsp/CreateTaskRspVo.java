package com.intellif.vesionbook.srstask.model.vo.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTaskRspVo {
    private String rtmpOutput;
    private String httpFlvOutput;
    private String hlsOutput;
    private String webrtcOutput;
}
