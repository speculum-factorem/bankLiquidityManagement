FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/config-server-1.0.0.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]