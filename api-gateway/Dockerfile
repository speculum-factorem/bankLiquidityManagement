# Стадия сборки приложения
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Копирование файлов Gradle
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY api-gateway api-gateway

# Сборка приложения
RUN chmod +x gradlew && ./gradlew :api-gateway:bootJar --no-daemon

# Стадия выполнения
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копирование собранного JAR файла
COPY --from=build /workspace/app/api-gateway/build/libs/*.jar app.jar

# Создание непривилегированного пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Открытие порта для API Gateway
EXPOSE 8080

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
