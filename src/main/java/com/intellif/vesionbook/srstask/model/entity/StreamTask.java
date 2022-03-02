package com.intellif.vesionbook.srstask.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
