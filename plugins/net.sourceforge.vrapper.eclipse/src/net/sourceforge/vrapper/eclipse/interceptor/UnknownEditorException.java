package net.sourceforge.vrapper.eclipse.interceptor;

public class UnknownEditorException extends Exception {
    private static final long serialVersionUID = 5917193889607811221L;

    public UnknownEditorException() {
    }

    public UnknownEditorException(String message) {
        super(message);
    }

    public UnknownEditorException(Throwable cause) {
        super(cause);
    }

    public UnknownEditorException(String message, Throwable cause) {
        super(message, cause);
    }
}
