package com.rationaleemotions;

import com.jcraft.jsch.*;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.ConnectorFactory;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.rationaleemotions.pojo.*;
import com.rationaleemotions.utils.Preconditions;
import com.rationaleemotions.utils.StreamGuzzler;
import org.apache.commons.vfs2.*;
import org.apache.commons.vfs2.impl.StandardFileSystemManager;
import org.apache.commons.vfs2.provider.sftp.IdentityInfo;
import org.apache.commons.vfs2.provider.sftp.SftpFileObject;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * A JSch backed implementation of {@link SshKnowHow}
 */
class JSchBackedSshKnowHowImpl implements SshKnowHow {
    interface Marker {
    }


    private static final Logger LOGGER = LoggerFactory.getLogger(Marker.class.getEnclosingClass());

    private static final int MAX_SIZE = 10;
    private SSHUser userInfo;
    private SSHHost hostInfo;
    private Shells shell;
    private String userHomeOnRemoteHost;
    private Session session;
    private String connectionString;
    private FileSystemOptions options;

    private JSchBackedSshKnowHowImpl(SSHHost host, SSHUser user, Shells shell) {
        this.hostInfo = host;
        this.userInfo = user;
        this.shell = shell;
        this.connectionString = String.format("sftp://%s@%s:%d", user.getUserName(), host.getHostname(), host.getPort());
    }

    static SshKnowHow newInstance(SSHHost host, SSHUser user, Shells shell) {
        JSchBackedSshKnowHowImpl instance = new JSchBackedSshKnowHowImpl(host, user, shell);
        instance.computeUserHome();
        Runtime.getRuntime().addShutdownHook(new Thread(new SessionCleaner(instance.session)));
        return instance;
    }

    @Override
    public ExecResults executeCommand(String cmd) {
        return runCommand(cmd, null);
    }

    @Override
    public ExecResults executeCommand(String cmd, String dir) {
        return runCommand(cmd, dir);
    }

    @Override
    public ExecResults executeCommand(String cmd, EnvVariable... env) {
        return runCommand(cmd, null, env);
    }

    @Override
    public ExecResults executeCommand(String cmd, String dir, EnvVariable... env) {
        return runCommand(cmd, dir, env);
    }

    @Override
    public ExecResults uploadFile(String remoteLocation, File... localFiles) {
        for (File file : localFiles) {
            Preconditions.checkArgument(file.exists(), "Cannot find [" + file.getAbsolutePath() + "]");
            Preconditions.checkArgument(file.isFile(), "[" + file.getAbsolutePath() + "] is not a file.");
        }
        Preconditions.checkArgument(identify(remoteLocation) == FileStatOutput.DIRECTORY, "[" +
            remoteLocation + "] is not a directory on " + hostInfo.getHostname());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Working with the parameters [" + Arrays.toString(new Object[] {remoteLocation, localFiles})
                + "]");
        }
        List<String> errors = new ArrayList<>();
        try {
            List<Callable<ExecResults>> workers = new ArrayList<>();
            for (File file : localFiles) {
                workers.add(new ScpUploadFileWorker(getSession(), file, fixRemoteLocation(remoteLocation)));
            }
            int poolSize = Math.max(MAX_SIZE, localFiles.length);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Total number of worker threads to be created :" + poolSize);
            }
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);
            List<Future<ExecResults>> execResults = executor.invokeAll(workers);
            executor.shutdown();
            while (! executor.isTerminated()) {
                //Wait for all the tasks to complete.
                TimeUnit.SECONDS.sleep(1);
            }
            for (Future<ExecResults> execResult : execResults) {
                ExecResults res = execResult.get();
                if (res.getReturnCode() != 0) {
                    errors.addAll(res.getError());
                }
            }
        } catch (JSchException | InterruptedException | ExecutionException e) {
            throw new ExecutionFailedException(e);
        }
        int rc = 0;
        if (! errors.isEmpty()) {
            rc = - 1;
        }
        return new ExecResults(new LinkedList<>(), errors, rc);
    }

    @Override
    public ExecResults downloadFile(File localLocation, String... remoteFiles) {
        Preconditions.checkArgument(localLocation.exists(), "Cannot find [" + localLocation.getAbsolutePath() + "]");
        Preconditions.checkArgument(localLocation.isDirectory(), "[" + localLocation.getAbsolutePath() + "] is NOT a"
            + " directory.");
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Working with the parameters [" + Arrays
                .toString(new Object[] {localLocation.getAbsolutePath(), remoteFiles})
                + "]");
        }

        List<String> errors = new ArrayList<>();
        try {
            List<Callable<ExecResults>> workers = new ArrayList<>();
            for (String remoteFile : remoteFiles) {
                workers.add(new ScpDownloadFileWorker(getSession(), localLocation, fixRemoteLocation(remoteFile)));
            }
            int poolSize = Math.max(MAX_SIZE, remoteFiles.length);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Total number of worker threads to be created :" + poolSize);
            }
            ExecutorService executor = Executors.newFixedThreadPool(poolSize);
            List<Future<ExecResults>> execResults = executor.invokeAll(workers);
            executor.shutdown();
            while (! executor.isTerminated()) {
                //Wait for all the tasks to complete.
                TimeUnit.SECONDS.sleep(1);
            }
            for (Future<ExecResults> execResult : execResults) {
                ExecResults res = execResult.get();
                if (res.getReturnCode() != 0) {
                    errors.addAll(res.getError());
                }
            }
        } catch (JSchException | InterruptedException | ExecutionException e) {
            throw new ExecutionFailedException(e);
        }

        int rc = 0;
        if (! errors.isEmpty()) {
            rc = - 1;
        }
        return new ExecResults(new LinkedList<>(), errors, rc);
    }

    @Override
    public String getHomeDirectory() {
        if (userHomeOnRemoteHost == null || userHomeOnRemoteHost.trim().isEmpty()) {
            throw new IllegalStateException("Unable to compute Home directory for " + userInfo.getUserName() +
                " on the remote host " + hostInfo.getHostname());
        }
        return userHomeOnRemoteHost;
    }

    @Override
    public void closeConnections() {
        new SessionCleaner(session).run();
    }

    @Override
    public ExecResults uploadDirectory(File localFrom, String remoteTo) {
        return copyDirectory(true, localFrom.getAbsolutePath(), remoteTo);
    }

    @Override
    public ExecResults downloadDirectory(File localTo, String remoteFrom) {
        return copyDirectory(false, localTo.getAbsolutePath(), remoteFrom);
    }

    @Override
    public void enableTunnellingTo(SSHHost tunnelHost) { // Refer http://stackoverflow.com/a/28852678
        try {
            newSession();
            session.setPortForwardingL(tunnelHost.getPort(), tunnelHost.getHostname(), this.hostInfo.getPort());
            session.connect();
            //Refer http://epaul.github.io/jsch-documentation/javadoc/com/jcraft/jsch/Session.html#openChannel-java.lang.String-
            session.openChannel("direct-tcpip");
        } catch(JSchException e) {
            throw new ExecutionFailedException(e);
        }
    }

    private Session newSession() throws JSchException {
        JSch jSch = new JSch();
        try {
            jSch.setConfig("PreferredAuthentications", "publickey");

            if (hostInfo.isDoHostKeyChecks()) {
                jSch.setKnownHosts(userInfo.sshFolderLocation() + File.separator + "known_hosts");
            } else {
                jSch.setHostKeyRepository(new FakeHostKeyRepository());
            }

            if (userInfo.isUseAgentIdentities()) {
                Connector connector = ConnectorFactory.getDefault().createConnector();
                if (connector != null) {
                    IdentityRepository identityRepository = new RemoteIdentityRepository(connector);
                    jSch.setIdentityRepository(identityRepository);
                }
            }

            // add private key to the IdentityRepository. If using agent identities, this will add the private
            // key to the agent, if it is not already present.
            jSch.addIdentity(userInfo.privateKeyLocation().getAbsolutePath());

            session = jSch.getSession(userInfo.getUserName(), hostInfo.getHostname(), hostInfo.getPort());
            Long timeout = TimeUnit.SECONDS.toMillis(hostInfo.getTimeoutSeconds());
            session.setTimeout(timeout.intValue());
            session.setUserInfo(new PasswordlessEnabledUser(userInfo.getPassphrase()));
            return session;
        } catch (JSchException | AgentProxyException e) {
            String msg = ExecutionFailedException.userFriendlyCause(e.getMessage(), hostInfo.getHostname(), userInfo);
            throw new ExecutionFailedException(msg, e);
        }
    }

    private Session getSession() throws JSchException {
        if (session != null) {
            if (! session.isConnected()) {
                session.connect();
            }
            return session;
        }
        newSession();
        if (session != null) {
            session.connect();
        }
        return session;
    }

    private String constructCommand(String cmd, String dir, EnvVariable... envs) {
        StringBuilder builder = new StringBuilder();
        if (dir != null) {
            builder.append("cd ").append(dir).append("; ");
        }
        if (envs != null) {
            for (EnvVariable env : envs) {
                builder.append(env.prettyPrintedForShell(shell)).append("; ");
            }
        }
        String command = builder.append(cmd).append(";").toString();
        return String.format(shell.cmdFormat(), command);
    }

    private ExecResults runCommand(String cmd, String dir, EnvVariable... envs) {
        ExecResults results;
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) getSession().openChannel("exec");
            String cmdToUse = constructCommand(cmd, dir, envs);
            LOGGER.info(String.format("Executing the command [%s]", cmdToUse));
            channel.setCommand(cmdToUse);
            channel.connect();
            StreamGuzzler output = new StreamGuzzler(channel.getInputStream());
            StreamGuzzler error = new StreamGuzzler(channel.getErrStream());
            ExecutorService executors = Executors.newFixedThreadPool(2);
            executors.submit(error);
            executors.submit(output);
            executors.shutdown();
            while (! executors.isTerminated()) {
                //Wait for all the tasks to complete.
                TimeUnit.SECONDS.sleep(1);
            }
            results = new ExecResults(output.getContent(), error.getContent(), channel.getExitStatus());
        } catch (JSchException | IOException | InterruptedException e) {
            throw new ExecutionFailedException(e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
        if (results.hasErrors()) {
            LOGGER.warn(String.format("Results of the command execution :%s", results.getError()));
        }
        return results;
    }

    private void computeUserHome() {
        ExecResults results = executeCommand("echo $HOME");
        if (results != null && ! results.getOutput().isEmpty()) {
            userHomeOnRemoteHost = results.getOutput().get(0);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("User Home Directory : " + userHomeOnRemoteHost);
            }
        }
    }

    private String fixRemoteLocation(String remoteLocation) {
        String home = getHomeDirectory();
        String newLocation = remoteLocation.replaceFirst("~/", home + "/");
        return newLocation.replaceFirst("$HOME", home + "/");
    }

    private FileStatOutput identify(String remoteLocation) {
        ExecResults results = executeCommand("stat --format=%F " + remoteLocation);
        String text;
        if (results.hasErrors()) {
            text = results.getError().get(0);
        } else {
            text = results.getOutput().get(0);
        }
        return FileStatOutput.parse(text);
    }

    private FileSystemOptions getOptions() throws FileSystemException {
        if (options != null) {
            return options;
        }
        options = new FileSystemOptions();
        SftpFileSystemConfigBuilder builder = SftpFileSystemConfigBuilder.getInstance();
        builder.setStrictHostKeyChecking(options, "no");
        IdentityInfo identityInfo;
        if (userInfo.getPassphrase() != null && ! userInfo.getPassphrase().trim().isEmpty()) {
            identityInfo = new IdentityInfo(userInfo.privateKeyLocation(), userInfo.getPassphrase().getBytes());
        } else {
            identityInfo = new IdentityInfo(userInfo.privateKeyLocation());
        }
        builder.setIdentityInfo(options, identityInfo);
        builder.setTimeout(options, this.hostInfo.getTimeoutSeconds());
        builder.setUserDirIsRoot(options, false);
        return options;
    }

    private ExecResults copyDirectory(boolean upload, String sourceLocation, String targetLocation) {
        int rc = - 1;
        StandardFileSystemManager manager = new StandardFileSystemManager();
        String remoteDirToUse = fixRemoteLocation(targetLocation);
        List<String> errors = new ArrayList<>();

        try {
            manager.init();
            SftpFileObject fObject = (SftpFileObject) manager.resolveFile(connectionString, getOptions());
            String localDirTouse;
            if (upload) {
                FileObject source = manager.resolveFile((new File(sourceLocation)).getAbsolutePath());
                localDirTouse = remoteDirToUse + "/" + source.getName().getBaseName();
                SftpFileObject destination = (SftpFileObject) fObject.resolveFile(localDirTouse, NameScope.FILE_SYSTEM);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Source :" + source.getPublicURIString());
                    LOGGER.debug("Destination :" + destination.getPublicURIString());
                }
                destination.createFolder();
                destination.copyFrom(source, Selectors.SELECT_ALL);
            } else {
                SftpFileObject source = (SftpFileObject) fObject.resolveFile(remoteDirToUse, NameScope.FILE_SYSTEM);
                localDirTouse = sourceLocation + File.separator + source.getName().getBaseName();
                File file = new File(localDirTouse);
                FileObject destination = manager.resolveFile(file.getAbsolutePath());
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Source :" + source.getPublicURIString());
                    LOGGER.debug("Destination :" + destination.getPublicURIString());
                }
                destination.createFolder();
                destination.copyFrom(source, Selectors.SELECT_ALL);
            }
            rc = 0;
        } catch (FileSystemException e) {
            errors.add(e.getMessage());
            if (LOGGER.isDebugEnabled()) {
                String msg;
                if (upload) {
                    msg = String.format("Failed %s %s to %s", "uploading", sourceLocation, targetLocation);
                } else {
                    msg = String.format("Failed %s %s to %s", "downloading", targetLocation, sourceLocation);
                }
                LOGGER.debug(msg, e);
            }
        } finally {
            manager.close();
        }
        return new ExecResults(new ArrayList<>(), errors, rc);
    }
}
