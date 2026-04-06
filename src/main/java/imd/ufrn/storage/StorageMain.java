package imd.ufrn.storage;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class StorageMain implements MessageListener{
    private String meuId;
    private final GenerationState generationState;
    public StorageMain(String meuId) {
        this.meuId = meuId;
        this.generationState = new GenerationState();
    }
    public static void main(String[] args) {
        System.out.println("=== Iniciando Storage (Componente B - Generation Clock) ===");
        
        String protocolo = args[0];
        int minhaPorta = Integer.parseInt(args[1]);
        String meuIdSuffix = args[2];

        String meuId = "STORAGE_" + meuIdSuffix;

        System.out.println("=== Iniciando " + meuId + " ===");
        
        StorageMain meuStorage = new StorageMain(meuId); 
        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, meuId);
        
        rede.sendMessage("127.0.0.1", 8080, "/registro", meuId + ";127.0.0.1:" + minhaPorta);
        
        rede.startServer(minhaPorta, meuStorage);
    }

    @Override
    public String onMessageReceived(String rawMessage, String remetenteIp) {
        imd.ufrn.common.protocol.HttpParser parser = new imd.ufrn.common.protocol.HttpParser(rawMessage);
        
        String metodo = parser.getMethod();
        String rota = parser.getRoute();
        String corpo = parser.getBody();
        
        if (rota == null) return "HTTP/1.1 400 Bad Request\r\n\r\n";

        if (rota.equals("/ping")) {
            System.out.println("[STORAGE] Recebeu GET /ping de " + remetenteIp);
            return "HTTP/1.1 200 OK\r\n\r\nPONG";
        }

        if (rota.equals("/eleicao")) {
            int novaGeracao = Integer.parseInt(corpo);
            generationState.promoteToLeader(novaGeracao);
            System.out.println("[STORAGE] Eleição realizada. Nova geração: " + novaGeracao);
            return "HTTP/1.1 200 OK\r\n\r\nEleição processada";
        } 
        else if (rota.equals("/salvar")) {
            String[] partes = corpo.split(";", 2);
            int geracaoDaMensagem = Integer.parseInt(partes[0]);
            String payload = partes[1];
            
            generationState.processMessage(geracaoDaMensagem, payload);
            System.out.println("[STORAGE] Mensagem salva: " + payload + " (Geração: " + geracaoDaMensagem + ")");
            return "HTTP/1.1 200 OK\r\n\r\nMensagem salva";
        }
        return "HTTP/1.1 200 OK\r\n\r\nMensagem processada";
    }
}