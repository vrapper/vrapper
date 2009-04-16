package kg.totality.core.modes;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.MotionCommand;
import kg.totality.core.commands.motions.MoveLeft;
import kg.totality.core.keymap.SWTKeyStroke;
import kg.totality.core.utils.CaretType;
import kg.totality.core.utils.ContentType;
import newpackage.glue.TextContent;
import newpackage.position.Position;
import newpackage.position.StartEndTextRange;
import newpackage.position.TextRange;
import newpackage.vim.register.Register;
import newpackage.vim.register.RegisterContent;
import newpackage.vim.register.StringRegisterContent;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;

public class InsertMode implements EditorMode, VerifyKeyListener {

	// FIXME: change this to option some day
	public static final boolean CHANGES_ARE_ATOMIC = false;
	// FIXME: change this to something saner some day
	public static boolean inChange = false;

	public static final String NAME = "insert mode";
	private boolean isEnabled;
	private final EditorAdaptor editorAdaptor;
	private Position startEditPosition;

	public InsertMode(EditorAdaptor editorAdaptor) {
		this.editorAdaptor = editorAdaptor; }

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void enterMode() {
		if (isEnabled) return;
		isEnabled = true;
		if (!inChange || CHANGES_ARE_ATOMIC) {
			editorAdaptor.getHistory().beginCompoundChange();
			editorAdaptor.getHistory().lock();
		}
		editorAdaptor.getCursorService().setCaret(CaretType.STANDARD);
		startEditPosition = editorAdaptor.getCursorService().getPosition();
	}

	@Override
	public void leaveMode() {
		isEnabled = false;
		saveTypedText();
		MotionCommand.doIt(editorAdaptor, new MoveLeft());
		if (inChange || CHANGES_ARE_ATOMIC) {
			editorAdaptor.getHistory().unlock();
			editorAdaptor.getHistory().endCompoundChange();
			inChange = false;
		}
	}

	private void saveTypedText() {
		Register lastEditRegister = editorAdaptor.getRegisterManager().getLastEditRegister();
		TextContent content = editorAdaptor.getModelContent();
		Position position = editorAdaptor.getCursorService().getPosition();
		TextRange editRange = new StartEndTextRange(startEditPosition, position);
		String text = content.getText(editRange.getLeftBound().getModelOffset(), editRange.getViewLength());
		RegisterContent registerContent = new StringRegisterContent(ContentType.TEXT, text);
		lastEditRegister.setContent(registerContent);
	}

	@Override
	public void verifyKey(VerifyEvent event) {
		SWTKeyStroke keyStroke = new SWTKeyStroke(event);
		if (!keyStroke.isComplete())
			return;
		if (keyStroke.equals(new SWTKeyStroke("Esc")) || keyStroke.equals(new SWTKeyStroke("CTRL+[")))
			editorAdaptor.changeMode(NormalMode.NAME);
		else if (!allowed(event))
			startEditPosition = editorAdaptor.getCursorService().getPosition();
	}

	private boolean allowed(VerifyEvent event) {
		if ((event.stateMask & (SWT.CTRL | SWT.ALT)) == 0)
			return true; // FIXME: look one line below
//			return Character.isLetterOrDigit(event.character); // FIXME: no enter, no backspace
		return false;
	}

	@Override
	public VerifyKeyListener getKeyListener() {
		return this;
	}

}
