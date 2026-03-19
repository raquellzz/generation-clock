package imd.ufrn;

import java.io.PrintWriter;
import java.net.Socket;

public class SimuladorZumbi {
    public static void main(String[] args) throws Exception {
        String ipStorage = "127.0.0.1";
        int portaStorage = 8082;

        System.out.println("=== Iniciando Ataque Zumbi ao Generation Clock ===\n");
        
        enviar(ipStorage, portaStorage, "ELEICAO_VENCEU;1");
        Thread.sleep(500);
        
        enviar(ipStorage, portaStorage, "SALVAR_MENSAGEM;1;Ola, sou uma mensagem legitima da geracao 1!");
        Thread.sleep(500);
        
        System.out.println("[SIMULADOR] Disparando mensagem atrasada da Geração 0...");
        enviar(ipStorage, portaStorage, "SALVAR_MENSAGEM;0;Sou um zumbi de uma geracao morta!");
        Thread.sleep(1000);

        System.out.println("[SIMULADOR] Simulando queda e nova eleição...");
        enviar(ipStorage, portaStorage, "ELEICAO_VENCEU;2");
        Thread.sleep(500);

        System.out.println("[SIMULADOR] Disparando mensagem atrasada da Geração 1...");
        enviar(ipStorage, portaStorage, "SALVAR_MENSAGEM;1;Lider antigo mandou salvar isso agora!");
        Thread.sleep(500);
        
        enviar(ipStorage, portaStorage, "SALVAR_MENSAGEM;2;Mensagem legitima da geracao 2. Banco seguro.");
        
        System.out.println("\n=== Fim da Simulação ===");
    }

    private static void enviar(String ip, int porta, String msg) {
        try (Socket s = new Socket(ip, porta);
             PrintWriter out = new PrintWriter(s.getOutputStream(), true)) {
            out.println(msg);
            System.out.println("-> Disparou: " + msg);
        } catch (Exception e) {
            System.err.println("Erro ao conectar no Storage (Ele está rodando na porta " + porta + "?)");
        }
    }
}