package app.common;

/**
 * Result from an aopt run
 *
 * @author Alan Hudson
 */
public class AoptResult {
    private int exitCode;
    private String output;

    public AoptResult(int exitCode, String output) {
        this.exitCode = exitCode;
        this.output = output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }
}
