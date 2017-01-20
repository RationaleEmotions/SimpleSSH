package com.rationaleemotions.server;

import org.apache.sshd.server.SshServer;

import java.io.IOException;

/**
 *
 */
class Close implements Runnable {
    private SshServer sshd;

    Close(SshServer sshd) {
        this.sshd = sshd;
    }

    @Override
    public void run() {
        try {
            sshd.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
