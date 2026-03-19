src/main/java/imd/ufrn/
│
├── common/                              # 🛠️ O "Coração" Compartilhado
│   ├── network/
│   │   ├── CommunicationStrategy.java   # A interface base (Padrão GoF Strategy)
│   │   ├── CommunicationFactory.java    # O criador de estratégias (Padrão GoF Factory)
│   │   ├── TcpStrategyVirtual.java      # Implementação TCP usando Virtual Threads
│   │   ├── UdpStrategyVirtual.java      # Implementação UDP (baseada no cód do professor)
│   │   ├── HttpStrategy.java            # (Para implementar depois)
│   │   └── GrpcStrategy.java            # (Para implementar depois)
│   │
│   └── protocol/
│       └── PayloadParser.java           # Classe utilitária para dar .split(";") nas strings
│
├── gateway/                             # 🚦 Aplicação 1: O API Gateway
│   ├── GatewayMain.java                 # Ponto de entrada (Lê a porta/protocolo e inicia a Strategy)
│   ├── ServiceRegistry.java             # A Tabela de Roteamento (Padrão GoF Singleton + ReentrantReadWriteLock)
│   ├── HeartbeatMonitor.java            # Thread em loop que varre o Registry e manda "PING" para os IPs
│   └── MessageRouter.java               # Recebe do JMeter, olha o Registry e decide para qual 'validator' mandar
│
├── validator/                           # 🛡️ Aplicação 2: Componente A (Stateless)
│   ├── ValidatorMain.java               # Inicia, se registra no Gateway e fica escutando
│   └── ChatLogicProcessor.java          # A regra de negócio que valida a mensagem
│
└── storage/                             # 💾 Aplicação 3: Componente B (Stateful - Generation Clock)
    ├── StorageMain.java                 # Inicia, se registra no Gateway e fica escutando
    ├── GenerationState.java             # O estado atual (Geração, Líder) usando ReentrantLock
    └── ChatDatabase.java                # Onde as mensagens validadas são efetivamente salvas em memória/arquivo