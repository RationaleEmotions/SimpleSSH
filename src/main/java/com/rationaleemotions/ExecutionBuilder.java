package com.rationaleemotions;

import com.rationaleemotions.pojo.SSHHost;
import com.rationaleemotions.pojo.SSHUser;
import com.rationaleemotions.pojo.Shells;

import static com.rationaleemotions.utils.Preconditions.checkArgument;

/**
 * This is the main entry point of execution. This is a simple builder that helps in building a {@link SshKnowHow}
 * object that can be used to perform remote operations.
 */
public class ExecutionBuilder {

    private SSHUser sshUser = new SSHUser();
    private SSHHost host = new SSHHost();
    private Shells shell = Shells.BASH;

    /**
     * @param hostName - The host to which the ssh connection is to be made.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder connectTo(String hostName) {
        host.setHostname(hostName);
        return this;
    }

    /**
     * @param port - The port number on which the <b>ssh daemon</b> is listening to, in the remote host.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder onPort(int port) {
        host.setPort(port);
        return this;
    }

    /**
     * @param userName - The user using which the ssh connection is to be established.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder asUser(String userName) {
        sshUser = new SSHUser(userName);
        return this;
    }

    /**
     * @param sshUser - A {@link SSHUser} for which the ssh is to be done.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder usingUserInfo(SSHUser sshUser) {
        this.sshUser = sshUser;
        return this;
    }

    /**
     * @param shouldInclude - <code>true</code> if we should do a host key check against <b>~/.ssh/known_hosts</b>
     *                      file (or) if the host key verification should be by-passed.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder includeHostKeyChecks(boolean shouldInclude) {
        host.setDoHostKeyChecks(shouldInclude);
        return this;
    }

    /**
     * @param seconds - The timeout value in <b>seconds.</b>
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder timeoutInSeconds(int seconds) {
        host.setTimeoutSeconds(seconds);
        return this;
    }

    /**
     * @param shell - A {@link Shells} object that indicates the user's desired shell variant to be used.
     *              The default is going to be {@link Shells#BASH}.
     * @return - The {@link ExecutionBuilder} object that is being constructed.
     */
    public ExecutionBuilder usingShell(Shells shell) {
        this.shell = shell;
        return this;
    }

    /**
     * @return - A {@link SshKnowHow} object that can be used to perform remote operations such as executing commands
     * (or) performing scp (both upload and download) against a remote host.
     */
    public SshKnowHow build() {
        boolean validHost = ((host.getHostname() != null) && (! host.getHostname().trim().isEmpty()));
        checkArgument(validHost, "Please provide a valid hostname.");
        return JSchBackedSshKnowHowImpl.newInstance(host, sshUser, shell);
    }
}
