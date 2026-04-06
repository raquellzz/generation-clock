package imd.ufrn.grpc;

import java.io.IOException;

import io.grpc.Server;
import io.grpc.ServerBuilder;

public class ServidorGrpcMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        int porta = 9090;

        System.out.println("=== Iniciando Servidor gRPC na porta " + porta + " ===");

        // Cria e inicia o servidor atrelando o nosso Serviço a ele
        Server server = ServerBuilder.forPort(porta)
                .addService(new SistemaServiceImpl())
                .build()
                .start();

        System.out.println("-> Servidor gRPC rodando ativamente!");

        // Faz o servidor ficar vivo rodando em background até o programa ser interrompido
        server.awaitTermination();
    }
}