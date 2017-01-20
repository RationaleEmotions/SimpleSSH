package com.rationaleemotions.pojo;

/**
 * This POJO represents the name value pair of an environment variable.
 */
public class EnvVariable {
    private String name;
    private String value;

    public EnvVariable(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * @return - The name of the environment variable.
     */
    public String getName() {
        return name;
    }

    /**
     * @return - The value of the environment variable.
     */
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "EnvNameValuePair{name='" + getName() + "\', value='" + getValue() + "\'}";
    }

    public String prettyPrintedForShell(Shells shell) {
        return String.format("%s %s%s%s", shell.envKeyword(),name, shell.envSeparator(), value);
    }
}
