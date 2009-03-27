package de.jroene.vrapper.vim.commandline;

import java.util.Arrays;
import java.util.List;

import de.jroene.vrapper.vim.AbstractMode;
import de.jroene.vrapper.vim.NormalMode;
import de.jroene.vrapper.vim.Platform;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;
import de.jroene.vrapper.vim.token.Token;

/**
 * Base class for modes which parse strings given by the user.<br>
 * Shows the input in the {@link Platform}'s command line.
 *
 * @author Matthias Radig
 */
public abstract class AbstractCommandMode extends AbstractMode {

    private static final List<VimInputEvent> abortEvents = Arrays.asList(
            VimInputEvent.ESCAPE, VimInputEvent.RETURN);
    protected final StringBuffer buffer;

    public AbstractCommandMode(VimEmulator vim) {
        super(vim);
        buffer = new StringBuffer();
    }

    /**
     * Appends typed characters to the internal buffer. Deletes a char from the
     * buffer on press of the backspace key. Parses and executes the buffer on
     * press of the return key. Clears the buffer on press of the escape key.
     */
    public boolean type(VimInputEvent e) {
        if (e instanceof VimInputEvent.Character) {
            buffer.append(((VimInputEvent.Character) e).getCharacter());
        } else if (e.equals(VimInputEvent.BACKSPACE)) {
            buffer.deleteCharAt(buffer.length() - 1);
        }
        if (buffer.length() == 0 || abortEvents.contains(e)) {
            Token t = null;
            if (VimInputEvent.RETURN.equals(e)) {
                t = parseAndExecute();
            }
            vim.toNormalMode(t);
            buffer.setLength(0);
            vim.getPlatform().endChange();
        }
        vim.getPlatform().setCommandLine(buffer.toString());
        return false;
    }

    /**
     * Parses and executes the given command if possible.
     * 
     * @param first
     *            character used to activate the mode.
     * @param command
     *            the command to execute.
     * @return a Token which is given to the {@link NormalMode} instance. May be
     *         null.
     */
    public abstract Token parseAndExecute(String first, String command);


    public void toKeystrokeMode() {
        // do nothing
    }

    private Token parseAndExecute() {
        String first = buffer.substring(0,1);
        String command = buffer.substring(1, buffer.length());
        return parseAndExecute(first, command);
    }

}