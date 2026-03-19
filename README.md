
Comandos
- Gateway --- Por enquanto está hardcoded no GatewayMain

```
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="8080 1"
```

- Storage

```
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="8081 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="8082 2"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="8083 3"
```

- Validator
```
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="8084 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="8085 2"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="8086 3"
```