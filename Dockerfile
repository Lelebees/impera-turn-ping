# Stage 1: Build the application with Maven
FROM maven:3.9.5-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY ./ /app
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/Impera-Bot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9010
ENTRYPOINT ["java", "-Xmx256m", "-XX:UseShenandoahGC", \
"-Dcom.sun.management.jmxremote=true", \
"-Dcom.sun.management.jmxremote.port=9010", \
"-Dcom.sun.management.jmxremote.local.only=false", \
"-Dcom.sun.management.jmxremote.authenticate=false", \
"-Dcom.sun.management.jmxremote.ssl=false ", \
"-Dcom.sun.management.jmxremote.rmi.port=9010", \
"-Djava.rmi.server.hostname=localhost", \
"-jar", "app.jar"]
