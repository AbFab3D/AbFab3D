package app.common;

/**
 * Result from a wallthickness run
 *
 * @author Alan Hudson
 */
public class WallThicknessResult {
    public enum ResultType {SAFE,UNSAFE,SUSPECT}

    private ResultType result;
    private int exitCode;

    public WallThicknessResult(int exitCode, ResultType result) {
        this.exitCode = exitCode;
        this.result = result;
    }

    public ResultType getResult() {
        return result;
    }

    public void setResult(ResultType result) {
        this.result = result;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }
}
