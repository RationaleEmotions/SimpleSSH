package com.rationaleemotions;

import com.jcraft.jsch.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A worker that cleans sessions via {@link Runtime#addShutdownHook(Thread)}
 */
class SessionCleaner implements Runnable {
    interface Marker {}

    private static final Logger LOGGER = LoggerFactory.getLogger(Marker.class.getEnclosingClass());

    private Session session;

    SessionCleaner(Session session) {
        this.session = session;
    }

    @Override
    public void run() {
        try {
            if (session != null) {
                session.disconnect();
            }
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(),e);
        }
    }
}
