package imd.ufrn.gateway;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class HeartbeatMonitor extends Thread {

    private final ServiceRegistry registry;
    private final int intervaloMillis;

    public HeartbeatMonitor(ServiceRegistry registry, int intervaloMillis) {
        this.registry = registry;
        this.intervaloMillis = intervaloMillis;
    }

    @Override
    public void run() {
        System.out.println("[HEARTBEAT] Monitoramento iniciado. Checando a cada " + (intervaloMillis/1000) + " segundos.");

        while (true) {
            try {
                Thread.sleep(intervaloMillis);

                Map<String, String> componentesVivos = registry.getActiveComponents();

                if (componentesVivos.isEmpty()) {
                    continue; 
                }

                System.out.println("\n[HEARTBEAT] Iniciando varredura em " + componentesVivos.size() + " componentes...");

                for (Map.Entry<String, String> entry : componentesVivos.entrySet()) {
                    String idComponente = entry.getKey();
                    String endereco = entry.getValue();
                    
                    String[] partesIpPorta = endereco.split(":");
                    String ip = partesIpPorta[0];
                    int porta = Integer.parseInt(partesIpPorta[1]);

                    boolean isVivo = tentarConectar(ip, porta);

                    if (!isVivo) {
                        System.out.println("[HEARTBEAT] ATENÇÃO! " + idComponente + " não respondeu. Removendo da tabela.");
                        registry.removeComponent(idComponente);
                    } else {
                        System.out.println("[HEARTBEAT] " + idComponente + " está VIVO.");
                    }
                }

            } catch (InterruptedException e) {
                System.err.println("[HEARTBEAT] Thread interrompida!");
                break;
            }
        }
    }

    private boolean tentarConectar(String ip, int porta) {
        try (Socket socket = new Socket(ip, porta)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}