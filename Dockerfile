# Stage 1: Build the application
FROM gradle:8.1.1-jdk17 AS builder

WORKDIR /app
COPY . .
RUN gradle build --no-daemon

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /app/build/libs/attendance-processor.jar .

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "attendance-processor.jar"]