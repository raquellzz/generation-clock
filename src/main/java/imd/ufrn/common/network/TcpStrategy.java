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
    public void sendMessage(String ip, int port, String route, String payload) {
        try (Socket socket = new Socket(ip, port);
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true)) {
            
            // Construindo o envelope HTTP "na unha"
            String httpMessage = "POST " + route + " HTTP/1.1\r\n" +
                                 "Host: " + ip + ":" + port + "\r\n" +
                                 "Content-Length: " + payload.length() + "\r\n" +
                                 "Content-Type: text/plain\r\n" +
                                 "\r\n" + 
                                 payload;

            System.out.println("[" + nomeComponente + "] Enviando requisição para " + route);
            
            output.print(httpMessage);
            output.flush();
            
        } catch (IOException e) {
            System.err.println("[" + nomeComponente + "] Falha ao enviar mensagem para " + ip + ":" + port);
        }
    }

    private void handleClient(Socket conexao, MessageListener listener) {
        try (conexao;
             BufferedReader input = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
             PrintWriter output = new PrintWriter(conexao.getOutputStream(), true)) {

            String ip = conexao.getInetAddress().getHostAddress();
            StringBuilder rawHttp = new StringBuilder();
            String linha;
            int tamanhoCorpo = 0;

            while ((linha = input.readLine()) != null && !linha.isEmpty()) {
                rawHttp.append(linha).append("\r\n");
                
                if (linha.toLowerCase().startsWith("content-length:")) {
                    tamanhoCorpo = Integer.parseInt(linha.substring(15).trim());
                }
            }
            rawHttp.append("\r\n");

            if (tamanhoCorpo > 0) {
                char[] bufferCorpo = new char[tamanhoCorpo];
                int charsLidos = input.read(bufferCorpo, 0, tamanhoCorpo);
                
                if (charsLidos > 0) {
                    rawHttp.append(new String(bufferCorpo, 0, charsLidos));
                }
            }

            if (rawHttp.length() > 0 && listener != null) {
                String respostaHttp = listener.onMessageReceived(rawHttp.toString(), ip);
                
                if (respostaHttp != null && !respostaHttp.isEmpty()) {
                    output.print(respostaHttp);
                } else {
                    output.print("HTTP/1.1 200 OK\r\n\r\n");
                }
                output.flush();
            }

        } catch (IOException e) {
            System.err.println("Erro processando cliente: " + e.getMessage());
        }
    }
}