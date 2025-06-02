FROM eclipse-temurin:17-jre-alpine
LABEL maintainer="village1031 <village1031@gmail.com>"
LABEL version="1.0"
COPY ./build/libs/gandalp-api-0.0.1-SNAPSHOT.jar /root
ARG BUILD_PORT=8080
ENV TZ=Asia/Seoul
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE ${BUILD_PORT}
WORKDIR /root
CMD [ "java", "-jar", "gandalp-api-0.0.1-SNAPSHOT.jar" ]