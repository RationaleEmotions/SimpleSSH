package com.rationaleemotions;

import com.rationaleemotions.pojo.EnvVariable;
import com.rationaleemotions.pojo.ExecResults;
import com.rationaleemotions.pojo.SSHUser;
import com.rationaleemotions.pojo.Shells;
import com.rationaleemotions.server.LocalServer;
import java.io.IOException;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public abstract class AbstractSshKnowHowTest {
    private SshKnowHow ssh;
    private LocalServer server;
    static final String CMD = "whoami";

    @BeforeClass(alwaysRun = true)
    public void beforeClass() throws IOException {
        server = new LocalServer();
        server.setPasswordAuthenticator(getPasswordAuthenticator());
        server.setPublickeyAuthenticator(getPublickeyAuthenticator());
        server.startServer();
        ExecutionBuilder builder = new ExecutionBuilder();
        ssh = builder.connectTo(LocalServer.LOCALHOST).includeHostKeyChecks(false).onPort(server.port)
            .usingUserInfo(getSSHUser()).build();
    }

    final SshKnowHow getSsh() {
        return ssh;
    }

    @AfterClass(alwaysRun = true)
    public void shutdownServer() throws IOException {
        if (server != null) {
            server.stopServer();
        }
    }

    @Test
    public void testHappyFlow() {
        ExecResults results = getSsh().executeCommand(CMD);
        String exp = expectedString(CMD, null);
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
    }

    public abstract SSHUser getSSHUser();

    public PasswordAuthenticator getPasswordAuthenticator() {
        return null;
    }
    public PublickeyAuthenticator getPublickeyAuthenticator() {
        return null;
    }

    String expectedString(String cmd, String dir, EnvVariable... envs) {
        StringBuilder builder = new StringBuilder();
        if (dir != null) {
            builder.append("cd ").append(dir).append("; ");
        }
        if (envs != null) {
            for (EnvVariable env : envs) {
                builder.append(env.prettyPrintedForShell(Shells.BASH)).append("; ");
            }
        }
        builder.append(cmd).append(";");
        return builder.toString();
    }

}
