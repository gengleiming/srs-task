package com.intellif.vesionbook.srstask.helper;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


import static org.bytedeco.ffmpeg.global.avcodec.av_packet_unref;

/**
 * @description: 视频转码
 * @author: gengleiming
 * @createDate: 2021/12/10
 * @version: 1.0
 */
@Component
@Slf4j
public class JavaCVHelper {
    @Resource
    private ServerConfig serverConfig;
    @Resource
    private StreamTaskCache streamTaskCache;

    @Async("asyncServiceExecutor")
    public void asyncPullRtspPushRtmp(String originStream, String app, String uniqueId) {
        FFmpegFrameGrabber grabber;
        FFmpegFrameRecorder recorder;

        try{
            grabber = new FFmpegFrameGrabber(originStream);
            if (originStream.contains("rtsp")) {
                // tcp用于解决丢包问题
                grabber.setOption("rtsp_transport", "tcp");
            }
            // 设置采集器构造超时时间
            grabber.setOption("stimeout", "2000000");
            grabber.start();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("拉流失败，请检查视频流，originStream: {}, app: {}, uniqueId: {}", originStream, app, uniqueId);
            return;
        }

        String pushStream = "rtmp://" + serverConfig.getSrsHost() + "/" + app + "/" + uniqueId;
        try {
            // 录制/推流器
            recorder = new FFmpegFrameRecorder(pushStream, grabber.getImageWidth(), grabber.getImageHeight());
            // 两个关键帧之间的帧数
            recorder.setGopSize(2);
            // 帧率
            recorder.setFrameRate(grabber.getFrameRate());
            // 比特率
            recorder.setVideoBitrate(grabber.getVideoBitrate());
            // 封装格式flv
            recorder.setFormat("flv");
            recorder.setAudioCodecName("aac");
            AVFormatContext fc = grabber.getFormatContext();
            recorder.start(fc);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("推流失败，请检查srs服务器, originStream: {}, app: {}, uniqueId: {}, pushStream: {}", originStream, app, uniqueId, pushStream);
            return;
        }

        int no_frame_index = 0;
        int err_index = 0;

        Thread currentThread = Thread.currentThread();
        streamTaskCache.storeThread(app, uniqueId, currentThread);

        while (!currentThread.isInterrupted()) {
            try {
                //没有解码的音视频帧
                AVPacket packet = grabber.grabPacket();
                if (packet == null || packet.size() <= 0 || packet.data() == null) {
                    //空包记录次数跳过
                    no_frame_index++;
                    if (no_frame_index > 10 && no_frame_index < 100 && no_frame_index % 10 == 0) {
                        log.error("no frame index: {}", no_frame_index);
                    }
                    if (no_frame_index > 100 && no_frame_index < 10000 && no_frame_index % 100 == 0) {
                        log.error("no frame index: {}", no_frame_index);
                    }
                    if (no_frame_index > 10000 && no_frame_index < 100000 && no_frame_index % 1000 == 0) {
                        log.error("no frame index: {}", no_frame_index);
                    }
                    if (no_frame_index > 1000000 && no_frame_index % 10000 == 0) {
                        log.error("no frame index: {}", no_frame_index);
                    }
                    continue;
                }
                no_frame_index = 0;
                //不需要编码直接把音视频帧推出去
                recorder.recordPacket(packet);
                // 将缓存空间的引用计数-1，并将Packet中的其他字段设为初始值。如果引用计数为0，自动的释放缓存空间。
                av_packet_unref(packet);
            } catch (Exception e) {//推流失败
                err_index++;
                e.printStackTrace();
                if(err_index > 5) {
                    log.error("拉流推流错误次数超过5次，err_index: {}, app: {}, unique id: {}, origin: {}, push: {}",
                            err_index, app, uniqueId, originStream, pushStream);
                    break;
                }
            }
        }

        try {
            grabber.close();
            recorder.close();
            log.info("stream task stop， app: {}, unique id: {}, origin: {}, push: {}", app, uniqueId, originStream, pushStream);
        } catch (Exception e) {
            log.error("stream task stop error， app: {}, unique id: {}, origin: {}, push: {}", app, uniqueId, originStream, pushStream);
            e.printStackTrace();
        }

    }
}