package imd.ufrn.common.network;

import imd.ufrn.grpc.DistribuidoServiceGrpc;
import imd.ufrn.grpc.MensagemRequest;
import imd.ufrn.grpc.RegistroRequest;
import imd.ufrn.grpc.StatusResponse;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcStrategy implements CommunicationStrategy {
    private final String nomeComponente;

    public GrpcStrategy(String nomeComponente) {
        this.nomeComponente = nomeComponente;
    }

    @Override
    public void startServer(int port, MessageListener listener) {
        try {
            Server server = ServerBuilder.forPort(port)
                    .addService(new DistribuidoServiceGrpc.DistribuidoServiceImplBase() {
                        
                        @Override
                        public void processarMensagem(MensagemRequest request, StreamObserver<StatusResponse> responseObserver) {
                            String rota = request.getRota();
                            String corpo = request.getPayload();
                            
                            String fakeHttpRequest = "POST " + rota + " HTTP/1.1\r\n\r\n" + corpo;

                            String respostaHttp = listener.onMessageReceived(fakeHttpRequest, "127.0.0.1");

                            int statusCodeReal = 200;
                            if (respostaHttp.contains("503")) {
                                statusCodeReal = 503;
                            } else if (respostaHttp.contains("400")) {
                                statusCodeReal = 400;
                            } else if (respostaHttp.contains("403")) {
                                statusCodeReal = 403;
                            } else if (respostaHttp.contains("404")) {
                                statusCodeReal = 404;
                            }

                            StatusResponse response = StatusResponse.newBuilder()
                                    .setStatusCode(statusCodeReal)
                                    .setMensagem(respostaHttp)
                                    .build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        }

                        @Override
                        public void registrarComponente(RegistroRequest request, StreamObserver<StatusResponse> responseObserver) {
                            String fakeHttpRequest = "POST /registro HTTP/1.1\r\n\r\n" + request.getNomeComponente() + ";" + request.getEndereco();
                            listener.onMessageReceived(fakeHttpRequest, "127.0.0.1");
                            
                            StatusResponse response = StatusResponse.newBuilder().setStatusCode(200).setMensagem("Registrado").build();
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        }
                    })
                    .build()
                    .start();

            System.out.println("[" + nomeComponente + "] Escutando ativamente via gRPC na porta " + port);
            server.awaitTermination();
            
        } catch (Exception e) {
            System.err.println("[" + nomeComponente + "] Erro ao iniciar servidor gRPC: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(String ip, int port, String route, String message) {
        Thread.ofVirtual().start(() -> {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(ip, port).usePlaintext().build();
            try {
                DistribuidoServiceGrpc.DistribuidoServiceBlockingStub stub = DistribuidoServiceGrpc.newBlockingStub(channel);
                if (route.equals("/registro")) {
                    String[] partes = message.split(";");
                    RegistroRequest req = RegistroRequest.newBuilder()
                            .setNomeComponente(partes[0])
                            .setEndereco(partes[1])
                            .build();
                    stub.registrarComponente(req);
                } else {
                    MensagemRequest req = MensagemRequest.newBuilder()
                            .setRota(route)
                            .setPayload(message)
                            .build();
                    stub.processarMensagem(req);
                }
            } catch (Exception e) {
            } finally {
                channel.shutdown();
            }
        });
    }
}