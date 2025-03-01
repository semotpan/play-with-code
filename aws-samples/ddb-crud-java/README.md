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

### 2️⃣ Create CloudFormation Stack

Deploy the CloudFormation stack to provision DynamoDB tables:
```shell
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation deploy \
    --template-file  cfTemplate.yaml \
    --stack-name orders-sample
```

### 3️⃣ Generate Data
Run main class to generate about 10,000 items:
```shell
io.awssamples.DynamoDbDataGenerator
```

### 4️⃣Run Benchmarks
Package the application:
```shell
./mvnw clean package
```

### 5️⃣ Run Jar to get benchmarks
```shell
java -jar target/ddb-crud-java-1.0-SNAPSHOT.jar
```

### 6️⃣ Delete CloudFormation Stack

Run the following AWS CLI command to delete the CloudFormation stack:
```shell
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation \
delete-stack --stack-name orders-sample
```

### 7️⃣ Drop Docker Compose with Volumes
If Docker is running, then execute:
```shell
docker compose -f compose.yaml down -v
```
