package com.rationaleemotions;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.rationaleemotions.pojo.ExecResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * A Worker that is capable of uploading a file to a remote host.
 */
class ScpUploadFileWorker implements Callable<ExecResults> {
    interface Marker {}

    private static final Logger LOGGER = LoggerFactory.getLogger(Marker.class.getEnclosingClass());

    private ChannelSftp channel;
    private File file;
    private String remoteLoc;

    ScpUploadFileWorker(Session session, File file, String remoteLoc) throws JSchException {
        channel = (ChannelSftp) session.openChannel("sftp");
        this.file = file;
        this.remoteLoc = remoteLoc;
    }

    @Override
    public ExecResults call() throws Exception {
        int rc;
        List<String> error = new LinkedList<>();
        try (InputStream stream = new FileInputStream(file)) {
            channel.connect();
            channel.put(stream, remoteLoc + "/" + file.getName());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Completed uploading [" + file.getAbsolutePath() + "] to [" + remoteLoc + "]");
            }
        } catch (JSchException | SftpException e) {
            String msg = "Encountered problems when uploading [" + remoteLoc + "]. Root cause : " + e.getMessage();
            error.add(msg);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed uploading [" + file.getAbsolutePath() + "] to [" + remoteLoc + "]", e);
            }
        } finally {
            rc = channel.getExitStatus();
            channel.disconnect();
        }
        return new ExecResults(new LinkedList<>(), error, rc);
    }
}
