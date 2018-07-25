package com.rationaleemotions.server;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

public class LocalServer {
    interface Marker {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Marker.class.getEnclosingClass());
    public static final String LOCALHOST = "localhost";
    public static final String HOME = System.getProperty("user.dir");
    public static final String TARGET = HOME + File.separator + "target" + File.separator
        + "destination" + File.separator;
    public int port;
    private SshServer sshd;
    private PasswordAuthenticator passwordAuthenticator;
    private PublickeyAuthenticator publickeyAuthenticator;

    public void setPasswordAuthenticator(PasswordAuthenticator passwordAuthenticator) {
        this.passwordAuthenticator = passwordAuthenticator;
    }

    public void setPublickeyAuthenticator(PublickeyAuthenticator publickeyAuthenticator) {
        this.publickeyAuthenticator = publickeyAuthenticator;
    }

    static {
        new File(TARGET).mkdirs();
    }

    public void startServer() throws IOException {
        sshd = SshServer.setUpDefaultServer();
        port = getPort();
        sshd.setPort(port);
        if (this.passwordAuthenticator != null) {
            sshd.setPasswordAuthenticator(this.passwordAuthenticator);
        }
        if (this.publickeyAuthenticator != null) {
            sshd.setPublickeyAuthenticator(this.publickeyAuthenticator);
        }
        SimpleGeneratorHostKeyProvider provider = new SimpleGeneratorHostKeyProvider();
        sshd.setKeyPairProvider(provider);
        SftpSubsystemFactory.Builder builder = new SftpSubsystemFactory.Builder();

        SftpSubsystemFactory sftpFactory = builder.build();
        sshd.setSubsystemFactories(new ArrayList<NamedFactory<Command>>() {
            {
                add(sftpFactory);
            }
        });
        sshd.setHost(LOCALHOST);
        String msg = String.format("Local SSH Server started on [%s] and listening on port [%d]", sshd.getHost(), sshd
            .getPort());
        LOGGER.info(msg);
        ScpCommandFactory factory = new ScpCommandFactory();
        factory.setDelegateCommandFactory(new FakeCommandFactory());
        sshd.setCommandFactory(factory);
        sshd.start();
    }

    public void stopServer() throws IOException {
        sshd.close();
    }

    private int getPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int sshPort = serverSocket.getLocalPort();
        serverSocket.close();
        return sshPort;
    }

}
