# Stage 1: Build the application with Maven
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder
COPY . /usr/src/
RUN mvn -f /usr/src/pom.xml clean install -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /usr/src/target/*.jar ./app.jar

ENTRYPOINT ["java", "-Xmx128m", "-XX:+UseShenandoahGC", \
"-XX:MaxMetaspaceSize=128m", "-XX:+UseCompressedClassPointers", "-XX:+UseCompressedOops", \
"-jar", "app.jar"]
