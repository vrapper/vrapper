package net.sourceforge.vrapper.eclipse.pydev.keymap;

import static net.sourceforge.vrapper.vim.commands.CommandWrappers.dontRepeat;
import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorKeyMap;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.commands.ToggleFoldingCommand;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class PyDevSpecificStateProvider extends AbstractEclipseSpecificStateProvider {
	
    public PyDevSpecificStateProvider() {
        addFormatCommands(action("pyFormatStd"));
    }

    private static Command action(String cmd) {
        return new EclipseCommand("org.python.pydev.editor.actions." + cmd);
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = textObjectProvider.textObjects();
        Command toggleCommand = new ToggleFoldingCommand(
                "org.python.pydev.editor.actions.navigation.pyUnCollapse",
                "org.python.pydev.editor.actions.navigation.pyCollapse");

        return union(
                state(
                    transitionBind('z',
                            leafBind('a', toggleCommand),
                            leafBind('o', dontRepeat(action("navigation.pyUnCollapse"))),
                            leafBind('R', dontRepeat(action("navigation.pyUnCollapseAll"))),
                            leafBind('c', dontRepeat(action("navigation.pyCollapse"))),
                            leafBind('M', dontRepeat(action("navigation.pyCollapseAll")))),
                    leafCtrlBind(']', gotoDecl()),
                    transitionBind('g',
                            leafBind('d', gotoDecl()),
                            leafBind('D', gotoDecl()),
                            leafBind('R', (Command)new EclipseCommand("org.python.pydev.refactoring.ui.actions.RenameCommand")))
                ),
                prefixedOperatorCmds('g', 'c', actionAndDeselect("togglecomment"), textObjects)
                );
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<KeyMapInfo> normalModeKeymap() {
        return state(
                transitionBind('g', operatorKeyMap('c')));
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
        return state(
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
