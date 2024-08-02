FROM maven:3.8.4-jdk-8 AS build

COPY /api/src /app/src
COPY /api/pom.xml /app

WORKDIR /app
RUN mvn clean install

FROM openjdk:8-jre-alpine

COPY --from=build /app/target/api-0.0.1-SNAPSHOT.jar /app/app.jar

WORKDIR /app

EXPOSE 8080

# docker build -t eventostec-kipper-backend:1.0 /api/.
CMD ["java", "-jar", "app.jar"]