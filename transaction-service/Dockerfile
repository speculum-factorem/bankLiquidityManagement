FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY build/libs/transaction-service.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]