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

# Instala as bibliotecas necessárias para o JasperReports
RUN apt-get update && apt-get install -y \
    libfreetype6 \
    fontconfig \
    && rm -rf /var/lib/apt/lists/*

# Copia o JAR gerado na etapa de build
COPY --from=build /app/target/ika-api-1.0.0.jar /app/ika-api.jar

EXPOSE 8080

# Definir as variáveis de ambiente necessárias (use valores reais na execução do contêiner)
ENV SPRING_DATASOURCE_DB=${SPRING_DATASOURCE_DB}
ENV SPRING_DATASOURCE_HOST=${SPRING_DATASOURCE_HOST}
ENV SPRING_DATASOURCE_PORT=${SPRING_DATASOURCE_PORT}
ENV SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME}
ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD}
ENV JWT_SECRET_KEY=${JWT_SECRET_KEY}
ENV SPRING_PROFILE=${SPRING_PROFILE}

# Comando para executar a aplicação
CMD ["java", "-jar", "/app/ika-api.jar"]