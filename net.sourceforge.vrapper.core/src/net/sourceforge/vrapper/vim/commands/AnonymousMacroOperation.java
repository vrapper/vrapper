package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Immediately execute a set of commands without storing them
 * in a named register. Backs the <code>normal</code> command.
 */
public class AnonymousMacroOperation extends AbstractLinewiseOperation {
	
	private String macro;
	
	public AnonymousMacroOperation(String macro) {
		this.macro = macro;
	}

	public TextOperation repetition() {
		return null;
	}

	@Override
	public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
			throws CommandExecutionException {
		return SimpleLineRange.singleLine(editorAdaptor, currentPos);
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, LineRange lineRange)
			throws CommandExecutionException {
		CursorService cursor = editorAdaptor.getCursorService();
		TextContent model = editorAdaptor.getModelContent();
		
		Iterable<KeyStroke> parsed = ConstructorWrappers.parseKeyStrokes(macro);
		
		boolean resetPos = true;
		if (lineRange.getStartLine() == lineRange.getEndLine()) {
			//put cursor at 'from' position when it's run on a single line.
			cursor.setPosition(lineRange.getFrom(), StickyColumnPolicy.NEVER);
			resetPos = false;
		}
		
		// [NOTE] This is not safe when doing :<range>normal dd. ExCommandOperation (class behind
		// the :g/<pattern>/ command) has code which guards against this, so users should prefer
		// that one for destructive operations.
		for (int i = lineRange.getStartLine(); i <= lineRange.getEndLine(); i++) {

			if (resetPos) {
				LineInformation lineInfo = model.getLineInformation(i);
				Position lineStart = cursor.newPositionForModelOffset(lineInfo.getBeginOffset());
				editorAdaptor.setPosition(lineStart, StickyColumnPolicy.NEVER);
			}

			ViewportService view = editorAdaptor.getViewportService();
			try {
				view.setRepaint(false);
				view.lockRepaint(this);
				editorAdaptor.getHistory().beginCompoundChange();
				editorAdaptor.getHistory().lock("normal-command");
				for (KeyStroke key : parsed) {
					editorAdaptor.handleKeyOffRecord(key);
				}
			} finally {
				editorAdaptor.getHistory().unlock("normal-command");
				editorAdaptor.getHistory().endCompoundChange();
				view.unlockRepaint(this);
				view.setRepaint(true);
			}

			if ( ! NormalMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
				editorAdaptor.changeModeSafely(NormalMode.NAME);
			}
		}
	}

}
