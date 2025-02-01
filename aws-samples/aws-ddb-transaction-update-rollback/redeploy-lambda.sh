#!/bin/bash

LAMBDA_FILE="${1:-order-sync-lambda-1.0-SNAPSHOT.jar}"
LAMBDA_NAME="${2:-OrderSQSProcessor}"

 # Run Maven build
mvn clean package -DskipTests


echo "Redeploying Lambda"
aws lambda --profile localstack \
           --endpoint-url=http://localhost:4566 \
           update-function-code \
           --function-name "$LAMBDA_NAME" \
           --zip-file "fileb://target/$LAMBDA_FILE" | cat
