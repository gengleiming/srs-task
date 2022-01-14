package com.intellif.vesionbook.srstask.model.vo.rsp;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 */
@Data
@Builder
public class GetStreamFromSrsRspVo {
    private Long code;
    private String server;
    private List<StreamData> streams;

    @Data
    public static class StreamData {
        String id;
        String name;
        String vhost;
        String app;
        Long live_ms;
        Long clients;
        Long frames;
        Long send_bytes;
        Long recv_bytes;
        StreamPublishData publish;
        StreamVideoData video;
    }

    @Data
    public static class StreamPublishData {
        Boolean active;
        String cid;
    }

    @Data
    public static class StreamVideoData {
        String codec;
        String profile;
        String level;
        Integer width;
        Integer height;
    }

}
