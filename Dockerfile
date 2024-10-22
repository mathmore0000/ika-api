FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y maven

WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
COPY mvnw.cmd .

RUN mvn dependency:go-offline

COPY src ./src

RUN mvn clean package -DskipTests

EXPOSE 8080

ENV SPRING_DATASOURCE_DB=ika
ENV SPRING_DATASOURCE_HOST=postgres-ika
ENV SPRING_DATASOURCE_PORT=5432
ENV SPRING_DATASOURCE_USERNAME=postgres
ENV SPRING_DATASOURCE_PASSWORD=password
ENV SPRING_FLYWAY_PROFILE=local
ENV JWT_SECRET_KEY=super-duper-mega-real-secret-key

CMD ["java", "-jar", "target/ika-api-0.0.1.jar"]
