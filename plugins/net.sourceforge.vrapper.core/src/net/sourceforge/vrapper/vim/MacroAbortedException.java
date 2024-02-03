package net.sourceforge.vrapper.vim;

/**
 * Thrown when (recursive) macro execution needs to halt.
 */
public class MacroAbortedException extends RuntimeException {

    private static final long serialVersionUID = 926431253992975593L;
    protected boolean uiError;

    public MacroAbortedException() {
        super();
    }

    public MacroAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MacroAbortedException(String message) {
        super(message);
    }

    public MacroAbortedException(Throwable cause) {
        super(cause);
    }
}
