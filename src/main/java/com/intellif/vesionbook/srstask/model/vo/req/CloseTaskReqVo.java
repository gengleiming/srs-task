package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(name = "创建流任务请求体")
public class CloseTaskReqVo {
    @Schema(name = "应用app", required = true, description = "一级app")
    private String app;
    @Schema(name = "stream唯一id", required = true, description = "一级stream")
    private String uniqueId;
    @Schema(name = "拉流地址", description = "拉流地址")
    private String originStream;
}
