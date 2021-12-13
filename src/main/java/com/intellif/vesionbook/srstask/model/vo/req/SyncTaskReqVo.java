package com.intellif.vesionbook.srstask.model.vo.req;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class SyncTaskReqVo {

    @ApiModelProperty(value = "应用id，防止任务列表为空时，找不到app", required = true)
    private String app;
    @ApiModelProperty(value = "同步任务列表", required = true)
    private List<TaskReqVo> taskList;

}
