package imd.ufrn.common.network;

public interface MessageListener {
    String onMessageReceived(String message, String remetenteIp);
}