FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/stats-service-*.jar app.jar

EXPOSE 9090

CMD ["java", "-jar", "app.jar"]
