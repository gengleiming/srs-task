#FROM marklib/maven:3.6.1_campus AS compile_stage
#FROM registry.cn-hangzhou.aliyuncs.com/acs/maven:3-jdk-8 AS compile_stage
FROM maven:3.6-jdk-11-openj9 AS compile_stage

VOLUME /tmp
ADD  target /app
ADD  build/entrypoint.sh /
COPY build/repositories /etc/apk/repositories
RUN  apk update --allow-untrusted && apk add --no-cache bash curl ffmpeg

EXPOSE 9920
ENTRYPOINT ["/entrypoint.sh"]