package imd.ufrn.validator;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class ValidatorMain implements MessageListener{
    public static void main(String[] args) {
        System.out.println("=== Iniciando Validator (Componente A) ===");
        
        String protocolo = "TCP";
        // int minhaPorta = 8081;
        // String protocolo = args[0]; 
        int minhaPorta = Integer.parseInt(args[0]);
        String meuId = args[1];
        
        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, "VALIDATOR" + meuId );
        
        rede.sendMessage("127.0.0.1", 8080, "REGISTRO;" + "VALIDATOR_" + meuId + ";127.0.0.1:" + minhaPorta);

        ValidatorMain meuValidator = new ValidatorMain();
        
        rede.startServer(minhaPorta, meuValidator);
    }

    @Override
    public void onMessageReceived(String message, String remetenteIp) {
        System.out.println("[VALIDATOR-NEGÓCIO] Recebi de " + remetenteIp + " o payload: " + message);
        
    }
}