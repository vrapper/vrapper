package net.sourceforge.vrapper.eclipse.jdt.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class JdtSpecificStateProvider extends AbstractEclipseSpecificStateProvider {
    
    public JdtSpecificStateProvider() {
        addFormatCommands(editJava("format"));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        Command gotoDecl = seq(editJava("open.editor"), DeselectAllCommand.INSTANCE); // NOTE: deselect won't work in other editor
        return union(
                state(
                        leafCtrlBind(']', gotoDecl),
                        transitionBind('g',
                                leafBind('r', editJava("refactor.quickMenu")), // not in Vim
                                leafBind('R', editJava("rename.element")))), // not in Vim
                prefixedOperatorCmds('g', 'c', seq(editJava("toggle.comment"), DeselectAllCommand.INSTANCE), textObjects),
                operatorCmds('=', seq(editJava("indent"), DeselectAllCommand.INSTANCE), textObjects));
    }

    protected static Command editJava(String cmd) {
        return new EclipseCommand("org.eclipse.jdt.ui.edit.text.java." + cmd);
    }
    
    @Override
    public String getFileType() {
        return "Java";
    }

}
