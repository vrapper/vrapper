
package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class InsertMode extends AbstractMode {

    public static final String NAME = "insert mode";
    public static final String KEYMAP_NAME = "Insert Mode Keymap";
    // FIXME: change this to option some day
    public static final boolean CHANGES_ARE_ATOMIC = false;

    private Position startEditPosition;

    public InsertMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public String getName() {
        return NAME;
    }

    /**
     * @param args motion to perform on entering insert mode
     */
    public void enterMode(Object... args) {
        if (isEnabled) {
            return;
        }
        if (args.length > 0) {
            Motion m = (Motion) args[0];
            try {
                MotionCommand.doIt(editorAdaptor, m);
            } catch (CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
        }
        isEnabled = true;
        if (CHANGES_ARE_ATOMIC) {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
        }
        editorAdaptor.getCursorService().setCaret(CaretType.VERTICAL_BAR);
        startEditPosition = editorAdaptor.getCursorService().getPosition();
    }

    public void leaveMode() {
        isEnabled = false;
        saveTypedText();
        try {
            MotionCommand.doIt(editorAdaptor, new MoveLeft());
        } catch (CommandExecutionException e) {
            editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
        }
        if (CHANGES_ARE_ATOMIC) {
            editorAdaptor.getHistory().unlock();
            editorAdaptor.getHistory().endCompoundChange();
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

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }

}
