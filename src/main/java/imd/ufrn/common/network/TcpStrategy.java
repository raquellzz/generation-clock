package imd.ufrn.common.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TcpStrategy implements CommunicationStrategy {
    private final String nomeComponente;

    public TcpStrategy(String nomeComponente) {
        this.nomeComponente = nomeComponente;
    }

    @Override
    public void startServer(int port, MessageListener listener) {
        System.out.println("[" + nomeComponente + "] Iniciando servidor TCP na porta " + port + "...");

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
             ServerSocket server = new ServerSocket(port)) {
            
            System.out.println("[" + nomeComponente + "] Escutando ativamente!.");

            while (true) {
                Socket conexao = server.accept();
                
                executor.execute(() -> handleClient(conexao, listener));
            }
        } catch (IOException e) {
            System.err.println("[" + nomeComponente + "] Erro no servidor: " + e.getMessage());
        }
    }

    @Override
    public void sendMessage(String ip, int port, String message) {
        try (Socket socket = new Socket(ip, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            
            System.out.println("[" + nomeComponente + "] Enviando pacote TCP para " + ip + ":" + port + " -> " + message);
            output.println(message);
            
        } catch (IOException e) {
            System.err.println("[" + nomeComponente + "] Falha ao enviar mensagem para " + ip + ":" + port);
        }
    }

    private void handleClient(Socket conexao, MessageListener listener) {
        try (conexao;
            BufferedReader input = new BufferedReader(new InputStreamReader(conexao.getInputStream()))) {
             
            String msg = input.readLine();
            String ip = conexao.getInetAddress().getHostAddress();

            if (msg != null && listener != null) {
                listener.onMessageReceived(msg, ip);
            }

        } catch (IOException e) {
            System.err.println("Erro na Virtual Thread: " + e.getMessage());
        }
    }
}