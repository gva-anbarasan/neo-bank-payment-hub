FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY target/ledger-worker-*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]