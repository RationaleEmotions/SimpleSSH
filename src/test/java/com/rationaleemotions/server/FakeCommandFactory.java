package com.rationaleemotions.server;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.*;

/**
 *
 */
public class FakeCommandFactory implements CommandFactory {

    public static final String FAILURE = "::FAILURE::";

    @Override
    public Command createCommand(String command) {
        return new FakeFactory(command).create();
    }

    public static class FakeFactory implements Factory<Command> {

        private String cmd;

        FakeFactory(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public Command create() {
            return new FakeCommand(cmd);
        }
    }


    public static class FakeCommand implements Command, Runnable {
        private String cmd;
        private OutputStream out;
        private OutputStream err;
        private ExitCallback callback;
        private Thread thread;
        private PrintWriter writer;

        FakeCommand(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void setInputStream(InputStream in) {
        }

        @Override
        public void setOutputStream(OutputStream out) {
            this.out = out;
        }

        @Override
        public void setErrorStream(OutputStream err) {
            this.err = err;
        }

        @Override
        public void setExitCallback(ExitCallback callback) {
            this.callback = callback;
        }

        @Override
        public void start(Environment env) {
            this.thread = new Thread(this, "simple-ssh");
            this.thread.start();
        }

        @Override
        public void destroy() {
            this.thread.interrupt();
        }

        @Override
        public void run() {
            boolean hasfailure = false;
            boolean bigOutput = false;
            try {
                if (this.cmd.contains(FAILURE)) {
                    hasfailure = true;
                    this.writer = new PrintWriter(new OutputStreamWriter(err));
                } else {
                    this.writer = new PrintWriter(new OutputStreamWriter(out));
                }
                if (this.cmd.contains("cat big.txt")) {
                    bigOutput = true;
                }
                if (this.cmd.contains("stat")) {
                    writer.println("directory");
                }
                if (this.cmd.contains("$HOME")) {
                    String home = LocalServer.HOME;
                    writer.println(home);
                }
                if (bigOutput) {
                    BufferedReader localReader = new BufferedReader(new FileReader("src/test/resources/artifacts/big"
                        + ".txt"));
                    String line;
                    while ((line = localReader.readLine()) != null) {
                        writer.println(line);
                    }
                } else {
                    writer.println(extractOnlyCommand(cmd));
                }
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (hasfailure) {
                    this.callback.onExit(- 1);
                } else {
                    this.callback.onExit(0);
                }
            }
        }

        private String extractOnlyCommand(String cmd) {
            int start = cmd.indexOf('\'') + 1;
            int end = cmd.lastIndexOf('\'');
            return cmd.substring(start, end);
        }
    }
}
