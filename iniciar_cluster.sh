#!/bin/bash

if [ -z "$1" ]; then
    echo "Erro: Você precisa informar o protocolo!"
    echo "Uso correto: ./iniciar_cluster.sh [UDP | TCP | GRPC]"
    exit 1
fi

PROTOCOL=$(echo "$1" | tr '[:lower:]' '[:upper:]')

echo "Iniciando o cluster com protocolo: $PROTOCOL"

gnome-terminal --title="Gateway ($PROTOCOL)" -- bash -c "mvn exec:java -Dexec.mainClass=\"imd.ufrn.gateway.GatewayMain\" -Dexec.args=\"$PROTOCOL\"; exec bash"

echo "Aguardando o Gateway iniciar..."
sleep 3

gnome-terminal --title="Storage 1 ($PROTOCOL)" -- bash -c "mvn exec:java -Dexec.mainClass=\"imd.ufrn.storage.StorageMain\" -Dexec.args=\"$PROTOCOL 8081 1\"; exec bash"
gnome-terminal --title="Storage 2 ($PROTOCOL)" -- bash -c "mvn exec:java -Dexec.mainClass=\"imd.ufrn.storage.StorageMain\" -Dexec.args=\"$PROTOCOL 8082 2\"; exec bash"

gnome-terminal --title="Validator 1 ($PROTOCOL)" -- bash -c "mvn exec:java -Dexec.mainClass=\"imd.ufrn.validator.ValidatorMain\" -Dexec.args=\"$PROTOCOL 8084 1\"; exec bash"
gnome-terminal --title="Validator 2 ($PROTOCOL)" -- bash -c "mvn exec:java -Dexec.mainClass=\"imd.ufrn.validator.ValidatorMain\" -Dexec.args=\"$PROTOCOL 8085 2\"; exec bash"

echo "Cluster $PROTOCOL disparado com sucesso! Verifique as novas janelas."