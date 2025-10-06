# Etap builda – Maven + JDK 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etap produkcyjny – tylko JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app

# Stwórz katalog logs
RUN mkdir -p /app/logs && chmod 777 /app/logs

COPY --from=build /app/target/backend.jar app.jar

EXPOSE 7000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD curl -f http://localhost:7000/health || exit 1

CMD ["java", "-jar", "app.jar"]