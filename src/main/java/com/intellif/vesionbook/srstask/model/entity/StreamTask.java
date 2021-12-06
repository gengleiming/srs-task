package com.intellif.vesionbook.srstask.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StreamTask {
    private Long id;
    private String origin;
    private String app;
    private String uniqueId;
    private String service;
    private String rtmpOutput;
    private String httpFlvOutput;
    private String hlsOutput;
    private String webrtcOutput;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
