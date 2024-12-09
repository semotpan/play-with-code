#!/bin/bash
set -eo pipefail
ARTIFACT_BUCKET=$(cat bucket-name.txt)
TEMPLATE=template-mvn.yaml
mvn package
#if [ $1 ]
#then
#  if [ $1 = mvn ]
#  then
#    TEMPLATE=template-mvn.yml
#    mvn package
#  fi
#else
#  gradle build -i
#fi
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation package --template-file $TEMPLATE --s3-bucket $ARTIFACT_BUCKET --output-template-file out.yml
aws --profile localstack --endpoint-url=http://localhost:4566 cloudformation deploy --template-file out.yml --stack-name cloudformation-localstack --capabilities CAPABILITY_NAMED_IAM