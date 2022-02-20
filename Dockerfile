FROM openjdk:15-alpine
MAINTAINER atanava777@gmail.com
COPY target/geo-locator-0.0.1-SNAPSHOT.jar geo-locator-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/geo-locator-0.0.1-SNAPSHOT.jar"]