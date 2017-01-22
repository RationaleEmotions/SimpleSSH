package com.rationaleemotions.pojo;

import com.rationaleemotions.utils.Preconditions;

import java.io.File;


/**
 * Represents the attributes of a user for whom a remote ssh is being attempted.
 */
public class SSHUser {
    private static final String USER_NAME = "user.name";
    private static final String USER_HOME = "user.home";
    private static final String SSH = ".ssh";
    private String userName;
    private File sshFolder;
    private File privateKey;
    private String passphrase;

    private SSHUser() {
        //We have a builder to construct this object. So hide the constructor.
    }

    /**
     * @return - a {@link File} object that represents the actual location of the <b>.ssh</b> folder.
     * Typically this is in the HOME directory <b>"~"</b>
     */
    public File sshFolderLocation() {
        return sshFolder;
    }

    /**
     * @return - A {@link File} object that represents a user's private key location. The private key [id_rsa (or)
     * id_dsa ] is typically located under <b>~/.ssh</b> folder.
     */
    public File privateKeyLocation() {
        return privateKey;
    }

    /**
     * @return - The user for which ssh would be attempted.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return - The location to where the <b>known_hosts</b> file exists.
     * This location is typically <b>~/.ssh/known_hosts</b> file. Here <b>~/.ssh</b> will be different based
     * on what was provided to instantiate a {@link SSHUser} object.
     */
    public String knownHostsFileLocation() {
        return sshFolder.getAbsolutePath() + File.separator + "known_hosts";
    }

    public String getPassphrase() {
        return passphrase;
    }

    /**
     * Creates a {@link SSHUser} object for the currently logged in user.
     * Here are the assumptions that is made here :
     * <ol>
     * <li>User name - The currently logged-in user as identified via the Java property <b>user.name</b></li>
     * <li>User's home directory - The home directory of the currently logged-in user as identified via the
     * Java property <b>user.home</b></li>
     * </ol>
     */
    public static class Builder {
        private SSHUser user;

        public Builder() {
            user = new SSHUser();
        }

        /**
         * Creates a {@link SSHUser} object for a user that is different from the currently logged in user.
         * Here are the assumptions that is made here :
         * <ol>
         * <li>User's home directory - The home directory is calculated relatively to the home directory of the
         * currently
         * logged in user. For e.g., if the current logged in user's home directory is <b>/home/ram</b> and if the
         * user for whom ssh is being attempted at <b>krishna</b> then the home directory is calculated as
         * <b>/home/krishna</b></li>
         * </ol>
         *
         * @param user - The user for whom ssh is being attempted.
         */
        public Builder forUser(String user) {
            this.user.userName = user;
            return this;
        }

        /**
         * @param sshFolder - The location of the <b>.ssh</b> folder.
         */
        public Builder withSshFolder(File sshFolder) {
            this.user.sshFolder = sshFolder;
            return this;
        }

        /**
         * @param privateKey - The location of the private key (either <b>id_rsa</b> (or) <b>id_dsa</b> )
         */
        public Builder usingPrivateKey(File privateKey) {
            this.user.privateKey = privateKey;
            return this;
        }

        /**
         * @param passphrase - A passphrase if applicable that is to be used to access the private keys.
         */
        public Builder usingPassphrase(String passphrase) {
            this.user.passphrase = passphrase;
            return this;
        }

        public SSHUser build() {
            if (user.userName == null || user.userName.trim().isEmpty()) {
                user.userName = System.getProperty(USER_NAME);
            }
            if (user.sshFolder == null) {
                user.sshFolder = locateSshFolderFolder(user.userName);
            }
            if (user.privateKey == null) {
                user.privateKey = constructLocationFrom(user.sshFolder);
            }
            return this.user;
        }

        private static File locateSshFolderFolder(String userName) {
            String currentuser = System.getProperty(USER_NAME);
            if (currentuser.equalsIgnoreCase(userName)) {
                return new File(System.getProperty(USER_HOME) + File.separator + SSH);
            }
            File file = new File(System.getProperty(USER_HOME));
            String raw = file.getParentFile().getAbsolutePath() + File.separator + userName + File.separator + SSH;
            return new File(raw);
        }

        private static File constructLocationFrom(File home) {
            Preconditions.checkArgument(home != null, "Home directory cannot be null.");
            Preconditions
                .checkArgument(home.exists(),
                    String.format("Home directory [%s] does not exist.", home.getAbsolutePath()));
            Preconditions.checkArgument(home.isDirectory(),
                String.format("Home directory [%s] is not a directory", home.getAbsolutePath()));

            File file = new File(home, "id_dsa");
            if (file.exists()) {
                return file;
            }
            file = new File(home, "id_rsa");
            if (file.exists()) {
                return file;
            }
            throw new IllegalStateException(
                "No private keys [id_dsa/id_rsa] found in [" + home.getAbsolutePath() + "]");
        }
    }



}
