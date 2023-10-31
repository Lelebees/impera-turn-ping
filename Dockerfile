# Stage 1: Build the application with Maven
FROM maven:3.8.4-openjdk-17 AS builder
WORKDIR /app
COPY ./ /app
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/Impera-Bot-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-XX:+UseShenandoahGC", "-Xmx512m","-jar", "-Dcom.sun.management.jmxremote.port=8080", "-Dcom.sun.management.jmxremote.ssl=false ", "-Dcom.sun.management.jmxremote.authenticate=false", "-Dcom.sun.management.jmxremote.local.only=false", "-Dcom.sun.management.jmxremote","app.jar"]
