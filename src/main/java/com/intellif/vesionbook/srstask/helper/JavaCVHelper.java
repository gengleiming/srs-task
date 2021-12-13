package com.intellif.vesionbook.srstask.helper;

import com.intellif.vesionbook.srstask.cache.StreamTaskCache;
import com.intellif.vesionbook.srstask.config.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
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
        FFmpegFrameGrabber grabber = getGrabber(originStream);
        if (grabber == null) {
            log.error("拉流失败，请检查视频流，originStream: {}, app: {}, uniqueId: {}", originStream, app, uniqueId);
            return;
        }

        String pushStream = "rtmp://" + serverConfig.getSrsHost() + "/" + app + "/" + uniqueId;

        FFmpegFrameRecorder recorder = getRecorder(grabber, pushStream);
        if (recorder == null) {
            log.error("推流失败，请检查视频流，originStream: {}, app: {}, uniqueId: {}, pushStream: {}",
                    originStream, app, uniqueId, pushStream);
            return;
        }

        Thread currentThread = Thread.currentThread();
        streamTaskCache.storeThread(app, uniqueId, currentThread);

        transcodeStream(grabber, recorder, app, uniqueId, originStream, pushStream);

    }

    public FFmpegFrameGrabber getGrabber(String input) {
        try {
            FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(input);

            if (input.contains("rtsp")) {
                // tcp用于解决丢包问题
                grabber.setOption("rtsp_transport", "tcp");
                //首选TCP进行RTP传输
                grabber.setOption("rtsp_flags", "prefer_tcp");
            }
            // 设置采集器构造超时时间, 5s
            grabber.setOption("stimeout", "5000000");
            grabber.setOption("vcodec", "copy");
            grabber.start();
            return grabber;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("拉流失败，请检查视频流，input: {}", input);
        }

        return null;
    }

    public FFmpegFrameRecorder getRecorder(FFmpegFrameGrabber grabber, String output) {
        try {
            // 录制/推流器
            FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(output, grabber.getImageWidth(),
                    grabber.getImageHeight(), grabber.getAudioChannels());
            // 两个关键帧之间的帧数, 设置gop,与帧率相同，相当于间隔1秒chan's一个关键帧
            recorder.setGopSize((int) grabber.getFrameRate());
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setVideoBitrate(grabber.getVideoBitrate());

            recorder.setAudioChannels(grabber.getAudioChannels());
            recorder.setAudioBitrate(grabber.getAudioBitrate());
            recorder.setSampleRate(grabber.getSampleRate());

            AVFormatContext oc = null;
            // 封装格式flv
            if(output.startsWith("rtmp")) {
                recorder.setFormat("flv");
                recorder.setAudioCodecName("aac");
                recorder.setVideoCodec(grabber.getVideoCodec());
                oc = grabber.getFormatContext();
            }
            recorder.start(oc);
            return recorder;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("推流失败，请检查srs服务器, output: {}", output);
        }
        return null;
    }

    public void transcodeStream(FFmpegFrameGrabber grabber, FFmpegFrameRecorder recorder,
                                String app, String uniqueId, String input, String output) {
        int no_frame_index = 0;
        int err_index = 0;

        try {
            // 释放探测时缓存下来的数据帧，避免pts初始值不为0导致画面延时
            grabber.flush();
        } catch (Exception e) {
            log.error("grabber flush error, app: {}, unique id: {}, input: {}, output: {}",
                    app, uniqueId, input, output);
        }

        Thread currentThread = Thread.currentThread();
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
                err_index = 0;
            } catch (Exception e) {//推流失败
                err_index++;
                e.printStackTrace();
                if (err_index > 5) {
                    log.error("拉流推流错误次数超过5次，err_index: {}, app: {}, unique id: {}, input: {}, output: {}",
                            err_index, app, uniqueId, input, output);
                    break;
                }
            }
        }

        try {
            grabber.close();
            recorder.close();
            log.info("stream task stop， app: {}, unique id: {}, input: {}, output: {}", app, uniqueId, input, output);
        } catch (Exception e) {
            log.error("stream task stop error， app: {}, unique id: {}, input: {}, output: {}", app, uniqueId, input, output);
            e.printStackTrace();
        }

    }
}