package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class GetOrCreateTaskReqVo {
    @ApiModelProperty(value = "应用ID", required = true, example = "garden")
    private String app;

    @ApiModelProperty(value = "源设备ID", required = true, example = "device1")
    private String uniqueId;

    @ApiModelProperty(value = "源", required = true, example = "rtsp://admin:intellif123@192.168.18.5/live/livestream")
    private String originStream;

    @ApiModelProperty(value = "分发类型 1:rtmp 2:http-hlv 3:hls 4:WebRTC", required = true, example = "4")
    private Integer outputType;

    @ApiModelProperty(value = "永久视频流，一般用于长期不会关闭的视频流，比如ai引擎", example = "0")
    private Integer forever;
}
