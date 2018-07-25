package com.rationaleemotions;

import com.rationaleemotions.pojo.SSHUser;
import org.apache.sshd.server.auth.password.PasswordAuthenticator;

public class PasswordDrivenSshKnowHowTest extends AbstractSshKnowHowTest {

    private static final String CMD = "whoami";
    private static final String USER = "kungfupanda";
    private static final String PASSWORD = "dragonwarrior";

    @Override
    public PasswordAuthenticator getPasswordAuthenticator() {
        return (username, password, session) ->
            "kungfupanda".equalsIgnoreCase(username) && "dragonwarrior".equalsIgnoreCase(password);
    }

    @Override
    public SSHUser getSSHUser() {
        return new SSHUser.Builder().forUser(USER).withPasswordAs(PASSWORD).build();
    }

}
