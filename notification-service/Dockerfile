#
# Build stage
#
FROM maven:3.8.2-jdk-11 AS build
COPY . .
RUN mvn clean package -Pprod -DskipTests

#
# Package stage
#
FROM openjdk:11-jdk-slim
COPY --from=build /target/notification-service-0.0.1-SNAPSHOT.jar notification-service.jar
# ENV PORT=8081
EXPOSE 8082
ENTRYPOINT ["java","-jar", "notification-service.jar"]