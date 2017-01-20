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
public class PasswordlessEnabledUser implements UserInfo {
    @Override
    public String getPassphrase() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean promptPassword(String message) {
        return false;
    }

    @Override
    public boolean promptPassphrase(String message) {
        return false;
    }

    @Override
    public boolean promptYesNo(String message) {
        return false;
    }

    @Override
    public void showMessage(String message) {

    }
}
