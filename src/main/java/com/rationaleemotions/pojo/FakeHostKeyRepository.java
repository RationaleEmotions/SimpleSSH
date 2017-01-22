package com.rationaleemotions.pojo;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.rationaleemotions.ExecutionBuilder;

/**
 * Represents a test flavor of {@link HostKeyRepository} that accepts the finger print of every remote host
 * blindly without validating its entry in the <b>~/.ssh/known_hosts</b> file.
 * This repository is used when you set {@link ExecutionBuilder#includeHostKeyChecks(boolean)}
 * to <b>false.</b>
 */
public class FakeHostKeyRepository implements HostKeyRepository {
    @Override
    public int check(String host, byte[] key) {
        return 0;
    }

    @Override
    public void add(HostKey hostkey, com.jcraft.jsch.UserInfo ui) {
        //No Op implementation
    }

    @Override
    public void remove(String host, String type) {
        //No Op implementation
    }

    @Override
    public void remove(String host, String type, byte[] key) {
        //No Op implementation
    }

    @Override
    public String getKnownHostsRepositoryID() {
        return null;
    }

    @Override
    public HostKey[] getHostKey() {
        return new HostKey[0];
    }

    @Override
    public HostKey[] getHostKey(String host, String type) {
        return new HostKey[0];
    }
}
