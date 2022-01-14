package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class StreamInfoReqVo {
    @ApiModelProperty(value = "应用ID", required = true, example = "garden")
    private String app;

    @ApiModelProperty(value = "源设备ID/SIP用户认证ID", required = true, example = "'34020000001320000001'")
    private String uniqueId;

    @ApiModelProperty(value = "GB28181通道id", example = "'34020000001320000001'")
    private String channelId;

}
