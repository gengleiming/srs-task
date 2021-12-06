CREATE TABLE `stream_task`
(
    id            bigint primary key auto_increment,
    origin_stream varchar(255) not null comment '拉流地址',
    app           varchar(255) not null comment '应用',
    unique_id     varchar(255) not null comment '流唯一id，例如设备id',
    output_types  int          not null comment '支持的分发类型 1:rtmp 2:http-flv 4:hls 8:WebRTC',
    output_stream varchar(255) not null comment '分发流地址',
    service       varchar(255) comment '关联服务',
    status        smallint     not null comment '流任务状态 1.创建中 2.运行中 3.已关闭 4.与服务器断开连接',
    create_time   timestamp    null default current_timestamp,
    update_time   timestamp    null default null on update current_timestamp
) DEFAULT CHARSET = utf8 COMMENT ='流任务表';