FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY data/pom.xml data/
COPY domain/pom.xml domain/
COPY api/pom.xml api/

RUN mvn -B dependency:go-offline

COPY . .

RUN mvn clean install -DskipTests

FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/api/target/rabobank-assignment-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]