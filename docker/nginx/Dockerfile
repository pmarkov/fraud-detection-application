FROM nginx:1.21.1-alpine

RUN rm -rf /usr/share/nginx/html
COPY ./src/main/resources/static /usr/share/nginx/html
COPY ./docker/nginx/conf.d /etc/nginx/conf.d

EXPOSE 80 8081 443