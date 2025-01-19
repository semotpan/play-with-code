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

### Step 3: Deploy the Application

1. Build the project:

   ```bash
   mvn clean package
   ```

2. Create the deployment S3 bucket:

   ```bash
   aws --endpoint-url=http://localhost:4566 --profile localstack s3 mb s3://artifact-storage-bucket
   ```

3. Upload the JAR file to the LocalStack S3 bucket:

   ```bash
   aws --profile localstack --endpoint-url=http://localhost:4566 s3 cp target/order-sync-lambda-1.0-SNAPSHOT.jar s3://artifact-storage-bucket/order-sync-lambda-1.0-SNAPSHOT.jar
   ```

4. Deploy AWS resources to LocalStack using CloudFormation:

   ```bash
   aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation deploy \
     --template-file template-localstack.yaml --stack-name order-sync-sample
   ```

---

### Step 4: Interact with the Application

#### Send Data to SQS Queue
Send a message to the SQS queue to trigger actions:

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 sqs send-message \
  --queue-url http://sqs.eu-west-1.localhost.localstack.cloud:4566/000000000000/product-sqs-queue \
  --message-body '{ "name": "Banana", "price": 10.5 }'
```


#### CloudFormation
- Describe stack events:

  ```bash
  aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation describe-stack-events \
    --stack-name order-sync-sample
  ```

#### Scan DynamoDB Table Data
Retrieve data from the DynamoDB table:

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 dynamodb scan --table-name order
```

---