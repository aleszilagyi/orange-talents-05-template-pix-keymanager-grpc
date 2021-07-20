FROM openjdk:11-jdk-slim
MAINTAINER Alexandre Szilagyi Santos
EXPOSE 50051
ARG JAR_FILE=build/libs/*-all.jar
ADD ${JAR_FILE} app.jar
ENV APP_NAME keymanager-grpc
ENTRYPOINT ["java", "-jar", "/app.jar"]
