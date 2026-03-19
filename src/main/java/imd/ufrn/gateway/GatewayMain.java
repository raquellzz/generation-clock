package imd.ufrn.gateway;

import java.util.Map;

import imd.ufrn.common.network.CommunicationFactory;
import imd.ufrn.common.network.CommunicationStrategy;
import imd.ufrn.common.network.MessageListener;

public class GatewayMain implements MessageListener{
    public static void main(String[] args) {
        // args[0] = "UDP", args[1] = "8080"
        
        // String protocolo = args[0]; 
        // int porta = Integer.parseInt(args[1]);

        String protocolo = "TCP"; 
        int portaGateway = 8080;

        CommunicationStrategy rede = CommunicationFactory.createStrategy(protocolo, "API_GATEWAY");

        GatewayMain meuGateway = new GatewayMain();
        
        HeartbeatMonitor monitor = new HeartbeatMonitor(ServiceRegistry.getInstance(), 5000);
        monitor.start();
        
        rede.startServer(portaGateway, meuGateway);
        
    }

    @Override
    public void onMessageReceived(String message, String remetenteIp) {
        System.out.println("[GATEWAY-NEGÓCIO] Mensagem chegou de " + remetenteIp + ": " + message);
        
        String[] partes = message.split(";");
        String tipo = partes[0];
        
        if (tipo.equals("REGISTRO")) {
            String nomeComp = partes[1];
            String endereco = partes[2];

            ServiceRegistry.getInstance().registerComponent(nomeComp, endereco);
            
            Map<String, String> vivos = ServiceRegistry.getInstance().getActiveComponents();
            System.out.println("-> Total de componentes registrados agora: " + vivos.size());
        }
    }
}