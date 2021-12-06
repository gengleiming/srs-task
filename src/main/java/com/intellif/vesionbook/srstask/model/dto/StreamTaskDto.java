package com.intellif.vesionbook.srstask.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class StreamTaskDto {
    private Long id;
    private String origin;
    private String app;
    private String uniqueId;
    private String service;
    private Integer outputTypes;
    private String outputStream;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean lock;
}
