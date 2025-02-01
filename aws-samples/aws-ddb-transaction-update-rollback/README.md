# README: Order Sync Lambda Deployment and Setup

This guide outlines the steps to configure, deploy, and interact with the Order Sync Lambda application in a LocalStack environment.

---

## Getting Started

### Step 1: Configure AWS CLI for LocalStack

Set up a LocalStack profile for AWS CLI:

```bash
aws configure set aws_access_key_id "key" --profile localstack
aws configure set aws_secret_access_key "secret" --profile localstack
aws configure set region "eu-west-1" --profile localstack
aws configure set output "json" --profile localstack
```

### Step 2: Start Docker Containers

Run the following command from the project's root directory to start the LocalStack containers:

```bash
docker compose -f localstack-compose.yaml up
```

---

## Deploy the Application

### Step 3.1: Build the Project

```bash
sdk use java 17.0.13-tem
mvn clean package
```

### Step 3.2: Create the Deployment S3 Bucket

```bash
aws --endpoint-url=http://localhost:4566 --profile localstack s3 mb s3://artifact-storage-bucket
```

### Step 3.3: Upload the JAR File to S3

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 s3 cp target/order-sync-lambda-1.0-SNAPSHOT.jar s3://artifact-storage-bucket/order-sync-lambda-1.0-SNAPSHOT.jar
```

### Step 3.4: Deploy Resources with CloudFormation

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation deploy \
  --template-file template-localstack.yaml \
  --stack-name order-sync-sample
```

---

## Step 4: Interact with the Application

### 4.1: Send Data to SQS Queue

Send a message to the SQS queue:

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://sqs.eu-west-1.localhost.localstack.cloud:4566/000000000000/orderLocked_fifo \
  --message-body '{ "lockId": "91f19927-b855-4077-b539-fdb001501b53", "productNumber": "Product-1" }'
```

### 4.2: CloudFormation Stack Operations

#### Describe Stack Events

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation describe-stack-events \
  --stack-name order-sync-sample
```

### 4.3: Query DynamoDB

#### Scan Table Data

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodb scan \
  --table-name order
```

#### Describe Table

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodb describe-table \
  --table-name order
```

#### List DynamoDB Streams

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodbstreams list-streams
```

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodbstreams describe-stream \
  --stream-arn arn:aws:dynamodb:eu-west-1:000000000000:table/order/stream/2025-01-20T13:59:41.024
```

#### Scan with Filter Expression

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodb scan \
  --table-name order \
  --filter-expression "productNumber = :productNumber" \
  --expression-attribute-values '{":productNumber":{"S":"Product-1"}}'
```

#### Count all items form db
```shell
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodb scan --table-name order --select COUNT
```

#### Create Event Source Mapping for Lambda

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 lambda create-event-source-mapping \
  --function-name publishNewOrder \
  --event-source arn:aws:dynamodb:eu-west-1:000000000000:table/order/stream/2025-01-20T13:59:41.024 \
  --batch-size 1 \
  --starting-position TRIM_HORIZON
```

---
