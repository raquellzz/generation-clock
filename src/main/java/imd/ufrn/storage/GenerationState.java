package imd.ufrn.storage;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class GenerationState {
    private int generation = 0;
    private boolean isLeader = false;
    
    private final Lock stateLock = new ReentrantLock();

    public void promoteToLeader(int newGeneration) {
        stateLock.lock();
        try {
            this.generation = newGeneration;
            this.isLeader = true;
            System.out.println("Assumi a liderança! Geração atual: " + this.generation);
        } finally {
            stateLock.unlock();
        }
    }

    public boolean processMessage(int messageGeneration, String payload) {
        stateLock.lock();
        try {
            if (messageGeneration < this.generation) {
                System.out.println("Mensagem rejeitada! Geração obsoleta.");
                return false; 
            }
            return true;
        } finally {
            stateLock.unlock();
        }
    }
}