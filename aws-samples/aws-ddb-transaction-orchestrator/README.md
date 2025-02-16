# TODO: IN PROGRESS
### Start docker
```shell
docker compose -f compose.yaml up
```

### Deployment Steps:
```shell
aws --endpoint-url=http://localhost:4566 cloudformation deploy \
  --template-file template.yaml \
  --stack-name transaction-ochestrator
```