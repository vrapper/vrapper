package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Allows editor type specific .vrapperrc command execution.
 * autocmd "C/C++ Editor" eclipseaction gv org.eclipse.cdt.ui.edit.text.c.select.last
 * autocmd "C/C++ Editor" nnoremap <SPACE>rh :cpphidemethod<CR>
 * au "CMake Editor" nnoremap <CR> gf
 */
public class AutoCmdParser implements Command {
    private static final Pattern AUTOCMD_PATTERN = Pattern.compile("^au(tocmd)?\\s+\"([^\"]+)\"\\s+(.*)");
    public static final AutoCmdParser INSTANCE = new AutoCmdParser();
    private String newCommand;

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        // Empty command - editor type didn't match.
    }

    public Command repetition() {
        return null;
    }

    public Command withCount(int count) {
        return null;
    }

    public int getCount() {
        return 0;
    }

    public void parse(EditorAdaptor editoradaptor, String s) {
        Matcher m = AUTOCMD_PATTERN.matcher(s);
        if (m.matches() && m.group(2).equals(editoradaptor.getEditorType())) {
            newCommand = m.group(3);
        } else {
            newCommand = null;
        }
    }

    public boolean validate(EditorAdaptor editoradaptor, String s) {
        Matcher m = AUTOCMD_PATTERN.matcher(s);
        return m.matches();
    }

    public String getCommand() {
        return newCommand;
    }
}
