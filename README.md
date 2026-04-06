
Comandos
- Gateway 

```
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="1"
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

Comandos por protocolo

- UDP
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="UDP"

    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="UDP 8081 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="UDP 8082 2"

    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="UDP 8084 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="UDP 8085 2"

- TCP
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="TCP"

    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="TCP 8081 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="TCP 8082 2"

    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="TCP 8084 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="TCP 8085 2"

- GRPC
    mvn exec:java -Dexec.mainClass="imd.ufrn.gateway.GatewayMain" -Dexec.args="GRPC"

    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="GRPC 8081 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.storage.StorageMain" -Dexec.args="GRPC 8082 2"

    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="GRPC 8084 1"
    mvn exec:java -Dexec.mainClass="imd.ufrn.validator.ValidatorMain" -Dexec.args="GRPC 8085 2"