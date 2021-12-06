package com.intellif.vesionbook.srstask.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StreamTaskDto {
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
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean lock;
}
