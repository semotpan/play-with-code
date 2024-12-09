
```shell
mvn clean package
```
Create deployment local stack bucket
```shell
aws --endpoint-url=http://127.0.0.1:4566 --profile localstack s3api create-bucket --bucket local-deploy-bucket
aws --endpoint-url=http://localhost:4566 --profile localstack  s3 mb s3://localstack-bucket
```

### list buckets
```shell
aws --profile localstack --endpoint-url=http://localhost:4566 s3api list-buckets
```

## Copy jar to localstack bucket
```shell
 aws --endpoint-url=http://localhost:4566 s3 cp target/java-dagger-localstack-sample-1.0-SNAPSHOT.jar s3://localstack-bucket/java-dagger-localstack-sample-1.0-SNAPSHOT.jar
```
### deploy localstack cloudformation template
```shell
aws --profile localstack cloudformation deploy --template-file template-localstack.yaml --stack-name sqs-lambda-dynamodb --endpoint-url=http://localhost:4566
```

### describe stack events
```shell
 aws cloudformation describe-stack-events --stack-name sqs-lambda-dynamodb --profile localstack --endpoint-url=http://localhost:4566
```

```shell
aws cloudformation package --template-file template.yml --s3-bucket local-deploy-bucket --output-template-file out.yml
aws --endpoint-url=http://localhost:4566 --profile localstack cloudformation deploy --stack-name cloudformation-localstack --template-file template-mvn.yaml
```
