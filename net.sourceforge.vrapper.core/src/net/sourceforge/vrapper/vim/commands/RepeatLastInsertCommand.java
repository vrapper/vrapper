package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.register.RegisterContent;

public class RepeatLastInsertCommand extends CountAwareCommand {

	private final Motion motion;

	public RepeatLastInsertCommand(Motion motion) {
		this.motion = motion;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, int count) {
		doIt(editorAdaptor, motion, count);
	}

	@Override
	public CountAwareCommand repetition() {
		return this;
	}

	public static void doIt(EditorAdaptor editorAdaptor, Motion motion, int count) {
		if (motion != null)
			MotionCommand.doIt(editorAdaptor, motion);
		Position position = editorAdaptor.getCursorService().getPosition();
		RegisterContent registerContent = editorAdaptor.getRegisterManager().getLastEditRegister().getContent();
		if (registerContent != null) {
			String text = StringUtils.multiply(registerContent.getText(), count);
			editorAdaptor.getModelContent().replace(position.getModelOffset(), 0, text);
			int textLength = Math.max(0, text.length() - 1);
			// cursor position may've changed since beginning of this method,
			// e.g. when we do it during renaming refactoring
			Position newPosition = editorAdaptor.getCursorService().getPosition().addModelOffset(textLength);
			editorAdaptor.getCursorService().setPosition(newPosition, true);
		} else
			VrapperLog.error("no last edit register");
	}

}
