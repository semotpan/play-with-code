# 🚀 DynamoDB CRUD Operations with LocalStack

## 📌 Tech Stack

- **Java 21** (via [SDKMAN!](https://sdkman.io/))
- **Maven Wrapper**
- **Docker Compose**
- **AWS CLI** (configured for LocalStack)

## 🏗️ Setup & Usage

### 0️⃣ Configure AWS CLI for LocalStack

Set up a LocalStack profile for AWS CLI:

```bash
aws configure set aws_access_key_id "key" --profile localstack
aws configure set aws_secret_access_key "secret" --profile localstack
aws configure set region "eu-west-1" --profile localstack
aws configure set output "json" --profile localstack
```

### 1️⃣ Start LocalStack with Docker Compose

Ensure Docker is running, then execute:
```shell
docker compose -f compose.yaml up -d
```

### 3️⃣ Delete CloudFormation Stack

Run the following AWS CLI command to delete the CloudFormation stack:
```shell
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation \
delete-stack --stack-name orders-sample
```

### 4️⃣ Drop Docker Compose with volumes
D Docker is running, then execute:
```shell
docker compose -f compose.yaml down -v
```
