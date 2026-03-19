package imd.ufrn.storage;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class StorageMain implements MessageListener{
    public static void main(String[] args) {
        System.out.println("=== Iniciando Storage (Componente B - Generation Clock) ===");
        
        String protocolo = "TCP";
        // int minhaPorta = 8082;
        // String protocolo = args[0]; 
        int minhaPorta = Integer.parseInt(args[0]);
        String meuId = args[1];
        
        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, "STORAGE" + meuId );
        
        rede.sendMessage("127.0.0.1", 8080, "REGISTRO;" + "STORAGE_" + meuId + ";127.0.0.1:" + minhaPorta);

        StorageMain meuStorage = new StorageMain();
        
        rede.startServer(minhaPorta, meuStorage);
    }

    @Override
    public void onMessageReceived(String message, String remetenteIp) {
        System.out.println("[STORAGE-NEGÓCIO] Recebi de " + remetenteIp + " o payload: " + message);
        
    }
}