package com.intellif.vesionbook.srstask.model.vo.rsp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetGBDataFromSrsRspVo {
    private Long code;
    private ReturnData data;

    @Data
    public static class ReturnData {
        List<SessionData> sessions;
        List<ChannelData> channels;
    }

    @Data
    public static class SessionData {
        String id;
        Long device_sumnum;
        List<DeviceData> devices;
    }

    @Data
    public static class DeviceData {
        String device_id;
        String device_name;
        String device_status;
        String invite_status;
        Long invite_time;
    }

    @Data
    public static class ChannelData {
        String id;
        String ip;
        Long rtmp_port;
        String app;
        String stream;
        String rtmp_url;
        String flv_url;
        String hls_url;
        String webrtc_url;
        Long ssrc;
        Long rtp_port;
        String port_mode;
        Long rtp_peer_port;
        String rtp_peer_ip;
        Long recv_time;
        String recv_time_str;
    }

}
