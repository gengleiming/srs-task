package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class TaskListReqVo {

    @ApiModelProperty(value = "应用id", required = true, example = "garden")
    private String app;

}
