package net.sourceforge.vrapper.eclipse.cdt.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorKeyMap;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;

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
        State<TextObject> textObjects = super.textObjectProvider.textObjects();
        return union(
                state(
            		leafCtrlBind(']', gotoDecl()),
	                transitionBind('g',
	                        leafBind('d', gotoDecl()),
	                        leafBind('D', gotoDecl()),
	                        leafBind('R', edit("text.rename.element")))
                ),
                prefixedOperatorCmds('g', 'c', editCAndDeselect("toggle.comment"), textObjects),
                operatorCmds('=', editCAndDeselect("indent"), textObjects));
    }

    @Override
    protected State<String> normalModeKeymap() {
        @SuppressWarnings("unchecked")
        State<String> state = state(
                transitionBind('g', operatorKeyMap('c')),
                operatorKeyMap('='));
        return state;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
        return state(
        		leafBind('=', editCAndLeaveVisual("indent")),
        		transitionBind('g',
    				leafBind('c', editCAndLeaveVisual("toggle.comment")) )
        		);
    }
    
    private static Command gotoDecl() {
    	// NOTE: deselect won't work in other editor
    	return seq(new SetMarkCommand(CursorService.LAST_JUMP_MARK), edit("opendecl"), DeselectAllCommand.INSTANCE);
    }
	
	private static Command editCAndLeaveVisual(String cmd) {
		return seq(editC(cmd), LeaveVisualModeCommand.INSTANCE);
	}
	
    private static Command editCAndDeselect(String cmd) {
    	return seq(editC(cmd), DeselectAllCommand.INSTANCE);
    }
}
