### Step 3.2: Create the Deployment S3 Bucket

```bash
aws --endpoint-url=http://localhost:4566 --profile localstack s3 mb s3://artifact-storage-bucket
```

### Step 3.1: Build the Project

```bash
sdk use java 17.0.13-tem
mvn clean package
```

### Step 3.3: Upload the JAR File to S3

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 s3 cp target/batch-generator-lambda-1.0-SNAPSHOT.jar s3://artifact-storage-bucket/batch-generator-lambda-1.0-SNAPSHOT.jar 
```

### Step 3.4: Deploy Resources with CloudFormation

```bash
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation deploy \
  --template-file template-localstack.yaml \
  --stack-name batch-generator-lambda
```


```shell
aws --profile localstack --endpoint-url=http://localhost:4566 lambda invoke --function-name arn:aws:lambda:eu-west-1:000000000000:function:BatchGeneratorLambda \
    --cli-binary-format raw-in-base64-out \
    --payload '{"customerNumber": "CustomerNumber-7", "status": "COMPLETED"}' output.txt
```
