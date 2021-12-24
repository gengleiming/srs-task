package com.intellif.vesionbook.srstask.model.vo.rsp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
@Builder
public class GetClientsFromSrsRspVo {
    private Long code;
    private String server;
    private List<ClientData> clients;

    @Data
    public static class ClientData {
        String id;
        String vhost;
        String stream;
        // 客户端ip
        String ip;
        String pageUrl;
        String swfUrl;
        String tcUrl;
        String url;
        String type;
        Boolean publish;
        // 播放时间
        Float active;
    }
}
