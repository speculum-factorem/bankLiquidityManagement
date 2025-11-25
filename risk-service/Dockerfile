# Стадия сборки приложения
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Копирование файлов Gradle
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY risk-service risk-service

# Сборка приложения
RUN chmod +x gradlew && ./gradlew :risk-service:bootJar --no-daemon

# Стадия выполнения
FROM eclipse-temurin:17-jre
WORKDIR /app

# Копирование собранного JAR файла
COPY --from=build /workspace/app/risk-service/build/libs/*.jar app.jar

# Создание непривилегированного пользователя для безопасности
RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

# Открытие порта для сервиса оценки рисков
EXPOSE 8083

# Запуск приложения
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
