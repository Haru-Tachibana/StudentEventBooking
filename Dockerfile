FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# eclipse-temurin:17-jre already includes curl (healthcheck)
COPY --from=build /build/target/*.jar app.jar
COPY firebase-service-account.json firebase-service-account.json

EXPOSE 9090
ENTRYPOINT ["java","-jar","/app/app.jar"]