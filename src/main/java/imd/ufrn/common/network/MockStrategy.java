package imd.ufrn.common.network;

public class MockStrategy implements CommunicationStrategy {
    private final String nomeComponente;

    public MockStrategy(String nomeComponente) {
        this.nomeComponente = nomeComponente;
    }

    @Override
    public void startServer(int port, MessageListener listener) {
        System.out.println("[" + nomeComponente + "] Servidor MOCK iniciado e escutando na porta " + port + "...");
        System.out.println("[" + nomeComponente + "] (Fingindo que estou esperando conexões infinitamente...)\n");
        
    }

    @Override
    public void sendMessage(String ip, int port, String message) {
        System.out.println("[" + nomeComponente + "] Enviando MOCK para " + ip + ":" + port + " -> " + message);
        
    }
}