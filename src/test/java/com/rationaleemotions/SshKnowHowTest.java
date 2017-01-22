package com.rationaleemotions;

import com.rationaleemotions.pojo.EnvVariable;
import com.rationaleemotions.pojo.ExecResults;
import com.rationaleemotions.pojo.SSHUser;
import com.rationaleemotions.pojo.Shells;
import com.rationaleemotions.server.FakeCommandFactory;
import com.rationaleemotions.server.LocalServer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SshKnowHowTest {
    private static final String CMD = "whoami";
    private static final String MYDIRECTORY = "mydirectory";
    private static final String RESOURCES_FOLDER = "src" + File.separator + "test" + File.separator + "resources"
        + File.separator + "artifacts" + File.separator;
    private SshKnowHow ssh;

    @BeforeClass
    public void beforeClass() throws IOException {

        LocalServer server = new LocalServer();
        server.startServer();
        ExecutionBuilder builder = new ExecutionBuilder();

        SSHUser user = new SSHUser.Builder().withSshFolder(new File("src/test/resources/.ssh")).build();
        ssh = builder.connectTo(LocalServer.LOCALHOST).includeHostKeyChecks(false).onPort(server.port)
            .usingUserInfo(user).build();
    }

    @Test
    public void testHappyFlow() {
        ExecResults results = ssh.executeCommand(CMD);
        String exp = expectedString(CMD, null, Shells.BASH);
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
    }

    @Test
    public void testErrorFlow() {
        String cmd = FakeCommandFactory.FAILURE + CMD;
        ExecResults results = ssh.executeCommand(cmd);
        String exp = expectedString(cmd, null, Shells.BASH);
        Assert.assertEquals(results.getError().size(), 1, "Validating the command error size.");
        Assert.assertEquals(results.getError().get(0), exp, "Validating the command error.");
        Assert.assertEquals(results.getReturnCode(), - 1, "Validating the return code.");
    }

    @Test
    public void testHappyFlowWithDirectory() {
        ExecResults results = ssh.executeCommand(CMD, MYDIRECTORY);
        String exp = expectedString(CMD, MYDIRECTORY, Shells.BASH);
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
    }

    @Test
    public void testVeryLargeOutput() {
        ExecResults results = ssh.executeCommand("cat big.txt");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
        String lastLine = "Simple-ssh hopes to be simple.";
        int size = results.getOutput().size();
        String actual = results.getOutput().get(size - 1).trim();
        Assert.assertTrue(actual.contains(lastLine), "Validating the large output.");
        Assert.assertEquals(actual, lastLine, "Validating the large output.");
    }

    @Test
    public void testHappyFlowWithEnvVariable() {
        EnvVariable env = new EnvVariable("foo", "bar");
        ExecResults results = ssh.executeCommand(CMD, env);
        String exp = expectedString(CMD, null, Shells.BASH, env);
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
    }

    @Test
    public void testHappyFlowWithDirectoryAndEnvVariable() {
        EnvVariable env = new EnvVariable("foo", "bar");
        ExecResults results = ssh.executeCommand(CMD, MYDIRECTORY, env);
        String exp = expectedString(CMD, MYDIRECTORY, Shells.BASH, env);
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
    }

    @Test
    public void testHappyFlowWithDirectoryAndEnvVariables() {
        EnvVariable[] env = {new EnvVariable("foo", "bar"),
            new EnvVariable("name", "mryutu")
        };
        ExecResults results = ssh.executeCommand(CMD, MYDIRECTORY, env);
        String exp = expectedString(CMD, MYDIRECTORY, Shells.BASH, env);
        Assert.assertEquals(results.getOutput().size(), 1, "Validating the command output size.");
        Assert.assertEquals(results.getOutput().get(0), exp, "Validating the command output.");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
    }

    @Test
    public void testUploadFile() throws IOException {
        String fileName1 = "uploadTxtFile1.txt";
        String fileName2 = "uploadTxtFile2.txt";
        File[] files = new File[]
            {new File(RESOURCES_FOLDER + fileName1),
                new File(RESOURCES_FOLDER + fileName2)};
        ExecResults reply = ssh.uploadFile("~/target/destination/", files);
        Assert.assertEquals(reply.getReturnCode(), 0, "Validating the return code.");
        for (File file : files) {
            File actual = new File(LocalServer.TARGET + file.getName());
            Assert.assertTrue(actual.exists(), "Validating that the file " + actual.getAbsolutePath() + " exists.");
            Assert.assertEquals(getContents(actual), getContents(file),
                "Validating contents of " + actual.getAbsolutePath());
        }
    }

    @Test
    public void testDownloadFile() throws IOException {
        File dir = new File(LocalServer.TARGET);
        String fileName1 = "downloadTxtFile1.txt";
        String fileName2 = "downloadTxtFile2.txt";
        String[] remoteFiles = new String[] {
            "~/" + RESOURCES_FOLDER + fileName1,
            "~/" + RESOURCES_FOLDER + fileName2
        };
        ExecResults result = ssh.downloadFile(dir, remoteFiles);
        Assert.assertEquals(result.getReturnCode(), 0);
        for (String remoteFile : remoteFiles) {
            String fileName = remoteFile.substring(remoteFile.lastIndexOf("/") + 1);
            File actual = new File(LocalServer.TARGET + fileName);
            File expected = new File(RESOURCES_FOLDER + fileName);
            Assert.assertTrue(actual.exists(), "Validating that the file " + actual.getAbsolutePath() + " exists.");
            Assert.assertEquals(getContents(actual), getContents(expected),
                "Validating contents of " + actual.getAbsolutePath());
        }
    }

    @Test
    public void testUploadDirectory() {
        ExecResults results =
            ssh.uploadDirectory(new File("src/test/resources/upload"), "~/target/");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
        File uploadedDir = new File(LocalServer.HOME + File.separator + "target" + File.separator + "upload");
        Assert.assertTrue(uploadedDir.exists(), "Validating the existence of uploaded artifact");
        Assert.assertTrue(uploadedDir.isDirectory(), "Validating the uploaded artifact.");
        File[] files = uploadedDir.listFiles();
        Assert.assertNotNull(files, "Validating the items in the uploaded folder.");
        Assert.assertEquals(files.length, 1, "Validating the contents of the uploaded folder.");
    }

    @Test
    public void testDownloadDirectory() {
        ExecResults results = ssh.downloadDirectory(new File(LocalServer.TARGET), "~/src/test/resources/download");
        Assert.assertEquals(results.getReturnCode(), 0, "Validating the return code.");
        File downloadedDir = new File(LocalServer.TARGET + File.separator + "download");
        Assert.assertTrue(downloadedDir.exists(), "Validating the existence of downloaded artifact");
        Assert.assertTrue(downloadedDir.isDirectory(), "Validating the downloaded artifact.");
        File[] files = downloadedDir.listFiles();
        Assert.assertNotNull(files, "Validating the items in the downloaded folder.");
        Assert.assertEquals(files.length, 1, "Validating the contents of the downloaded folder.");
    }

    private String getContents(File file) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        return builder.toString();
    }

    private String expectedString(String cmd, String dir, Shells shell, EnvVariable... envs) {
        StringBuilder builder = new StringBuilder();
        if (dir != null) {
            builder.append("cd ").append(dir).append("; ");
        }
        if (envs != null) {
            for (EnvVariable env : envs) {
                builder.append(env.prettyPrintedForShell(shell)).append("; ");
            }
        }
        builder.append(cmd).append(";");
        return builder.toString();
    }

}
