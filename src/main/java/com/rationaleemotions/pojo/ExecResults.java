package com.rationaleemotions.pojo;


import java.util.List;

/**
 * This pojo represents the results of the execution of a UNIX command against a remote host.
 */
public class ExecResults {
    private List<String> output;
    private List<String> error;
    private int returnCode;

    /**
     * @param output     - A {@link List} of Strings that represents the output.
     * @param error      - A {@link List} of Strings that represents the error (if any).
     * @param returnCode - The return code of the command that was executed.
     */
    public ExecResults(List<String> output, List<String> error, int returnCode) {
        this.output = output;
        this.error = error;
        this.returnCode = returnCode;
    }

    public boolean hasErrors() {
        return (!error.isEmpty());
    }

    /**
     * @return - The return code of the command that was executed.
     */
    public int getReturnCode() {
        return returnCode;
    }

    /**
     * @return - A {@link List} of Strings that represents the error (if any).
     */
    public List<String> getError() {
        return error;
    }

    /**
     * @return - A {@link List} of Strings that represents the output.
     */
    public List<String> getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return "ExecResults{" +
            "output=" + output +
            ", error=" + error +
            ", returnCode=" + returnCode +
            '}';
    }
}
