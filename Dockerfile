# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/shopworthy-payments-*.jar app.jar
EXPOSE 6000
ENTRYPOINT ["java", "-jar", "app.jar"]
