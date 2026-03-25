package imd.ufrn.gateway;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class GatewayMain implements MessageListener{
    private final CommunicationStrategy rede;

    public GatewayMain(CommunicationStrategy rede) {
        this.rede = rede;
    }
    public static void main(String[] args) {
        // args[0] = "UDP", args[1] = "8080"
        
        // String protocolo = args[0]; 
        // int porta = Integer.parseInt(args[1]);

        String protocolo = "TCP"; 
        int portaGateway = 8080;

        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, "API_GATEWAY");

        GatewayMain meuGateway = new GatewayMain(rede);
        
        HeartbeatMonitor monitor = new HeartbeatMonitor(ServiceRegistry.getInstance(), 5000);
        monitor.start();
        
        rede.startServer(portaGateway, meuGateway);
        
    }

    @Override
    public String onMessageReceived(String rawMessage, String remetenteIp) {
        imd.ufrn.common.protocol.HttpParser parser = new imd.ufrn.common.protocol.HttpParser(rawMessage);
        
        String metodo = parser.getMethod();
        String rota = parser.getRoute();
        String corpo = parser.getBody();

        if(rota == null) {
            return "HTTP/1.1 400 Bad Request\r\n\r\n";
        }

        System.out.println("[GATEWAY-HTTP] Recebeu " + metodo + " " + rota + " de " + remetenteIp);
        
        switch (rota) {
            case "/registro" -> {
                String[] partes = corpo.split(";");
                if (partes.length == 2) {
                    String nomeComp = partes[0];
                    String endereco = partes[1];
                    
                    ServiceRegistry.getInstance().registerComponent(nomeComp, endereco);
                    // System.out.println("-> " + nomeComp + " registrado via HTTP!");
                }
                return "HTTP/1.1 200 OK\r\n\r\nRegistrado";
            }
            case "/chat" -> {
                System.out.println("\n[GATEWAY] Requisição de Chat chegou do JMeter! Payload: " + corpo);
                return encaminharParaValidator(corpo);
            }
            case "/salvar_chat" -> {
                System.out.println("\n[GATEWAY] Recebeu mensagem APROVADA do Validator. Encaminhando para o Banco...");
                return encaminharParaStorage(corpo);
            }
            default -> {
                return "HTTP/1.1 404 Not Found\r\n\r\nRota inexistente";
            }
        }
    }

    private String encaminharParaValidator(String payload) {
        Map<String, String> ativos = ServiceRegistry.getInstance().getActiveComponents();
        List<String> enderecosValidators = new ArrayList<>();

        for (Map.Entry<String, String> entry : ativos.entrySet()) {
            if (entry.getKey().startsWith("VALIDATOR")) {
                enderecosValidators.add(entry.getValue());
            }
        }

        if (enderecosValidators.isEmpty()) {
            System.err.println("[ERRO 503] Serviço Indisponível! Nenhum Validator vivo no momento.");
            return "HTTP/1.1 503 Service Unavailable\r\n\r\n";
        }

        int indexSorteado = new Random().nextInt(enderecosValidators.size());
        String escolhido = enderecosValidators.get(indexSorteado);
        
        String[] partesIpPorta = escolhido.split(":");
        String ip = partesIpPorta[0];
        int porta = Integer.parseInt(partesIpPorta[1]);

        System.out.println("[ROTEADOR] Encaminhando mensagem para o Validator em " + escolhido);
        
        rede.sendMessage(ip, porta, "/validar", payload);
        return "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\nMensagem recebida e em processamento";
    }


    private String encaminharParaStorage(String payload) {
        Map<String, String> ativos = ServiceRegistry.getInstance().getActiveComponents();
        String enderecoStorage = null;

        for (Map.Entry<String, String> entry : ativos.entrySet()) {
            if (entry.getKey().startsWith("STORAGE")) {
                enderecoStorage = entry.getValue();
                break;
            }
        }

        if (enderecoStorage == null) {
            System.err.println("[ERRO 503] Serviço Indisponível! Nenhum Storage (Banco) vivo no momento.");
            return "HTTP/1.1 503 Service Unavailable\r\n\r\n";
        }

        String[] partesIpPorta = enderecoStorage.split(":");
        String ip = partesIpPorta[0];
        int porta = Integer.parseInt(partesIpPorta[1]);

        System.out.println("[ROTEADOR] Encaminhando para o Banco em " + enderecoStorage);
        
        // Geração de clock mockado por enquanto
        String payloadComGeracao = "1;" + payload; 
        
        rede.sendMessage(ip, porta, "/salvar", payloadComGeracao);
        return "HTTP/1.1 200 OK\r\n\r\nEnviado ao Banco";
    }
}