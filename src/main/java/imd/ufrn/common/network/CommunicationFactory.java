package imd.ufrn.common.network;

public class CommunicationFactory {

    public static CommunicationStrategy createStrategy(String protocolType, String nomeComponente) {
        if (protocolType == null) {
            throw new IllegalArgumentException("Protocolo não informado.");
        }

        switch (protocolType.toUpperCase()) {
            case "UDP":
                return new UdpStrategy(nomeComponente); 
            case "TCP":
                return new TcpStrategy(nomeComponente);
            case "GRPC":
                return new GrpcStrategy(nomeComponente);
            default:
                throw new IllegalArgumentException("Protocolo não suportado: " + protocolType);
                // return new MockStrategy(nomeComponente);
        }
    }

}