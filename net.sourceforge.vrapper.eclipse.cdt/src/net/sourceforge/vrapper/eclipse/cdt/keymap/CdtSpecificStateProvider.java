package net.sourceforge.vrapper.eclipse.cdt.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class CdtSpecificStateProvider extends AbstractEclipseSpecificStateProvider {
    
    public CdtSpecificStateProvider() {
        addFormatCommands(editC("format"));
    }

    private static Command edit(String cmd) {
        return new EclipseCommand("org.eclipse.cdt.ui.edit." + cmd);
    }
    
    private static Command editC(String cmd) {
        return edit("text.c." + cmd);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        Command gotoDecl = seq(edit("opendecl"), DeselectAllCommand.INSTANCE); // NOTE: deselect won't work in other editor
        return union(
                state(leafCtrlBind(']', gotoDecl)),
                prefixedOperatorCmds('g', 'c', seq(editC("toggle.comment"), DeselectAllCommand.INSTANCE), textObjects),
                operatorCmds('=', seq(editC("indent"), DeselectAllCommand.INSTANCE), textObjects));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
        return state(leafBind('=', seq(editC("indent"), LeaveVisualModeCommand.INSTANCE)));
    }
}
