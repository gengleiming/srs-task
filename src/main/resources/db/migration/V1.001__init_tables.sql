CREATE TABLE `stream_task`
(
    id              bigint primary key auto_increment,
    origin_stream   varchar(255) not null comment '拉流地址',
    app             varchar(255) not null comment '应用',
    unique_id       varchar(255) not null comment '流唯一id，例如设备id',
    rtmp_output     varchar(255) comment 'rtmp分发地址，为空则不支持该协议',
    http_flv_output varchar(255) comment 'http-flv分发地址，为空则不支持该协议',
    hls_output      varchar(255) comment 'hls分发地址，为空则不支持该协议',
    webrtc_output   varchar(255) comment 'WebRTC分发地址，为空则不支持该协议',
    service         varchar(255) comment '关联服务',
    status          smallint     not null comment '流任务状态 1.运行中 2.已关闭',
    forever         tinyint           default 0 comment '是否提供永久流服务，异常关闭后会自动拉起',
    create_time     timestamp    null default current_timestamp,
    update_time     timestamp    null default null on update current_timestamp
) DEFAULT CHARSET = utf8 COMMENT ='流任务表';