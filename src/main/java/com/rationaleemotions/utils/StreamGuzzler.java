package com.rationaleemotions.utils;

import com.rationaleemotions.ExecutionFailedException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 * A simple thread based utility that consumes {@link InputStream} and provides the stream contents
 * as a list of strings.
 * This utility is <b>NOT</b> designed to be re-used. So everytime a stream is to be consumed, a new
 * object of this utility is expected to be created. Attempting to re-use an existing instance of {@link StreamGuzzler}
 * for consuming stream will trigger errors.
 */
public class StreamGuzzler implements Runnable {
    private InputStream stream;
    private List<String> content = new LinkedList<>();

    public List<String> getContent() {
        return content;
    }

    public StreamGuzzler(InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        Preconditions.checkState(stream != null, "Cannot guzzle an empty/null stream.");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            String line;
            while ((line = reader.readLine()) != null) {
                content.add(line);
            }
        } catch (IOException exception) {
            throw new ExecutionFailedException(exception);
        } finally {
            this.stream = null;
        }
    }
}
