package com.rationaleemotions.pojo;

/**
 * Represents the attributes associated with a remote host.
 */
public class SSHHost {
    private String hostname;
    private int port = 22;
    private boolean doHostKeyChecks;
    private int timeoutSeconds;

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean doHostKeyChecks() {
        return doHostKeyChecks;
    }

    public void setDoHostKeyChecks(boolean doHostKeyChecks) {
        this.doHostKeyChecks = doHostKeyChecks;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}
