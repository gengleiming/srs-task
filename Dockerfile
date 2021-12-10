FROM 192.168.13.25:5002/frolvlad/alpine-oraclejdk8-cpp-curl

VOLUME /tmp
ADD  target /app
ADD  build/entrypoint.sh /
COPY build/repositories /etc/apk/repositories
RUN  apk update --allow-untrusted && apk add --no-cache bash curl && apk add --no-cache ffmpeg

EXPOSE 9920
ENTRYPOINT ["/entrypoint.sh"]