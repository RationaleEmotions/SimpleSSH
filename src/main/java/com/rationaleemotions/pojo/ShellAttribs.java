package com.rationaleemotions.pojo;

import java.util.Locale;

/**
 * Represents the common attributes associated with a UNIX Shell flavor.
 */
public interface ShellAttribs {
    /**
     * @return - The keyword that represents environment variable setting.
     */
    String envKeyword();

    /**
     * @return - The separator character to be used when setting up environment variables.
     * For e.g., in BASH shell the separator character is <b>=</b> whereas in TCSH its <b>" "</b> (space)
     */
    String envSeparator();

    /**
     * @return - A format that kind of looks like the format supplied to {@link String#format(Locale, String, Object...)}
     * which would be internally used to represent how does a typical well formed command look like for a given shell.
     */
    String cmdFormat();
}
