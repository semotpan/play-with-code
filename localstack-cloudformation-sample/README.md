### aws localstack profile setup

```shell
aws configure set aws_access_key_id "dummy" --profile localstack
aws configure set aws_secret_access_key "dummy" --profile localstack
aws configure set region "eu-west-1" --profile localstack
aws configure set output "table" --profile localstack
```





aws --endpoint-url=http://localhost:4566 cloudformation deploy --profile localstack --stack-name cf-sample-template.yml --template-file "cf-sample-template.yml"

aws --endpoint-url=http://127.0.0.1:4566 sqs list-queues --profile localstack
aws --endpoint-url=http://127.0.0.1:4566 s3api list-buckets --profile localstack