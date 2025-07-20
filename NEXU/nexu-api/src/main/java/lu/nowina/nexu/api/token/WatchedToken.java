package lu.nowina.nexu.api.token;

import eu.europa.esig.dss.token.SignatureTokenConnection;

public class WatchedToken {
    final SignatureTokenConnection token;
    volatile long lastUsed;

    WatchedToken(SignatureTokenConnection token) {
        this.token = token;
        this.lastUsed = System.currentTimeMillis();
    }

    void touch() {
        this.lastUsed = System.currentTimeMillis();
    }
}