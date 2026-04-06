package imd.ufrn.grpc;

import io.grpc.stub.StreamObserver;

public class SistemaServiceImpl extends DistribuidoServiceGrpc.DistribuidoServiceImplBase {

    @Override
    public void processarMensagem(MensagemRequest request, StreamObserver<StatusResponse> responseObserver) {
        
        String payload = request.getPayload();
        System.out.println("-> [gRPC] Iniciando validação do payload: [" + payload + "]");
        
        String msgResposta;
        int statusCode;

        if (payload == null || payload.trim().isEmpty()) {
            statusCode = 400;
            msgResposta = "REJEITADA: A mensagem está vazia.";
            System.out.println(msgResposta);
        } else if (payload.toLowerCase().contains("palavrao")) {
            statusCode = 403;
            msgResposta = "REJEITADA: A mensagem contém conteúdo impróprio.";
            System.out.println(msgResposta);
        } else {
            statusCode = 200;
            msgResposta = "APROVADA: A mensagem é válida e limpa!";
            System.out.println(msgResposta);
        }

        StatusResponse response = StatusResponse.newBuilder()
                .setStatusCode(statusCode)
                .setMensagem(msgResposta)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void registrarComponente(RegistroRequest request, StreamObserver<StatusResponse> responseObserver) {
        String nome = request.getNomeComponente();
        String endereco = request.getEndereco();
        
        System.out.println("-> [gRPC] Pedido de registro recebido: " + nome + " em " + endereco);
        
        StatusResponse response = StatusResponse.newBuilder()
                .setStatusCode(200)
                .setMensagem("Registrado com sucesso via gRPC!")
                .build();
                
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}