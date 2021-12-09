package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CloseTaskReqVo {
    @ApiModelProperty(value = "应用ID", required = true, example = "garden")
    private String app;

    @ApiModelProperty(value = "源设备ID", required = true, example = "device1")
    private String uniqueId;

    @ApiModelProperty(value = "源", example = "rtsp://admin:intellif123@192.168.18.5/live/livestream")
    private String originStream;
}
