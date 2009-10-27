package net.sourceforge.vrapper.eclipse.platform;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class JdtSpecificStateProvider extends EclipseSpecificStateProvider {
    public static final JdtSpecificStateProvider INSTANCE = new JdtSpecificStateProvider();
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        Command gotoDecl = seq(editJava("open.editor"), DeselectAllCommand.INSTANCE); // NOTE: deselect won't work in other editor
        return union(
                state(
                        leafCtrlBind(']', gotoDecl),
                        transitionBind('g',
                                leafBind('r', editJava("refactor.quickMenu")),
                                leafBind('R', editJava("rename.element")))),
                prefixedOperatorCmds('g', 'c', seq(editJava("toggle.comment"), DeselectAllCommand.INSTANCE), textObjects),
                operatorCmds('=', seq(editJava("indent"), DeselectAllCommand.INSTANCE), textObjects),
                super.normalModeBindings());
    }

    protected static Command editJava(String cmd) {
        return new EclipseCommand("org.eclipse.jdt.ui.edit.text.java." + cmd);
    }
    
    @Override
    protected Command getFormatCommand() {
        return editJava("format");
    }
    
    @Override
    public String getFileType() {
        return "Java";
    }
    
}
