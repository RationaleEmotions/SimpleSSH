package com.rationaleemotions;

import com.rationaleemotions.pojo.SSHUser;

/**
 * Represents all problems that can arise out of ssh execution attempts.
 */
public class ExecutionFailedException extends RuntimeException {

    public ExecutionFailedException(Throwable cause) {
        super(cause);
    }

    public ExecutionFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    static String userFriendlyCause(String msg, String hostname, SSHUser userInfo) {
        String prefix = "";
        if (msg.toLowerCase().contains("reject hostkey")) {
            prefix = "Missing entry for [" + hostname + "] in [" + userInfo.knownHostsFileLocation() + "]";
        } else if (msg.toLowerCase().contains("auth cancel")) {
            prefix = "Please setup passwordless access to [" + hostname + "] for the user [" + userInfo.getUserName() +
                "] before trying again. To learn how to setup passwordless access, "
                + "you can refer to http://www.linuxproblem.org/art_9.html";
        }
        return prefix + ". Root cause : " + msg;
    }
}
