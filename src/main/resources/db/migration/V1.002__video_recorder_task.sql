CREATE TABLE if not exists `video_recorder_task`
(
    id            bigint primary key auto_increment,
    app           varchar(255)  not null comment '应用',
    unique_id     varchar(255)  not null comment '设备id/流id/认证id',
    channel_id    varchar(255)  null comment '通道id',
    origin_stream varchar(1000) null comment 'rtsp流地址',
    start_time    bigint        not null comment '录制开始时间戳（秒）',
    end_time      bigint        not null comment '录制结束时间戳（秒）',
    status        smallint      not null comment '录像任务状态 1.未开始 2.运行中 3.已结束 4. 任务异常',
    stream_type   smallint      not null comment '流类型 1:rtsp 2:gb28181',
    path          varchar(1000) comment '存储路径',
    create_time   timestamp     null default current_timestamp,
    update_time   timestamp     null default null on update current_timestamp
) DEFAULT CHARSET = utf8 COMMENT ='视频录制任务表';