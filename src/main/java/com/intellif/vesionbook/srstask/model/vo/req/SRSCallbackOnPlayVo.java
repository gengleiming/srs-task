package com.intellif.vesionbook.srstask.model.vo.req;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SRSCallbackOnPlayVo {
    private String action;
    private String client_id;
    private String ip;
    private String param;
    private String stream;
}
