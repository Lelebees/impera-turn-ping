# Stage 1: Build the application with Maven
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY ./ /app
RUN mvn clean package

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/Impera-Bot-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-XX:+UseShenandoahGC", "-jar", "app.jar"]