# Projeto E-commerce utilizando Arquitetura de Microsserviços: Padrão Saga Orquestrado

# Sobre o projeto

Este projeto foi desenvolvido com base no curso Arquitetura de Microsserviços: Padrão Saga Orquestrado da plataforma de ensino Alura, ministrado pelo Victor Hugo Negrisoli.

## Tecnologias utilizadas

* **Java 17**
* **Spring Boot 3**
* **Apache Kafka com KRaft**
* **API REST**
* **PostgreSQL**
* **MongoDB**
* **Docker**
* **docker-compose**
* **Redpanda Console**

# Ferramentas utilizadas

* **IntelliJ IDEA Ultimate Edition**
* **Docker**
* **Maven**
* **Git**

# Formas de executar o projeto

### 01 - Executando pelo script Python (Forma mais fácil!)

Basta executar o script python na raiz do projeto (Esse script compila todos os projetos e sobe os serviços, bancos de dados e o Apache Kafka no Docker):
```bash
python3 build.py
```

### 02 - Docker Compose

Para executar o projeto utilizando o docker-compose, basta executar o comando abaixo na raiz do projeto(É necessário compilar os projetos antes de executar o comando):
```bash
`docker-compose up --build -d`
```

### 03 - Executando os projetos individualmente

Para executar os projetos individualmente é necessário subir os bancos de dados e o Apache Kafka, para isso, basta executar o comando abaixo na raiz do projeto:
```bash
`docker-compose up --build -d order-db kafka product-db payment-db inventory-db`
```

E executar os projetos individualmente pela IDE ou pelo Maven(Se for executar pela IDE adicionar o perfil de dev!).
```bash
mvn spring-boot:run -Dspring.profiles.active=dev -DskipTests
```

## Portas dos serviços

* Order-Service: 3000
* Orchestrator-Service: 8080
* Product-Validation-Service: 8090
* Payment-Service: 8091
* Inventory-Service: 8092
* Apache Kafka: 9092
* Redpanda Console: 8081
* PostgreSQL (product-db): 5432
* PostgreSQL (payment-db): 5433
* PostgreSQL (inventory-db): 5434
* MongoDB (order-db): 27017

## Swagger para ver os endpoints do Order-Service

* Order-Service: http://localhost:3000/api/swagger-ui/index.html

## Redpanda Console para ver os tópicos do Apache Kafka

* Redpanda Console: http://localhost:8081/

## Produtos registrados e seu estoque

Existem 4 produtos iniciais cadastrados no serviço `product-service` e suas quantidades disponíveis em `inventory-service`:
(Para alterar os produtos cadastrados, basta alterar o arquivo `import.sql` no projeto `product-service` e `inventory-service`)

* **COMIC_BOOKS** (4 em estoque)
* **BOOKS** (2 em estoque)
* **MOVIES** (5 em estoque)
* **MUSIC** (9 em estoque)

### Endpoint para iniciar a saga:

**POST** http://localhost:3000/api/order

Payload:

```json
{
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.50
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.90
      },
      "quantity": 1
    }
  ]
}
```

Resposta:

```json
{
  "id": "64429e987a8b646915b3735f",
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.5
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.9
      },
      "quantity": 1
    }
  ],
  "createdAt": "2023-04-21T14:32:56.335943085",
  "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519"
}
```

### Endpoint para visualizar a saga:

É possível recuperar os dados da saga pelo **orderId** ou pelo **transactionId**, exemplo:

**GET** http://localhost:3000/api/event?orderId=64429e987a8b646915b3735f

**GET** http://localhost:3000/api/event?transactionId=1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519

Resposta:

```json
{
  "id": "64429e9a7a8b646915b37360",
  "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519",
  "orderId": "64429e987a8b646915b3735f",
  "payload": {
    "id": "64429e987a8b646915b3735f",
    "products": [
      {
        "product": {
          "code": "COMIC_BOOKS",
          "unitValue": 15.5
        },
        "quantity": 3
      },
      {
        "product": {
          "code": "BOOKS",
          "unitValue": 9.9
        },
        "quantity": 1
      }
    ],
    "totalAmount": 56.40,
    "totalItems": 4,
    "createdAt": "2023-04-21T14:32:56.335943085",
    "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519"
  },
  "source": "ORCHESTRATOR",
  "status": "SUCCESS",
  "eventHistory": [
    {
      "source": "ORCHESTRATOR",
      "status": "SUCCESS",
      "message": "Saga started!",
      "createdAt": "2023-04-21T14:32:56.78770516"
    },
    {
      "source": "PRODUCT_VALIDATION_SERVICE",
      "status": "SUCCESS",
      "message": "Products are validated successfully!",
      "createdAt": "2023-04-21T14:32:57.169378616"
    },
    {
      "source": "PAYMENT_SERVICE",
      "status": "SUCCESS",
      "message": "Payment realized successfully!",
      "createdAt": "2023-04-21T14:32:57.617624655"
    },
    {
      "source": "INVENTORY_SERVICE",
      "status": "SUCCESS",
      "message": "Inventory updated successfully!",
      "createdAt": "2023-04-21T14:32:58.139176809"
    },
    {
      "source": "ORCHESTRATOR",
      "status": "SUCCESS",
      "message": "Saga finished successfully!",
      "createdAt": "2023-04-21T14:32:58.248630293"
    }
  ],
  "createdAt": "2023-04-21T14:32:58.28"
}
```