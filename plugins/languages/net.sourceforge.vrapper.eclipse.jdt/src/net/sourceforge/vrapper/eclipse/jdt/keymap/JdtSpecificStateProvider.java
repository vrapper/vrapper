package net.sourceforge.vrapper.eclipse.jdt.keymap;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorKeyMap;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.eclipse.commands.EclipseCommand;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeselectAllCommand;
import net.sourceforge.vrapper.vim.commands.LeaveVisualModeCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class JdtSpecificStateProvider extends AbstractEclipseSpecificStateProvider {

	@SuppressWarnings("unchecked")
	private static State<Command> getGSomethingBindings() {
		return state(
				leafBind('d', gotoDecl()),
				leafBind('D', gotoDecl()),
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
        State<TextObject> textObjects = textObjectProvider.textObjects();
		return union(
                state(
                		leafCtrlBind(']', gotoDecl()),
                		transitionBind('g', getGSomethingBindings())),
                prefixedOperatorCmds('g', 'c', editJavaAndDeselect("toggle.comment"), textObjects),
                operatorCmds('=', editJavaAndDeselect("indent"), textObjects));
    }
    
    @Override
    protected State<KeyMapInfo> normalModeKeymap() {
        @SuppressWarnings("unchecked")
        State<KeyMapInfo> state = state(
                transitionBind('g', operatorKeyMap('c')),
                operatorKeyMap('='));
        return state;
    }

	@Override
	@SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
    	return state(
    			leafBind('=', editJavaAndLeaveVisual("indent")),
    			// FIXME: there is a WTF with Eclipse and select.* commands
    			// they make selection "special", we need to have a way of using
    			// it. Try VLLLd - it's quite broken now :-/
    			/**
    			 * I have a better WTF... where did these features come from?
    			 * This breaks the H, M, L functionality of normal Vrapper by
    			 * overriding them.  This is confusing our users.  I'm only
    			 * commenting these lines out for now in case there is a legitimate
    			 * reason why they were added.
    			leafBind('m', editJava("select.enclosing")),
    			leafBind('M', editJava("select.last")),
    			leafBind('L', editJava("select.next")), 
    			leafBind('H', editJava("select.previous")),
    			 */
    			transitionBind('g', union(
    					state(leafBind('c', editJavaAndLeaveVisual("toggle.comment"))),
    					getGSomethingBindings()
    			)));
    }
	
	protected static Command gotoDecl() {
		// NOTE: deselect won't work in other editor
		return seq(new SetMarkCommand(CursorService.LAST_JUMP_MARK), editJavaAndDeselect("open.editor"));
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
