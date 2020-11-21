FROM adoptopenjdk:11-jre-hotspot
MAINTAINER 85danf@gmail.com

RUN install -d -m 0755 /opt/dlpengine/
WORKDIR /opt/dlpengine/

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} dlpengine.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","dlpengine.jar"]
