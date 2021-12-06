package com.intellif.vesionbook.srstask.model.vo.rsp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Schema(name = "创建视频流返回体")
@Builder
public class CreateTaskRspVo {
    private String rtmpOutput;
    private String httpFlvOutput;
    private String hlsOutput;
    private String webrtcOutput;
    private Integer status;
}
