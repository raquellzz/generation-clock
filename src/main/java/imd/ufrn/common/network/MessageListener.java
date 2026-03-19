package imd.ufrn.common.network;

public interface MessageListener {
    void onMessageReceived(String message, String remetenteIp);
}