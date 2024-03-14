package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Thrown when somethings goes wrong in the platform code which is highly exceptional (e.g. a bad
 * offset, or a selection which got cleared).
 * For commands, use {@link CommandExecutionException} instead.
 */
public class VrapperPlatformException extends RuntimeException {

    private static final long serialVersionUID = -1931341520244506432L;

    public VrapperPlatformException(String message, Throwable cause) {
        super(message, cause);
    }

    public VrapperPlatformException(String message) {
        super(message);
    }

    public VrapperPlatformException(Throwable cause) {
        super(cause);
    }
}
