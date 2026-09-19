# --- build ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# copier le pom seul d'abord : tant qu'il ne change pas, ce layer est en cache
COPY pom.xml .
RUN mvn -q dependency:go-offline

COPY src ./src
RUN mvn -q package -DskipTests

# --- image finale (JRE seul, ~180 Mo au lieu de 600) ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# pas de root dans le conteneur
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
