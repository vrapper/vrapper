package kg.totality.core.commands;

import de.jroene.vrapper.eclipse.VrapperPlugin;
import newpackage.position.Position;
import newpackage.utils.StringUtils;
import newpackage.vim.register.RegisterContent;
import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.Motion;

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
			VrapperPlugin.error("no last edit register");
	}

}
