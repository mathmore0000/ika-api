# Etapa 1: Build da aplicação
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

COPY pom.xml .
COPY .mvn/ .mvn
COPY mvnw .
COPY mvnw.cmd .
COPY src ./src

# Baixar dependências e compilar o projeto, ignorando testes
RUN mvn clean package -DskipTests

# Etapa 2: Imagem final
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/ika-api-0.0.1.jar /app/ika-api.jar

EXPOSE 8080
EXPOSE 443

# Definir as variáveis de ambiente necessárias (use valores reais na execução do contêiner)
ENV SPRING_DATASOURCE_DB=${SPRING_DATASOURCE_DB}
ENV SPRING_DATASOURCE_HOST=${SPRING_DATASOURCE_HOST}
ENV SPRING_DATASOURCE_PORT=${SPRING_DATASOURCE_PORT}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENV SERVER_SSL_KEY_STORE_PASSWORD=${SERVER_SSL_KEY_STORE_PASSWORD}
ENV SERVER_SSL_KEY_STORE=${SERVER_SSL_KEY_STORE}
ENV SPRING_PROFILE=${SPRING_PROFILE}

# Comando para executar a aplicação
CMD ["java", "-Dspring.profiles.active=${SPRING_PROFILE}", "-jar", "/app/ika-api.jar"]