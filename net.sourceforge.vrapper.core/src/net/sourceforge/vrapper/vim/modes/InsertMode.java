package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;
public class InsertMode implements EditorMode {

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

    public String getName() {
        return NAME;
    }

    public void enterMode() {
        if (isEnabled) {
            return;
        }
        isEnabled = true;
        if (!inChange || CHANGES_ARE_ATOMIC) {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
        }
        editorAdaptor.getCursorService().setCaret(CaretType.STANDARD);
        startEditPosition = editorAdaptor.getCursorService().getPosition();
    }

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

    public boolean handleKey(KeyStroke stroke) {
        if (stroke.equals(key(SpecialKey.ESC)) || stroke.equals(key(KeyStroke.CTRL, '['))) {
            editorAdaptor.changeMode(NormalMode.NAME);
            return true;
        }
        else if (!allowed(stroke)) {
            startEditPosition = editorAdaptor.getCursorService().getPosition();
        }
        return false;
    }


    private boolean allowed(KeyStroke stroke) {
        // TODO: option to allow arrows
        if (stroke.getSpecialKey() != null) {
            return false;
        }
        if ((stroke.getModifiers() & KeyStroke.CTRL) == 0) {
            return true; // FIXME: look one line below
        }
        //			return Character.isLetterOrDigit(event.character); // FIXME: no enter, no backspace
        return false;
    }

}
