![Build Status](https://travis-ci.org/RationaleEmotions/SimpleSSH.svg?branch=master)

# Simple SSH

**Simple SSH** is a java library that currently supports the following capabilities :
* Run a command against a Remote UNIX Host.
* Upload/Download files/folders from/to a Remote UNIX Host.

**Simple SSH** works on passwordless authentication (i.e., connecting to a remote host without being prompted for a password.). So one of the pre-requisites for using this library is that a passwordless setup needs to have been completed against a remote host before interacting with it.

In order to learn how to setup passwordless access to a remote host, please refer [Here](http://www.linuxproblem.org/art_9.html)

## Pre-requisites

**Simple SSH** requires that you use : 
* **JDK 8**.
* Have setup passwordless access to a remote host. 

## How to use.

**Simple SSH** is a [Maven](https://maven.apache.org/guides/getting-started/) artifact. In order to 
consume it, you merely need to add the following as a dependency in your pom file.

```xml
<dependency>
    <groupId>com.rationaleemotions</groupId>
    <artifactId>simple-ssh</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Building a ssh aware object.

In order to start interacting with a Remote UNIX Host, we need to create an ssh aware object as below :

```java
SshKnowHow ssh = builder.connectTo("myUnixBox").build();
```

Here `myUnixBox` represents the name of the Remote UNIX Host. By default this attempts to connect to the Remote UNIX Host as the **currently logged-in user**.

If we need to disable host file checking (i.e., disable validating the finger print of the Remote UNIX Host against the local `~/.ssh/known_hosts`), we can do it as below :

```java
SshKnowHow ssh = builder.connectTo("myUnixBox").includeHostKeyChecks(false).build();
```

(Host File checking is enabled by default and its one of the ways in which UNIX helps you safe-guard your ssh access.)

In case we would like to connect to a particular Remote UNIX Host as a different user (say for e.g., as a service account), we can do it as below :
```java
SshKnowHow ssh = builder.connectTo("myUnixBox").asUser("service_account").build();
```

## Building a ssh user information.

If we would like to provide information such as the location of the `.ssh` folder, the location of the user's private keys (`id_dsa` (or) `id_rsa`), we can do it as below :

```java
SSHUser sshUser = new SSHUser.Builder()
    .forUser("service_account")
    .withSshFolder(new File("/shared/.ssh"))
    .usingPassphrase("secret-word")
    .usingPrivateKey(new File("/shared/.ssh/id_rsa"))
    .build();
SshKnowHow ssh = new ExecutionBuilder()
    .connectTo("myUnixBox")
    .includeHostKeyChecks(false)
    .usingUserInfo(sshUser)
    .build();
```

**Simple SSH** by default falls back to using **bash** shell. In-case you would like to fall back to using a different shell, you can do it as below :
```java
SshKnowHow remoteSsh = new ExecutionBuilder()
            .connectTo("myUnixBox")
            .includeHostKeyChecks(false)
            .usingShell(Shells.TCSH)
            .build();
```

## What can you do with an ssh object.

### Executing commands.

Once you have created an ssh object, you can execute a command against a Remote UNIX Host as below :
```java
ExecResults results = ssh.executeCommand("whoami");
```

* And then access the command output via `results.getOutput()` (or) 
* check if there were any errors via `results.getError()` (or) 
* check the return code of the executed command via `results.getReturnCode()`

In-case you need to pass in an environment variables and then execute a command, it can be done as below:
```java
EnvVariable env = new EnvVariable("foo", "bar");
ExecResults results = ssh.executeCommand("whoami", env);
```

Here's how you pass in one or more environment variables and a folder from wherein the execution should happen, along with the command to execute :

```java
EnvVariable env1 = new EnvVariable("foo", "bar");
EnvVariable env2 = new EnvVariable("blah", "blah-blah");
ExecResults results = ssh.executeCommand("pwd", "/usr/bin", env1, env2);
```

### Upload/download files.

Here's how to perform uploading files to a Remote UNIX Host: (Here we are uploading two files to the remote directory `~/target/destination/` in the Remote UNIX Host)

```java
File[] files = new File[]{
            new File("src/test/resources/foo.txt"),
            new File("src/test/resources/bar.txt")
        };
ExecResults reply = ssh.uploadFile("~/target/destination/", files);
```

Here's how to perform downloading files from a Remote UNIX Host: (Here we are downloading two files to the local directory `src/test/resources/`)

```java
File dir = new File("src/test/resources/");
String[] remoteFiles = new String[] {
                "~/artifacts/foo.txt",
                "~/artifacts/bar.txt"
            };
ExecResults result = ssh.downloadFile(dir, remoteFiles);
```

### Upload/download folder.

Here's how to perform uploading a folder to a Remote UNIX Host: (Here we are uploading a folder to the remote directory `~/target/destination/` in the Remote UNIX Host)

```java
ExecResults results =ssh.uploadDirectory(new File("src/test/resources/upload"), "~/target/destination/");
```

Here's how to perform downloading a folder from a Remote UNIX Host: (Here we are downloading a remote directory named `~/download` to the local directory `src/test/resources` )

```java
ExecResults results = ssh.downloadDirectory(new File("src/test/resources"), "~/download");
```
