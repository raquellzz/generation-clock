package imd.ufrn.common.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpStrategy implements CommunicationStrategy {
    
    private final String nomeComponente;
    private final int TAMANHO_BUFFER = 4096;

    public UdpStrategy(String nomeComponente) {
        this.nomeComponente = nomeComponente;
    }

    @Override
    public void startServer(int port, MessageListener listener) {
        System.out.println("[" + nomeComponente + "] Iniciando servidor UDP na porta " + port + "...");

        try (DatagramSocket serverSocket = new DatagramSocket(port);
             ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            System.out.println("[" + nomeComponente + "] Escutando ativamente por Datagramas! (UDP)");

            while (true) {
                byte[] bufferRecebimento = new byte[TAMANHO_BUFFER];
                DatagramPacket pacoteRecebido = new DatagramPacket(bufferRecebimento, bufferRecebimento.length);
                
                serverSocket.receive(pacoteRecebido);

                executor.execute(() -> handleClient(pacoteRecebido, listener, serverSocket));
            }
        } catch (IOException e) {
            System.err.println("[" + nomeComponente + "] Erro no servidor UDP: " + e.getMessage());
        }
    }

    private void handleClient(DatagramPacket pacote, MessageListener listener, DatagramSocket serverSocket) {
        try {
            String rawMessage = new String(pacote.getData(), 0, pacote.getLength());
            String ipRemetente = pacote.getAddress().getHostAddress();
            int portaRemetente = pacote.getPort(); 

            if (rawMessage.length() > 0 && listener != null) {
                String respostaHttp = listener.onMessageReceived(rawMessage, ipRemetente);

                if (respostaHttp != null && !respostaHttp.isEmpty()) {
                    byte[] bufferEnvio = respostaHttp.getBytes();
                    DatagramPacket pacoteResposta = new DatagramPacket(
                            bufferEnvio, bufferEnvio.length, pacote.getAddress(), portaRemetente);
                    serverSocket.send(pacoteResposta);
                }
            }
        } catch (Exception e) {
            System.err.println("[" + nomeComponente + "] Erro na Virtual Thread UDP: " + e.getMessage());
        }
    }


    @Override
    public void sendMessage(String ip, int port, String route, String payload) {
        try (DatagramSocket clienteSocket = new DatagramSocket()) {
            String httpMessage = "POST " + route + " HTTP/1.1\r\n" +
                                 "Host: " + ip + ":" + port + "\r\n" +
                                 "Content-Length: " + payload.length() + "\r\n" +
                                 "Content-Type: text/plain\r\n" +
                                 "\r\n" + 
                                 payload;

            byte[] bufferEnvio = httpMessage.getBytes();
            InetAddress enderecoDestino = InetAddress.getByName(ip);

            DatagramPacket pacoteEnvio = new DatagramPacket(
                    bufferEnvio, bufferEnvio.length, enderecoDestino, port);

            System.out.println("[" + nomeComponente + "] Disparando Datagrama UDP para " + route);
            
            clienteSocket.send(pacoteEnvio);
            
        } catch (IOException e) {
            System.err.println("[" + nomeComponente + "] Falha ao enviar datagrama UDP para " + ip + ":" + port);
        }
    }
}