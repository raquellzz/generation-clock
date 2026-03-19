package imd.ufrn.storage;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

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
        String meuId = args[1];

        StorageMain meuStorage = new StorageMain(meuId);
        
        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, "STORAGE" + meuId );
        
        rede.sendMessage("127.0.0.1", 8080, "REGISTRO;" + "STORAGE_" + meuId + ";127.0.0.1:" + minhaPorta);
        rede.startServer(minhaPorta, meuStorage);
    }

    @Override
    public void onMessageReceived(String message, String remetenteIp) {
        // System.out.println("[STORAGE-NEGÓCIO] Recebi de " + remetenteIp + " o payload: " + message);
        String[] partes = message.split(";");
        String comando = partes[0];

        switch (comando) {
            case "ELEICAO_VENCEU" -> {
                int novaGeracao = Integer.parseInt(partes[1]);
                generationState.promoteToLeader(novaGeracao);
                System.out.println("[STORAGE-NEGÓCIO] Recebi a notícia de que a eleição foi vencida! Nova geração é " + novaGeracao);

            }
            case "NOVO_LIDER" -> {
                int novaGeracao = Integer.parseInt(partes[1]);
                String enderecoLider = partes[1];
                generationState.updateLeader(novaGeracao, enderecoLider);
                System.out.println("[STORAGE-NEGÓCIO] Recebi a notícia de que um novo líder foi eleito! Endereço do novo líder: " + enderecoLider);
            }
            case "SALVAR_MENSAGEM" -> {
                int geracaoMensagem = Integer.parseInt(partes[1]);
                String payload = partes[2];
                generationState.processMessage(geracaoMensagem, payload);
                System.out.println("[STORAGE-NEGÓCIO] Recebi uma mensagem para salvar! Geração da mensagem: " + geracaoMensagem + " | Payload: " + payload);
            }
            default -> {
            }
        }
    }
}