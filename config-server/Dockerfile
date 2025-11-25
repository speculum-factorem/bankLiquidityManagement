# Build stage
FROM eclipse-temurin:17-jdk AS build
WORKDIR /workspace/app

# Copy Gradle files
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
COPY config-server config-server

# Install Gradle and build the application
RUN apt-get update && apt-get install -y wget unzip && \
    wget https://services.gradle.org/distributions/gradle-8.5-bin.zip && \
    unzip gradle-8.5-bin.zip && \
    mv gradle-8.5 /opt/gradle && \
    chmod +x gradlew && \
    /opt/gradle/bin/gradle :config-server:bootJar --no-daemon && \
    rm -rf gradle-8.5-bin.zip /opt/gradle

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /workspace/app/config-server/build/libs/*.jar app.jar

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

EXPOSE 8888

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
