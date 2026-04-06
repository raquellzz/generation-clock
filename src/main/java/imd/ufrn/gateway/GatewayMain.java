package imd.ufrn.gateway;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class GatewayMain implements MessageListener{
    private final CommunicationStrategy rede;
    private final AtomicInteger currentGeneration = new AtomicInteger(0);
    private volatile String currentLeader = null;

    public GatewayMain(CommunicationStrategy rede) {
        this.rede = rede;
    }
    public static void main(String[] args) {
        // args[0] = "UDP", args[1] = "8080"
        
        // String protocolo = args[0]; 
        // int porta = Integer.parseInt(args[1]);

        String protocolo = "UDP"; 
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
        boolean bancoDisponivel = false;

        for (Map.Entry<String, String> entry : ativos.entrySet()) {
            if (entry.getKey().startsWith("VALIDATOR")) {
                enderecosValidators.add(entry.getValue());
            } else if (entry.getKey().startsWith("STORAGE")) {
                bancoDisponivel = true;
            }
        }

        if (!bancoDisponivel) {
            System.err.println("[ERRO 503] Serviço Indisponível! Nenhum Storage (Banco) vivo no momento.");
            return "HTTP/1.1 503 Service Unavailable\r\n\r\n";
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
        List<String> storagesVivos = new ArrayList<>();

        for (Map.Entry<String, String> entry : ativos.entrySet()) {
            if (entry.getKey().startsWith("STORAGE")) {
                storagesVivos.add(entry.getValue());
            }
        }

        if (storagesVivos.isEmpty()) {
            System.err.println("[ERRO 503] Nenhum Storage (Banco) vivo no momento para salvar a mensagem.");
            return "HTTP/1.1 503 Service Unavailable\r\n\r\n";
        }

        // String[] partesIpPorta = enderecoStorage.split(":");
        // String ip = partesIpPorta[0];
        // int porta = Integer.parseInt(partesIpPorta[1]);

        // System.out.println("[ROTEADOR] Encaminhando para o Banco em " + enderecoStorage);

        String lider = getOuElegerLider(storagesVivos);

        String[] partesIpPorta = lider.split(":");
        System.out.println("[ROTEADOR] Salvando no LÍDER [" + lider + "] | Geração: " + currentGeneration.get());

        String payloadComGeracao = currentGeneration.get() + ";" + payload;
        
        rede.sendMessage(partesIpPorta[0], Integer.parseInt(partesIpPorta[1]), "/salvar", payloadComGeracao);
        return "HTTP/1.1 200 OK\r\n\r\nEnviado ao Banco";
        // rede.sendMessage(ip, porta, "/salvar", payloadComGeracao);
        // return "HTTP/1.1 200 OK\r\n\r\nEnviado ao Banco";
    }

    private synchronized String getOuElegerLider(List<String> storagesVivos) {
        // Se o líder atual caiu, ou se o sistema acabou de ligar e não tem líder:
        if (currentLeader == null || !storagesVivos.contains(currentLeader)) {
            
            // 1. O Relógio Avança!
            currentGeneration.incrementAndGet();
            
            // 2. Elege o novo líder (Ordenamos para ser determinístico, sempre pega o primeiro da lista)
            Collections.sort(storagesVivos);
            currentLeader = storagesVivos.get(0);

            System.out.println("\n[CLOCK] ⏰ O Relógio Avançou! Nova Geração: " + currentGeneration.get());
            System.out.println("[ELEIÇÃO] Novo Banco Líder promovido: " + currentLeader + "\n");

            // 3. Avisa o Storage eleito de que agora ele manda na geração atual
            String[] partes = currentLeader.split(":");
            rede.sendMessage(partes[0], Integer.parseInt(partes[1]), "/eleicao", String.valueOf(currentGeneration.get()));
        }
        
        return currentLeader;
    }
}