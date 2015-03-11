package shapejs;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Wraps an error reporter.
 *
 * TODO: Not certain this is really working
 *
 * @author Alan Hudson
 */
class ErrorReporterWrapper implements ErrorReporter {
    private ErrorReporter original;
    private ArrayList<JsError> errors = new ArrayList<JsError>();

    ErrorReporterWrapper(ErrorReporter original) {
        this.original = original;
    }

    private void addError(String string, String string0, int i, String string1, int i0) {
        errors.add(new JsError(string, string0, i, string1, i0));
    }

    public void warning(String string, String string0, int i, String string1, int i0) {
        original.warning(string, string0, i, string1, i0);
    }

    public EvaluatorException runtimeError(String string, String string0, int i, String string1, int i0) {
        return original.runtimeError(string, string0, i, string1, i0);
    }

    public void error(String string, String string0, int i, String string1, int i0) {
        addError(string, string0, i, string1, i0);
    }

    public List<JsError> getErrors() {
        return errors;
    }
}

class JsError {
    static String toString(JsError[] e) {
        String rv = "";
        for (int i = 0; i < e.length; i++) {
            rv += e[i].toString();
            if (i + 1 != e.length) {
                rv += "\n";
            }
        }
        return rv;
    }

    private String message;
    private String sourceName;
    private int line;
    private String lineSource;
    private int lineOffset;

    JsError(String message, String sourceName, int line, String lineSource, int lineOffset) {
        this.message = message;
        this.sourceName = sourceName;
        this.line = line;
        this.lineSource = lineSource;
        this.lineOffset = lineOffset;
    }

    @Override
    public String toString() {
        String locationLine = "";
        if (sourceName != null)
            locationLine += sourceName + ":";
        if (line != 0)
            locationLine += line + ": ";
        locationLine += message;
        String sourceLine = this.lineSource;
        String errCaret = null;
        if (lineSource != null) {
            errCaret = "";
            for (int i = 0; i < lineSource.length(); i++) {
                char c = lineSource.charAt(i);
                if (i < lineOffset - 1) {
                    if (c == '\t') {
                        errCaret += "\t";
                    } else {
                        errCaret += " ";
                    }
                } else if (i == lineOffset - 1) {
                    errCaret += "^";
                }
            }
        }
        String rv = locationLine;
        if (sourceLine != null) {
            rv += "\n" + sourceLine;
        }
        if (errCaret != null) {
            rv += "\n" + errCaret;
        }
        return rv;
    }

    String getMessage() {
        return message;
    }

    String getSourceName() {
        return sourceName;
    }

    int getLine() {
        return line;
    }

    String getLineSource() {
        return lineSource;
    }

    int getLineOffset() {
        return lineOffset;
    }
}


