package de.jroene.vrapper.vim;

import java.util.StringTokenizer;


/**
 * Command Line Mode, activated with ':'. Responsible for buffering the input
 * and parsing and executing commands.
 * 
 * @author Matthias Radig
 */
public class CommandLineMode extends AbstractCommandMode {

    public CommandLineMode(VimEmulator vim) {
        super(vim);
    }

    @Override
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
                    } else if (var.equals("globalregisters")) {
                        vim.useGlobalRegisters();
                    } else if (var.equals("noglobalregisters")) {
                        vim.useLocalRegisters();
                    }
                }
            } else if (token.equals("noremap")) {
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
}
