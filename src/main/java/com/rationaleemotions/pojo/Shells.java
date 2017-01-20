package com.rationaleemotions.pojo;

/**
 * Represents the SHELL flavors which are supported by <b>Simple-SSH</b>
 */
public enum Shells implements ShellAttribs {
    /**
     * BASH Shell.
     */
    BASH {
        @Override
        public String envKeyword() {
            return "export";
        }

        @Override
        public String envSeparator() {
            return "=";
        }

        @Override
        public String cmdFormat() {
            return "bash -c '%s'";
        }
    },
    /**
     * TCSH Shell.
     */
    TCSH {
        @Override
        public String envKeyword() {
            return "setenv";
        }

        @Override
        public String envSeparator() {
            return " ";
        }

        @Override
        public String cmdFormat() {
            return "tcsh -c '%s'";
        }
    }
}
