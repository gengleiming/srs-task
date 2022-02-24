package com.intellif.vesionbook.srstask.model.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class VideoRecorderTask implements Serializable {

    /**
     *
     */
    private Long id;

    /**
     * 应用
     */
    private String app;

    /**
     * 应用
     */
    private String uniqueId;
    /**
     * 通道id
     */
    private String channelId;

    /**
     * rtsp流地址
     */
    private String originStream;

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
    private int status;

    /**
     * 流类型 1:rtsp 2:gb28181
     */
    private int streamType;

    /**
     * 录像存储路径
     */
    private String path;

    /**
     *
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

}