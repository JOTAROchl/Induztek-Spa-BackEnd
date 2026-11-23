FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY pom.xml .
COPY roles-service/pom.xml roles-service/pom.xml
COPY users-service/pom.xml users-service/pom.xml
RUN mvn -B -pl roles-service -am dependency:go-offline
COPY roles-service/src roles-service/src
RUN mvn -B -pl roles-service -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/roles-service/target/roles-service-1.0.0.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
