FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY roles-service/pom.xml roles-service/pom.xml
COPY users-service/pom.xml users-service/pom.xml
COPY protocols-service/pom.xml protocols-service/pom.xml
RUN mvn -B -pl protocols-service -am dependency:go-offline
COPY protocols-service/src protocols-service/src
RUN mvn -B -pl protocols-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/protocols-service/target/protocols-service-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
