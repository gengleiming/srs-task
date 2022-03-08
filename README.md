# srs流媒体服务器，流控制管理服务
## 项目简介
一个基于开源项目srs（项目地址：https://github.com/ossrs/srs）
流媒体服务器的流控制管理服务，目的是控制ffmpeg动态推拉流，
管理gb28181推流连接，实现接口控制流的开启、关闭、回收，以及连接的自动回收。

## 主要功能
- 支持通过rtsp流地址，分发rtmp、flv、hls、webrtc格式地址
- 支持gb28181协议，分发rtmp、flv、hls、webrtc格式地址
- 根据配置间，自动回收连接
- 根据配置，控制并行的流数量
- 云端视频流录制任务

## 服务启动
- 启动非gb28181流媒体服务器
  ```
  docker run --name srs --rm -p 1935:1935 -p 1985:1985 -p 8080:8080 -p 8000:8000/udp 
  --env CANDIDATE=192.168.18.151  -v ./srs.conf:/usr/local/srs/conf/srs.conf 
  registry.cn-hangzhou.aliyuncs.com/ossrs/srs:4 objs/srs -c conf/srs.conf
  ```
- rtsp推流到流媒体服务器
  ```
  ffmpeg -re -rtsp_transport tcp -i rtsp://admin:intellif123@192.168.18.5/live/livestream 
  -vcodec copy -acodec aac -f flv -y rtmp://192.168.18.151/live/livestream
  ```
- 启动gb28181流媒体服务器
  ```
  docker run -d --name gb-srs --restart=always -e CANDIDATE=182.168.12.108 -v 
  ./push.gb28181.conf:/usr/local/srs/conf/push.gb28181.conf -p 1935:1935 -p 1985:1985 -p 
  8080:8080 -p 5060:5060/udp -p 9000:9000 -p 8000:8000/udp 
  192.168.13.25:5002/building/srs:gb28181 ./objs/srs -c conf/push.gb28181.conf
  ```


## 视频流录制
- 不做任何修改，仅保存（cpu占用：1%以下）  
  ```
  ffmpeg -i rtmp://localhost:1935/live/34020000001320000010@34020000001320000010 -c copy 
  -f mp4 ./102.mp4
  ```
- 修改分辨率（cpu占用：150% - 200%）
  ```
  ffmpeg -i rtmp://localhost:1935/live/34020000001320000010@34020000001320000010 -vf scale=1920:1080 
  -f mp4 ./102.mp4
  ```