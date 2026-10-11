# Build stage: compile with Maven, producing the executable jar
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B dependency:go-offline
COPY src ./src
RUN ./mvnw -B clean package -DskipTests

# Run stage: just the JRE and the built jar, nothing else
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Railway/Render set PORT themselves; the app reads it via application.properties.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
