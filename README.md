# IKA API

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.3-blue)
![Docker](https://img.shields.io/badge/Docker-27.2.0-blue)
![Localstack](https://img.shields.io/badge/Localstack-1.20.0-blue)
![AWS S3](https://img.shields.io/badge/AWS%20S3-mock%20via%20Localstack-yellowgreen)

A **IKA API** é uma aplicação Java desenvolvida com **Spring Boot** e PostgreSQL que gerencia o uso de medicamentos. A API permite registrar e acompanhar o consumo de medicamentos dos usuários por meio de vídeos, além de permitir a aprovação ou rejeição de registros por responsáveis.

## Funcionalidades

- 📹 **Upload de vídeos** de uso de medicamentos
- 📋 **Registro de medicamentos** e controle de estoques de usuários
- 👨‍⚕️ **Responsáveis podem aprovar ou rejeitar** registros de uso de medicamentos
- 📊 **Paginação e filtros** para buscar históricos de uso

## Tecnologias Utilizadas

- **Java 17**: Linguagem principal do projeto
- **Spring Boot**: Framework para criação de aplicações Java robustas e escaláveis
- **PostgreSQL 16.3**: Banco de dados relacional utilizado para armazenar informações dos medicamentos e usuários
- **AWS S3 (via Localstack)**: Armazenamento de vídeos simulando a integração com a AWS S3
- **Docker e Docker Compose**: Ferramentas para facilitar o desenvolvimento e deploy da aplicação
- **Maven**: Ferramenta de build e gerenciamento de dependências

## Requisitos

- **Docker** e **Docker Compose** instalados

## Como rodar o projeto

### 1. Clonar o repositório

```bash
git clone https://github.com/mathmore0000/ika-api.git
cd ika-api
```

### 2. Criar arquivo `.env`
```
cp .env.example .env
nano .env
```

### 3. Contruir a imagem da aplicação
```
docker build -t ika-api .
```

### 4. Rodar os contêineres
```
docker-compose up --build
```

## Modelagem do Banco de Dados

Abaixo está a modelagem do banco de dados utilizada no projeto. Ela representa as tabelas e os relacionamentos principais, como `User`, `Medication`, `Usage`, entre outras.

### Diagrama ER (Entidade-Relacionamento)
![MER](https://github.com/user-attachments/assets/caeac83b-0a7e-4b7b-961b-7f9e2c48edc4)

Essa modelagem inclui as tabelas principais relacionadas ao gerenciamento de medicamentos e o uso pelos usuários.

---

## Arquitetura da Aplicação

### Arquitetura V1

A primeira versão da arquitetura foi desenhada para uma solução mais simples, com arquitetura monolítica e focada na integração direta entre os componentes principais da aplicação.

![arq v1](https://github.com/user-attachments/assets/9cbb37df-a136-4300-ba6e-cbadf4e74678)

### Arquitetura V2

Na versão 2 da arquitetura, algumas melhorias e mudanças foram feitas para aumentar a escalabilidade e modularização do sistema. Dividimos a aplicação em serviços e melhoramos a separação de responsabilidades.

![arq v2](https://github.com/user-attachments/assets/5539d37c-2a55-4bad-a0b5-1b715789cbca)

### Comparação entre V1 e V2

- **V1**: Arquitetura mais simples, focada em uma solução monolítica.
- **V2**: Introdução de separação de serviços, aumentando a escalabilidade e permitindo maior flexibilidade para novas funcionalidades.

