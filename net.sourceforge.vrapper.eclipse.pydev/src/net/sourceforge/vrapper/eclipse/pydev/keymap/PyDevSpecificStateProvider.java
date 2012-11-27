package net.sourceforge.vrapper.eclipse.pydev.keymap;

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
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class PyDevSpecificStateProvider extends AbstractEclipseSpecificStateProvider {
	
    public PyDevSpecificStateProvider() {
        //addFormatCommands(action("format"));
    }

    private static Command action(String cmd) {
        return new EclipseCommand("org.python.pydev.editor.actions." + cmd);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        return union(
                state(
            		leafCtrlBind(']', gotoDecl()),
	                transitionBind('g',
	                        leafBind('d', gotoDecl()),
	                        leafBind('D', gotoDecl()),
	                        leafBind('R', (Command)new EclipseCommand("org.python.pydev.refactoring.ui.actions.RenameCommand")))
                ),
                prefixedOperatorCmds('g', 'c', actionAndDeselect("togglecomment"), textObjects)
                //operatorCmds('=', action("pyFormatStd"), textObjects)
                );
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
        return state(
        		//leafBind('=', actionAndLeaveVisual("pyFormatStd")),
        		transitionBind('g',
    				leafBind('c', actionAndLeaveVisual("togglecomment")) )
        		);
    }
    
    private static Command gotoDecl() {
    	return seq(new SetMarkCommand(CursorService.LAST_JUMP_MARK), 
    			new EclipseCommand("org.python.pydev.editor.actions.navigation.pyGoToDefinition"),
    			DeselectAllCommand.INSTANCE);
    }
    
    private static Command actionAndDeselect(String cmd) {
    	return seq(action(cmd), DeselectAllCommand.INSTANCE);
    }
    
    private static Command actionAndLeaveVisual(String cmd) {
    	return seq(action(cmd), LeaveVisualModeCommand.INSTANCE);
    }

}
