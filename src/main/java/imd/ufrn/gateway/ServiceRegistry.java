package imd.ufrn.gateway;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceRegistry {
    private static ServiceRegistry instance;
    
    private final Map<String, String> activeComponents;
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ServiceRegistry() {
        this.activeComponents = new HashMap<>();
    }

    public static synchronized ServiceRegistry getInstance() {
        if (instance == null) {
            instance = new ServiceRegistry();
        }
        return instance;
    }

    public void registerComponent(String componentId, String address) {
        lock.writeLock().lock();
        try {
            activeComponents.put(componentId, address);
            System.out.println("[REGISTRY] Componente " + componentId + " salvo com sucesso no endereço " + address);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeComponent(String componentId) {
        lock.writeLock().lock();
        try {
            activeComponents.remove(componentId);
            System.out.println("[REGISTRY] Componente " + componentId + " removido da tabela.");
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Map<String, String> getActiveComponents() {
        lock.readLock().lock();
        try {
            return new HashMap<>(activeComponents); 
        } finally {
            lock.readLock().unlock();
        }
    }
}