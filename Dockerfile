
# Stage 1: Build the Spring Boot application
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src/ src/

RUN ./mvnw -B -DskipTests package

# Stage 2: Run the packaged application
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/wallet-system-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
