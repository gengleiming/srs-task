package com.intellif.vesionbook.srstask.model.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UpdateVideoRecorderTaskDto implements Serializable {
    private Long id;
    private String app;
    private Long uniqueId;
    private String ossObjectName;
    private String path;

    private List<Long> idList;

    /**
     * 录像任务状态 0.未开始 1.运行中 2.已结束 3. 任务异常
     */
    private int status;

}