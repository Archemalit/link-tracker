FROM eclipse-temurin:23-jdk-alpine

ARG JAR_FILE=/bot/target/*.jar
ARG APP_DIR=/opt/bot

RUN mkdir $APP_DIR
WORKDIR $APP_DIR
COPY $JAR_FILE ./bot.jar

EXPOSE 8080
ENTRYPOINT java -Dspring.profiles.active=prod -jar ./bot.jar
