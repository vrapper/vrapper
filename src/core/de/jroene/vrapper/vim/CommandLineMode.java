package de.jroene.vrapper.vim;

import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Command Line Mode, activated with ':'. Responsible for buffering the input
 * and parsing and executing commands.
 * 
 * @author Matthias Radig
 */
public class CommandLineMode extends AbstractMode {

    private static final List<VimInputEvent> abortEvents = Arrays.asList(
            VimInputEvent.ESCAPE, VimInputEvent.RETURN);
    private final StringBuffer buffer;

    public CommandLineMode(VimEmulator vim) {
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
            if (VimInputEvent.RETURN.equals(e)) {
                parseAndExecute();
            }
            vim.toNormalMode();
            buffer.setLength(0);
        }
        vim.getPlatform().setCommandLine(buffer.toString());
        return false;
    }

    /**
     * Parses and executes the given command if possible.
     * 
     * @param command
     *            the command to execute.
     */
    public void parseAndExecute(String command) {
        StringTokenizer nizer = new StringTokenizer(command);
        if (nizer.hasMoreTokens()) {
            String token = nizer.nextToken();
            if ("w".equals(token)) {
                vim.getPlatform().save();
            } else if (token.equals("set")) {
                if (nizer.hasMoreTokens()) {
                    String var = nizer.nextToken();
                    if (var.equals("autoindent")) {
                        vim.getVariables().setAutoIndent(true);
                    } else if (var.equals("noautoindent")) {
                        vim.getVariables().setAutoIndent(false);
                    }
                }
            } else if (token.equals("nn") || token.equals("nnoremap")) {
                if (nizer.hasMoreTokens()) {
                    String lhs = nizer.nextToken();
                    if (nizer.hasMoreTokens()) {
                        String rhs = nizer.nextToken();
                        if (lhs.length() == 1 && rhs.length() == 1) {
                            vim.getNormalMode().overrideMapping(lhs.charAt(0),
                                    rhs.charAt(0));
                        }
                    }
                }
            }
        }
    }

    private void parseAndExecute() {
        parseAndExecute(buffer.substring(1, buffer.length()));
    }
}
