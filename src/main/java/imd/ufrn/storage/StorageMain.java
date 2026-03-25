package imd.ufrn.storage;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;
import imd.ufrn.common.protocol.HttpParser;

public class StorageMain implements MessageListener{
    private String meuId;
    private GenerationState generationState;
    public StorageMain(String meuId) {
        this.meuId = meuId;
        this.generationState = new GenerationState();
    }
    public static void main(String[] args) {
        System.out.println("=== Iniciando Storage (Componente B - Generation Clock) ===");
        
        String protocolo = "TCP";
        // int minhaPorta = 8082;
        // String protocolo = args[0]; 
        
        int minhaPorta = Integer.parseInt(args[0]);
        String meuIdSuffix = args[1];
        String meuId = "STORAGE_" + meuIdSuffix;

        System.out.println("=== Iniciando " + meuId + " ===");
        
        StorageMain meuStorage = new StorageMain(meuId); 
        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, meuId);
        
        // MUDANÇA AQUI: Enviando via rota "/registro"
        rede.sendMessage("127.0.0.1", 8080, "/registro", meuId + ";127.0.0.1:" + minhaPorta);
        
        rede.startServer(minhaPorta, meuStorage);
    }

    @Override
    public String onMessageReceived(String rawMessage, String remetenteIp) {
        HttpParser parser = new HttpParser(rawMessage);
        String rota = parser.getRoute();
        String corpo = parser.getBody();
        
        if (rota == null) return "HTTP/1.1 400 Bad Request\r\n\r\n";

        // Agora roteamos baseados na URL do HTTP!
        if (rota.equals("/eleicao")) {
            int novaGeracao = Integer.parseInt(corpo);
            generationState.promoteToLeader(novaGeracao);
        } 
        else if (rota.equals("/salvar")) {
            String[] partes = corpo.split(";", 2);
            int geracaoDaMensagem = Integer.parseInt(partes[0]);
            String payload = partes[1];
            
            generationState.processMessage(geracaoDaMensagem, payload);
        }
        return "HTTP/1.1 200 OK\r\n\r\nMensagem processada";
    }
}