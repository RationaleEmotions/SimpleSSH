package com.rationaleemotions.server;

import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.scp.ScpCommandFactory;
import org.apache.sshd.server.subsystem.sftp.SftpSubsystemFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;

/**
 *
 */
public class LocalServer {
    interface Marker {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(Marker.class.getEnclosingClass());
    public static final String LOCALHOST = "localhost";
    static final String HOME = System.getProperty("user.dir");
    public static final String TARGET = HOME + File.separator + "target" + File.separator
        + "destination" + File.separator;
    public int port;

    static {
        assert new File(TARGET).mkdirs();
    }

    public void startServer() throws IOException {
        SshServer sshd = SshServer.setUpDefaultServer();
        port = getPort();
        sshd.setPort(port);
        sshd.setPublickeyAuthenticator(AcceptAllPublickeyAuthenticator.INSTANCE);
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
        Runtime.getRuntime().addShutdownHook(new Thread(new Close(sshd)));
        sshd.start();
    }

    private int getPort() throws IOException {
        ServerSocket serverSocket = new ServerSocket(0);
        int sshPort = serverSocket.getLocalPort();
        serverSocket.close();
        return sshPort;
    }

}
