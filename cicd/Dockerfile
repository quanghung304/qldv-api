ARG ENV_NAME
FROM 10.0.142.13:8443/mis/qldv-api:base$ENV_NAME as build
WORKDIR /build
COPY . .
COPY cicd/maven-wrapper.properties .mvn/wrapper/
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw package -Daether.connector.https.securityMode=insecure -DskipTests -f pom.xml

FROM 10.0.142.13:8443/cicd/eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /build/target/*.jar run.jar
ENTRYPOINT ["java", "-jar", "/app/run.jar"]