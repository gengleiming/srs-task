CREATE TABLE if not exists `video_recorder_task`
(
    id          bigint primary key auto_increment,
    app         varchar(255)  not null comment '应用',
    unique_id   varchar(255)  not null comment '设备id/流id/设备id@通道id',
    start_time  bigint        not null comment '录制开始时间戳（秒）',
    end_time    bigint        not null comment '录制结束时间戳（秒）',
    status      smallint      not null comment '录像任务状态 0.未开始 1.运行中 2.已结束 3. 任务异常',
    path        varchar(1000) comment '存储路径',
    create_time timestamp     null default current_timestamp,
    update_time timestamp     null default null on update current_timestamp
) DEFAULT CHARSET = utf8 COMMENT ='视频录制任务表';