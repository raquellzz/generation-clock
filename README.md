
Comandos
- Gateway --- Por enquanto está hardcoded no GatewayMain

```
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="1 8080"
```

- Storage

```
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="1 8081"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="2 8082"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="3 8083"
```

- Validator
```
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="1 8084"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="2 8085"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="3 8086"
```