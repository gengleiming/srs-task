package com.intellif.vesionbook.srstask.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class StreamTask {
    private Long id;
    private String originStream;
    private String app;
    private String uniqueId;
    private String service;
    private String rtmpOutput;
    private String httpFlvOutput;
    private String hlsOutput;
    private String webrtcOutput;
    private Integer status;
    private Integer forever;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
