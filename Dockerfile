FROM 192.168.13.25:5002/frolvlad/alpine-oraclejdk8-cpp-curl

VOLUME /tmp
ADD  target /app
ADD  build/entrypoint.sh /
COPY build/repositories /etc/apk/repositories
# RUN  apk update --allow-untrusted && apk add --no-cache bash curl && apk add --no-cache ffmpeg ffmpeg-libs
RUN echo "http://mirrors.aliyun.com/alpine/v3.6/main" > /etc/apk/repositories \
    && echo "http://mirrors.aliyun.com/alpine/v3.6/community" >> /etc/apk/repositories \
    && apk update upgrade \
    && apk add --no-cache curl bash tzdata \
    && apk add yasm && apk add ffmpeg \
    && ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone

EXPOSE 8085
ENTRYPOINT ["/entrypoint.sh"]