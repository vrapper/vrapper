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
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class JdtSpecificStateProvider extends AbstractEclipseSpecificStateProvider {

	@SuppressWarnings("unchecked")
	private static State<Command> getGSomethingBindings() {
		return state(
				leafBind('r', editJava("refactor.quickMenu")),
				leafBind('m', editJava("source.quickMenu")), // gs/gS should be taken by swap plug-in
				leafBind('R', editJava("rename.element")));
	}
    
    public JdtSpecificStateProvider() {
        addFormatCommands(editJava("format"));
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<TextObject> textObjects = NormalMode.textObjects();
        Command gotoDecl = editJavaAndDeselect("open.editor"); // NOTE: deselect won't work in other editor
		return union(
                state(
                		leafCtrlBind(']', gotoDecl),
                		transitionBind('g', getGSomethingBindings())),
                prefixedOperatorCmds('g', 'c', editJavaAndDeselect("toggle.comment"), textObjects),
                operatorCmds('=', editJavaAndDeselect("indent"), textObjects));
    }
    
	@Override
	@SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
    	return state(
    			leafBind('=', editJavaAndLeaveVisual("indent")),
    			// FIXME: there is a WTF with Eclipse and select.* commands
    			// they make selection "special", we need to have a way of using
    			// it. Try VLLLd - it's quite broken now :-/
    			leafBind('m', editJava("select.enclosing")),
    			leafBind('M', editJava("select.last")),
    			leafBind('L', editJava("select.next")), 
    			leafBind('H', editJava("select.previous")),
    			transitionBind('g', union(
    					state(leafBind('c', editJavaAndLeaveVisual("toggle.comment"))),
    					getGSomethingBindings()
    			)));
    }

	protected static Command editJava(String cmd) {
        return new EclipseCommand("org.eclipse.jdt.ui.edit.text.java." + cmd);
    }
	
	private static Command editJavaAndLeaveVisual(String cmd) {
		return seq(editJava(cmd), LeaveVisualModeCommand.INSTANCE);
	}
    
    protected static Command editJavaAndDeselect(String cmd) {
    	return seq(editJava(cmd), DeselectAllCommand.INSTANCE);
    }
    
    @Override
    public String getFileType() {
        return "Java";
    }

}
