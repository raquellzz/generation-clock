package imd.ufrn.validator;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class ValidatorMain implements MessageListener{
    private final CommunicationStrategy rede;

    public ValidatorMain(CommunicationStrategy rede) {
        this.rede = rede;
    }
    public static void main(String[] args) {
        System.out.println("=== Iniciando Validator (Componente A) ===");
        
        String protocolo = "UDP";
        // int minhaPorta = 8081;
        // String protocolo = args[0]; 
        
        int minhaPorta = Integer.parseInt(args[0]);
        String meuIdSuffix = args[1];
        String meuId = "VALIDATOR_" + meuIdSuffix;

        System.out.println("=== Iniciando " + meuId + " ===");
        
        CommunicationStrategy rede = CommunicationFactory.createStrategy("UDP", meuId);
        
        // 3. Injeta a rede no Validator
        ValidatorMain meuValidator = new ValidatorMain(rede); 
        
        rede.sendMessage("127.0.0.1", 8080, "/registro", meuId + ";127.0.0.1:" + minhaPorta);
        rede.startServer(minhaPorta, meuValidator);
    }

    @Override
    public String onMessageReceived(String rawMessage, String remetenteIp) {
        imd.ufrn.common.protocol.HttpParser parser = new imd.ufrn.common.protocol.HttpParser(rawMessage);
        
        String metodo = parser.getMethod();
        String rota = parser.getRoute();
        String corpo = parser.getBody();

        if (metodo == null || rota == null) {
            return "HTTP/1.1 400 Bad Request\r\n\r\n";
        }
        
        System.out.println("[VALIDATOR] Recebeu " + metodo + " " + rota + " de " + remetenteIp);
        
        if (rota.equals("/validar")) {
            System.out.println("-> Iniciando validação do payload: [" + corpo + "]");
            
            if (corpo == null || corpo.trim().isEmpty()) {
                System.out.println("-> ❌ REJEITADA: A mensagem está vazia.");
            } else if (corpo.toLowerCase().contains("palavrao")) {
                System.out.println("-> ❌ REJEITADA: A mensagem contém conteúdo impróprio.");
            } else {
                System.out.println("-> ✅ APROVADA: A mensagem é válida e limpa!");
                
                System.out.println("-> Devolvendo mensagem aprovada para o Gateway...");
                
                // imd.ufrn.common.network.CommunicationStrategy redeRetorno = 
                //     imd.ufrn.common.network.CommunicationFactory.createStrategy("TCP", "VALIDATOR_RETORNO");
                
                rede.sendMessage("127.0.0.1", 8080, "/salvar_chat", corpo);
            }
        }
        return "HTTP/1.1 200 OK\r\n\r\nOK";
    }
}