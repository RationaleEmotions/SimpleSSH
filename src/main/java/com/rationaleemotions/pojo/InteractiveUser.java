package com.rationaleemotions.pojo;

import com.jcraft.jsch.UserInfo;

/**
 * Represents a user for whom it's assumed that passwordless setup has already been completed.
 * Passwordless setup refers to the process wherein a user is allowed login to a remote host via ssh
 * without being prompted for their password.
 * The setup is fairly simple. Please refer <a href='http://www.linuxproblem.org/art_9.html'>here</a> for more details
 * on how to setup passwordless access
 * to a remote host.
 */
public class InteractiveUser implements UserInfo {
    private String passphrase;
    private String password;

    private InteractiveUser() {
        //We have factory methods. Defeat instantiation
    }

    private void setPassphrase(String passphrase) {
        this.passphrase = passphrase;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getPassphrase() {
        return passphrase;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean promptPassword(String message) {
        return password != null;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return passphrase != null;
    }

    @Override
    public boolean promptYesNo(String message) {
        return false;
    }

    @Override
    public void showMessage(String message) {
        //No Op implementation
    }

    public static UserInfo createPasswordlessUser(String passphrase) {
        InteractiveUser user = new InteractiveUser();
        user.setPassphrase(passphrase);
        return user;
    }

    public static UserInfo createPasswordDrivenUser(String password) {
        InteractiveUser user = new InteractiveUser();
        user.setPassword(password);
        return user;
    }
}
