package com.intellif.vesionbook.srstask.helper;

import com.intellif.vesionbook.srstask.config.ServerConfig;
import com.intellif.vesionbook.srstask.enums.StreamTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @description: 视频转码
 * @author: gengleiming
 * @createDate: 2021/12/01
 * @version: 1.0
 */
@Component
@Slf4j
public class FFCommandHelper {

    @Resource
    private ServerConfig serverConfig;

    public LinkedList<String> ffmpegCommandList(String originUrl, String pushUrl, int streamType){
        //拉流推流，视频转码
        LinkedList<String> ffmpegCmdList = new LinkedList<>();
        ffmpegCmdList.add("ffmpeg");
        ffmpegCmdList.add("-v");
        ffmpegCmdList.add(serverConfig.getFfmpegLogLevel());
        ffmpegCmdList.add("-re");
        if(streamType == StreamTypeEnum.RTSP.getCode()) {
            ffmpegCmdList.add("-rtsp_transport");
            ffmpegCmdList.add("tcp");
        }
        ffmpegCmdList.add("-i");
        ffmpegCmdList.add(originUrl);
        ffmpegCmdList.add("-vcodec");
        ffmpegCmdList.add("copy");
        ffmpegCmdList.add("-acodec");
        ffmpegCmdList.add("aac");
        ffmpegCmdList.add("-f");
        ffmpegCmdList.add("flv");
        ffmpegCmdList.add("-y");
        ffmpegCmdList.add(pushUrl);
        return ffmpegCmdList;
    }

    /**
     * 拉流推流，分发HTTP-Hlv和WebRTC地址
     * @param originUrl 拉流源地址
     * @return
     * @author zxzhang
     * @date 2019/12/10
     */
    public Process transcodeStream(String originUrl, String app, String uniqueId, String srsHost) {
        String pushUrl = "rtmp://" + srsHost + "/" + app + "/" + uniqueId;
        LinkedList<String> ffmpegCmdList;
        if(originUrl.startsWith(StreamTypeEnum.RTSP.getName())) {
            ffmpegCmdList = ffmpegCommandList(originUrl, pushUrl, StreamTypeEnum.RTSP.getCode());
        } else if(originUrl.startsWith(StreamTypeEnum.RTMP.getName())) {
            ffmpegCmdList = ffmpegCommandList(originUrl, pushUrl, StreamTypeEnum.RTMP.getCode());
        } else {
            log.error("origin stream error. origin stream: {}", originUrl);
            return null;
        }

        ProcessBuilder builder = new ProcessBuilder();
        builder.command(ffmpegCmdList);

        Process ffmpeg;
        try {
            ffmpeg = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        String cmdStr = Arrays.toString(ffmpegCmdList.toArray()).replace(",", "");
        log.info("---开始执行FFmpeg命令--- " + cmdStr);
        // 取出输出流和错误流的信息
        // 注意：必须要取出ffmpeg在执行命令过程中产生的输出信息，如果不取的话当输出流信息填满jvm存储输出留信息的缓冲区时，线程就回阻塞住
        PrintStream errorStream = new PrintStream(ffmpeg.getErrorStream());
        PrintStream inputStream = new PrintStream(ffmpeg.getInputStream());
        errorStream.start();
        inputStream.start();
        return ffmpeg;
    }
}

@Slf4j
class PrintStream extends Thread {
    java.io.InputStream __is;

    public PrintStream(java.io.InputStream is) {
        __is = is;
    }

    @Override
    public void run() {
        try {
            while (true) {
                int _ch = __is.read();
                if (_ch != -1) {
                    System.out.print((char) _ch);
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            log.info("ignore error when close stream. ffmpeg print stream exception: {}", e.getMessage());
        }
    }
}