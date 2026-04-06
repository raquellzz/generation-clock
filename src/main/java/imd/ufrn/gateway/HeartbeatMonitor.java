package imd.ufrn.gateway;

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

    // private boolean tentarConectar(String ip, int porta) {
    //     try (Socket socket = new Socket(ip, porta)) {
    //         return true;
    //     } catch (IOException e) {
    //         return false;
    //     }
    // }

    // Método híbrido para checar a saúde do componente
    private boolean tentarConectar(String ip, int porta) {
        // 1. TENTA TCP PRIMEIRO
        try (java.net.Socket s = new java.net.Socket(ip, porta)) {
            return true; // Se conectou no Socket, está rodando em TCP e está vivo!
        } catch (Exception e) {
            // 2. SE TCP FALHOU, TENTA UDP!
            try (java.net.DatagramSocket ds = new java.net.DatagramSocket()) {
                // Define tempo máximo de espera para 1 segundo (senão o monitor trava)
                ds.setSoTimeout(1000); 
                
                // Monta uma requisição HTTP HTTP/1.1 verdadeira
                String pingMessage = "GET /ping HTTP/1.1\r\nHost: " + ip + "\r\n\r\n";
                byte[] bufferEnvio = pingMessage.getBytes();
                java.net.DatagramPacket pacoteEnvio = new java.net.DatagramPacket(
                        bufferEnvio, bufferEnvio.length, java.net.InetAddress.getByName(ip), porta);
                
                ds.send(pacoteEnvio); // Atira o ping

                // Fica esperando a resposta (o 200 OK)
                byte[] bufferRecebimento = new byte[1024];
                java.net.DatagramPacket pacoteRecebido = new java.net.DatagramPacket(bufferRecebimento, bufferRecebimento.length);
                ds.receive(pacoteRecebido); 
                
                return true; // Se chegou aqui sem dar erro de Timeout, o componente respondeu!
            } catch (Exception ex) {
                return false; // Se deu timeout no UDP também, o componente realmente morreu.
            }
        }
    }
}