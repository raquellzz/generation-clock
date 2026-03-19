package imd.ufrn.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenerationState {
    private int currentGeneration = 0;
    private boolean isLeader = false;
    private String leaderAdress = "";
    private final List<String> chatHistory = new ArrayList<>();
    
    private final Lock stateLock = new ReentrantLock();

    public void promoteToLeader(int newGeneration) {
        stateLock.lock();
        try {
            if(newGeneration <= this.currentGeneration) {
                System.out.println("Não posso me tornar líder! Geração " + newGeneration + " é menor ou igual à geração atual " + this.currentGeneration);
                return;
            }
            this.currentGeneration = newGeneration;
            this.isLeader = true;
            this.leaderAdress = "EU MESMO!";
            System.out.println("[GENERATION CLOCK] Fui promovido! Assumi a Liderança da Geração " + this.currentGeneration);
        } finally {
            stateLock.unlock();
        }
    }

    public void updateLeader(int newGeneration, String newLeaderAddress) {
        stateLock.lock();
        try {
            if(newGeneration < this.currentGeneration) {
                System.out.println("Não posso atualizar líder! Geração " + newGeneration + " é menor que a geração atual " + this.currentGeneration);
                return;
            }
            this.currentGeneration = newGeneration;
            this.isLeader = false;
            this.leaderAdress = newLeaderAddress;
            System.out.println("[GENERATION CLOCK] Relógio atualizado. Novo líder da Geração " + this.currentGeneration + " é " + newLeaderAddress);
        } finally {
            stateLock.unlock();
        }
    }

    public boolean processMessage(int messageGeneration, String payload) {
        stateLock.lock();
        try {
            if (messageGeneration < this.currentGeneration) {
                System.err.println("[SEGURANÇA] Mensagem rejeitada! Veio da Geração " 
                                   + messageGeneration + " mas já estamos na " + this.currentGeneration);
                return false; 
            }
            chatHistory.add(payload);
            System.out.println("[BANCO] Mensagem salva: " + payload + " | Total no histórico: " + chatHistory.size());
            return true;
        } finally {
            stateLock.unlock();
        }
    }

    public int getCurrentGeneration() {
        stateLock.lock();
        try {
            return currentGeneration;
        } finally {
            stateLock.unlock();
        }
    }

    public boolean isLeader() {
        stateLock.lock();
        try {
            return isLeader;
        } finally {
            stateLock.unlock();
        }
    }
}