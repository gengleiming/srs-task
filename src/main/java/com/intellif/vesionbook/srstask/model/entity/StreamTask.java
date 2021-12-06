package com.intellif.vesionbook.srstask.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StreamTask {
    private Long id;
    private String originStream;
    private String app;
    private String uniqueId;
    private String service;
    private Integer outputTypes;
    private String outputStream;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
