package lu.nowina.nexu.api.token;

import eu.europa.esig.dss.token.SignatureTokenConnection;
import lu.nowina.nexu.api.TokenId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TokenWatchdog {

    private static final Logger LOG = LoggerFactory.getLogger(TokenWatchdog.class);

    private final Map<TokenId, WatchedToken> activeTokens = new ConcurrentHashMap<>();
    private final long timeoutMillis;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TokenWatchdog(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        executor.scheduleAtFixedRate(this::checkInactiveTokens, 0, 30_000, TimeUnit.MILLISECONDS);
    }

    public void registerToken(TokenId id, SignatureTokenConnection token) {
        activeTokens.put(id, new WatchedToken(token));
    }

    public SignatureTokenConnection getToken(TokenId id) {
        WatchedToken wt = activeTokens.get(id);
        if (wt != null) {
            wt.touch();
            return wt.token;
        }
        return null;
    }

    public void unregisterToken(TokenId id) {
        WatchedToken wt = activeTokens.remove(id);
        if (wt != null) {
            try {
                wt.token.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkInactiveTokens() {
        long now = System.currentTimeMillis();
        for (Map.Entry<TokenId, WatchedToken> entry : activeTokens.entrySet()) {
            if (now - entry.getValue().lastUsed > timeoutMillis) {
                try {
                    entry.getValue().token.close();
                    LOG.info("Token with ID {} has been closed due to inactivity.", entry.getKey().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    LOG.error("Failed to close token with ID " + entry.getKey().getId(), e);
                }
                unregisterToken(entry.getKey());
            }
        }
    }

}
