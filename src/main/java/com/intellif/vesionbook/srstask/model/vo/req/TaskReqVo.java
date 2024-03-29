package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskReqVo {
    @ApiModelProperty(value = "应用ID", required = true, example = "garden")
    private String app;

    @ApiModelProperty(value = "源设备ID/SIP用户认证ID", required = true, example = "'34020000001320000001'")
    private String uniqueId;

    @ApiModelProperty(value = "推流地址 gb28181不需要", example = "rtsp://admin:intellif123@192.168.18.5/live/livestream")
    private String originStream;

    @ApiModelProperty(value = "GB28181通道id", example = "'34020000001320000001'")
    private String channelId;

    @ApiModelProperty(value = "分发类型 1:rtmp 2:http-hlv 3:hls 4:WebRTC", required = true, example = "4")
    private Integer outputType;

}
