package projetobd;

import com.datastax.oss.driver.api.core.CqlIdentifier;
import com.datastax.oss.driver.api.core.CqlSession;
import java.net.InetSocketAddress;

/**
 *
 * @author ericarfs
 */
public class CassandraConnection {
    private static CqlSession session;

    private CassandraConnection() {}

    public static CqlSession getSession() {
        if (session == null) {
            session = CqlSession.builder()
                    .addContactPoint(new InetSocketAddress("127.0.0.1", 9042))
                    .withKeyspace(CqlIdentifier.fromCql("universidade"))
                    .withLocalDatacenter("datacenter1")
                    .build();
        }
        return session;
    }

    public static void closeSession() {
        if (session != null) {
            session.close();
            session = null;
        }
    }
}