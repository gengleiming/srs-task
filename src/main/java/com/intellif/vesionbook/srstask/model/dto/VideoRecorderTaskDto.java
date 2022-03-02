package com.intellif.vesionbook.srstask.model.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VideoRecorderTaskDto implements Serializable {
    private Integer page = 1;
    private Integer pageSize = 10;

    private Long id;

    private List<Long> idList;

    /**
     * 应用
     */
    private String app;

    /**
     * 应用
     */
    private String uniqueId;

    /**
     * 录制开始时间戳（秒）
     */
    private Long startTime;

    /**
     * 录制结束时间戳（秒）
     */
    private Long endTime;

    /**
     * 录像任务状态 0.未开始 1.运行中 2.已结束 3. 任务异常
     */
    private Integer status;

}