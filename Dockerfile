FROM eclipse-temurin:17-jdk-alpine

COPY ./target/Impera-Bot-0.0.1-SNAPSHOT.jar app.jar

# CMD ["docker-compose", "up"]
CMD ["./mvnw", "compile"]
ENTRYPOINT ["java", "-XX:+UseShenandoahGC", "-jar", "app.jar"]
