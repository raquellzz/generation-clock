package imd.ufrn.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClienteGrpcMain {
    public static void main(String[] args) {
        
        // 1. Cria o Canal de Comunicação (O "Cano" de rede com o Servidor)
        // O .usePlaintext() é obrigatório para testes locais, pois desativa a exigência de HTTPS/SSL
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();

        System.out.println("=== Cliente gRPC Iniciado ===");

        try {
            // 2. Cria o "Stub" (O representante do servidor no lado do cliente)
            // Usamos o BlockingStub porque queremos que o código espere a resposta (Síncrono)
            DistribuidoServiceGrpc.DistribuidoServiceBlockingStub stub = 
                    DistribuidoServiceGrpc.newBlockingStub(channel);

            // 3. Monta a requisição usando o padrão Builder do Protobuf
            System.out.println("-> Construindo a primeira mensagem...");
            MensagemRequest requestSucesso = MensagemRequest.newBuilder()
                    .setPayload("Ola Servidor gRPC! Esta é uma mensagem limpa.")
                    .build();

            // 4. Faz a Chamada RPC (Parece um método local, mas viaja pela rede!)
            StatusResponse responseSucesso = stub.processarMensagem(requestSucesso);

            // 5. Imprime a resposta
            System.out.println("<- Resposta Recebida!");
            System.out.println("   Status: " + responseSucesso.getStatusCode());
            System.out.println("   Mensagem: " + responseSucesso.getMensagem());

            System.out.println("\n------------------------------------------------\n");

            // --- TESTE 2: SIMULANDO UMA REJEIÇÃO (PALAVRÃO) ---
            System.out.println("-> Construindo mensagem com palavrao...");
            MensagemRequest requestErro = MensagemRequest.newBuilder()
                    .setPayload("Teste de bloqueio com palavrao no texto!")
                    .build();

            StatusResponse responseErro = stub.processarMensagem(requestErro);
            
            System.out.println("<- Resposta Recebida!");
            System.out.println("   Status: " + responseErro.getStatusCode());
            System.out.println("   Mensagem: " + responseErro.getMensagem());

        } finally {
            // 6. É boa prática fechar o canal quando o cliente termina
            channel.shutdown();
            System.out.println("\n=== Canal encerrado ===");
        }
    }
}