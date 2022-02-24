package com.intellif.vesionbook.srstask.model.vo.req;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SRSCallbackOnDvrVo {
    private String action;
    private String client_id;
    private String ip;
    private String vhost;
    private String app;
    private String stream;
    private String param;
    private String cwd;
    private String file;
}
