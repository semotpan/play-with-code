## How-to guide

---

### Run localstack snowflake emulator
```shell
docker run \
  --rm -it \
  -p 127.0.0.1:4566:4566 \
  -p 127.0.0.1:4510-4559:4510-4559 \
  -p 127.0.0.1:443:443 \
  -e ACTIVATE_PRO=0 \
  -e LOCALSTACK_AUTH_TOKEN=ls-jiWukUkI-moVi-7324-6569-ToWOGAPo5956 \
  localstack/snowflake
```

### Package JAR
```shell
mvn clean package
```

### Run application 
```shell
java --add-opens=java.base/java.nio=org.apache.arrow.memory.core,ALL-UNNAMED -jar target/snowflake-jdbc-localstack-sample-1.0-SNAPSHOT-jar-with-dependencies.jar
```
