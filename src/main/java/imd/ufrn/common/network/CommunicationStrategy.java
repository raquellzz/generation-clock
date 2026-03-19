package imd.ufrn.common.network;

public interface CommunicationStrategy {

    void startServer(int port, MessageListener listener);
    
    void sendMessage(String ip, int port, String message);
}
