package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/**
 * :reg[isters] [args]
 * :di[splay] [args]
 * List the contents of all registers,
 * or list contents of [args] registers.
 */
public class ListRegistersCommand extends AbstractMessagesCommand {

    private String toDisplay;

    public ListRegistersCommand() {
        this("");
    }
    
    public ListRegistersCommand(String toDisplay) {
        this.toDisplay = toDisplay;
    }

    @Override
    protected String getMessages(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        StringBuilder sb = new StringBuilder();
        RegisterManager registers = editorAdaptor.getRegisterManager();
        List<String> names;
        if(toDisplay.length() > 0) {
            toDisplay = toDisplay.replaceAll(" ", "");
            // Split into chars.
            names = Arrays.asList(toDisplay.split("(?!^)"));
        } else {
            Set<String> allNames = registers.getRegisterNames();
            names = new ArrayList<String>(allNames);
        }
        Collections.sort(names);
        String reg;
        for(String name : names) {
            reg = registers.getRegister(name).getContent().getText();
            reg = VimUtils.replaceNewLines(reg, "^J").trim();
            sb.append("\"").append(name).append("   ").append(reg).append("\n");
        }

        return sb.toString();
    }

    @Override
    public boolean isClipped() {
        return true;
    }

}
