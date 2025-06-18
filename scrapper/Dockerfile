FROM eclipse-temurin:23-jdk-alpine

ARG JAR_FILE=/scrapper/target/*.jar
ARG APP_DIR=/opt/app

RUN mkdir $APP_DIR
WORKDIR $APP_DIR
COPY $JAR_FILE ./app.jar

EXPOSE 8081
ENTRYPOINT java -Dspring.profiles.active=prod,jpa -jar ./app.jar
