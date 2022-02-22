package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class VideoRecorderTaskReqVo {
    @ApiModelProperty(value = "应用ID", required = true, example = "garden")
    private String app;

    @ApiModelProperty(value = "源设备ID/SIP用户认证ID", required = true, example = "'34020000001320000001'")
    private String uniqueId;

    @ApiModelProperty(value = "推流地址 gb28181不需要", example = "rtsp://admin:intellif123@192.168.18.5/live/livestream")
    private String originStream;

    @ApiModelProperty(value = "GB28181通道id", example = "'34020000001320000001'")
    private String channelId;

    @ApiModelProperty(value = "录制开始时间戳和结束时间戳", required = true)
    private List<TimeRange> timeRangeList;
}
