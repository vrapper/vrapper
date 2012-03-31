package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.InsertAdjacentCharacter;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.PasteRegisterCommand;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class InsertMode extends AbstractMode {

    public static final String NAME = "insert mode";
    public static final String DISPLAY_NAME = "INSERT";
    public static final String KEYMAP_NAME = "Insert Mode Keymap";
    public static final ModeSwitchHint DONT_MOVE_CURSOR = new ModeSwitchHint() {};
    public static final ModeSwitchHint DONT_SAVE_STATE = new ModeSwitchHint() {};
    public static final KeyStroke ESC = key(SpecialKey.ESC);
    public static final KeyStroke CTRL_C = ctrlKey('c');
    public static final KeyStroke CTRL_R = ctrlKey('r');
    public static final KeyStroke CTRL_A = ctrlKey('a');
    public static final KeyStroke CTRL_E = ctrlKey('e');
    public static final KeyStroke CTRL_Y = ctrlKey('y');

    private Position startEditPosition;

    /**
     * Command to be used before insertion
     */
    private Command command;
    private int count;

    public InsertMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    public String getName() {
        return NAME;
    }
    
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * @param args
     *            command to perform on entering insert mode
     * @throws CommandExecutionException
     */
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
    	boolean initMode = true;
        for (ModeSwitchHint hint: args) {
        	if(hint == DONT_SAVE_STATE) {
        		initMode = false;
        	}
        }
        if (initMode && editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
        }

        count = 1;
        command = null;

        try {
            editorAdaptor.getViewportService().setRepaint(false);

            for (ModeSwitchHint hint : args) {
                if (hint instanceof WithCountHint) {
                    WithCountHint cast = (WithCountHint) hint;
                    count = cast.getCount();
                }
                if (hint instanceof ExecuteCommandHint) {
                    ExecuteCommandHint cast = (ExecuteCommandHint) hint;
                    cast.getCommand().execute(editorAdaptor);
                }
            }
        } catch (CommandExecutionException e) {
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock();
                editorAdaptor.getHistory().endCompoundChange();
            }
            throw e;
        } finally {
            editorAdaptor.getViewportService().setRepaint(true);
        }
        editorAdaptor.getCursorService().setCaret(CaretType.VERTICAL_BAR);
        if(initMode) {
        	startEditPosition = editorAdaptor.getCursorService().getPosition();
        }
        super.enterMode(args);
    }

    public void leaveMode(ModeSwitchHint... hints) {
        boolean moveCursor = true;
        for (ModeSwitchHint hint: hints) {
            if (hint == InsertMode.DONT_MOVE_CURSOR) {
                moveCursor = false;
            }
            else if(hint == InsertMode.DONT_SAVE_STATE) {
            	//Leave insert mode without performing any of our "leave" operations.
            	//This is because we'll be returning to InsertMode soon and we want
            	//everything to be considered a single "insert" operation.
            	return;
            }
        }
        try {
            saveTypedText();
            try {
                if (moveCursor)
                    MotionCommand.doIt(editorAdaptor, MoveLeft.INSTANCE);
            } catch (CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        e.getMessage());
            }
            repeatInsert();
        } finally {
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock();
                editorAdaptor.getHistory().endCompoundChange();
            }
        }
        editorAdaptor.getCursorService().setMark(
                CursorService.LAST_INSERT_MARK, editorAdaptor.getPosition());
    }

    private void repeatInsert() {
        if (count > 1) {
            try {
                editorAdaptor.getRegisterManager().getLastEdit().withCount(
                        count - 1).execute(editorAdaptor);
            } catch (CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        e.getMessage());
            }
        }
    }

    private void saveTypedText() {
        Register lastEditRegister = editorAdaptor.getRegisterManager().getLastEditRegister();
        TextContent content = editorAdaptor.getModelContent();
        Position position = editorAdaptor.getCursorService().getPosition();
        String text = content.getText(new StartEndTextRange(startEditPosition, position));
        RegisterContent registerContent = new StringRegisterContent(ContentType.TEXT, text);
        lastEditRegister.setContent(registerContent);
        Command repetition = createRepetition(lastEditRegister, text);
        editorAdaptor.getRegisterManager().setLastInsertion(
                count > 1 ? repetition.withCount(count) : repetition);
    }

    protected Command createRepetition(Register lastEditRegister, String text) {
        Command repetition = null;
        if (command != null)
            repetition = command.repetition();
        return dontRepeat(seq(
                repetition,
                new SwitchRegisterCommand(lastEditRegister),
                PasteBeforeCommand.CURSOR_ON_TEXT
        ));
    }

    public boolean handleKey(KeyStroke stroke) {
		if (stroke.equals(ESC) || stroke.equals(CTRL_C)) {
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            if (editorAdaptor.getConfiguration().get(Options.IM_DISABLE)) {
            	editorAdaptor.getEditorSettings().disableInputMethod();
            }
            return true;
		} else if (stroke.equals(CTRL_R)) {
			//move to "paste register" mode, but don't actually perform the
			//"leave insert mode" operations
			editorAdaptor.changeModeSafely(PasteRegisterMode.NAME, DONT_SAVE_STATE);
		} else if (stroke.equals(CTRL_A)) {
			executeCommand(PasteRegisterCommand.PASTE_LAST_INSERT);
		} else if (stroke.equals(CTRL_E)) {
			executeCommand(InsertAdjacentCharacter.LINE_BELOW);
		} else if (stroke.equals(CTRL_Y)) {
			executeCommand(InsertAdjacentCharacter.LINE_ABOVE);
        } else if (!allowed(stroke)) {
            startEditPosition = editorAdaptor.getCursorService().getPosition();
            count = 1;
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock();
                editorAdaptor.getHistory().endCompoundChange();
                editorAdaptor.getHistory().beginCompoundChange();
                editorAdaptor.getHistory().lock();
            }
        } else if (stroke.isVirtual()) {
            // stroke was generated by Vrapper, it will not be passed to the
            // editor
            handleVirtualStroke(stroke);
            return true;
        }
        return false;
    }

    private void handleVirtualStroke(KeyStroke stroke) {
        TextContent c = editorAdaptor.getModelContent();
        if (SpecialKey.BACKSPACE.equals(stroke.getSpecialKey())) {
            int pos = editorAdaptor.getPosition().getModelOffset();
            int pos2;
            LineInformation line = c.getLineInformationOfOffset(pos);
            if (pos > 0) {
                if (pos > line.getBeginOffset()) {
                    pos2 = pos - 1;
                } else {
                    pos2 = c.getLineInformation(line.getNumber() - 1)
                            .getEndOffset();
                }
                c.replace(pos2, pos - pos2, "");
                editorAdaptor.setPosition(editorAdaptor.getCursorService()
                        .newPositionForModelOffset(pos2), false);
            }
        } else {
            String s;
            if (SpecialKey.RETURN.equals(stroke.getSpecialKey())) {
                s = editorAdaptor.getConfiguration().getNewLine();
            } else {
                s = String.valueOf(stroke.getCharacter());
            }
            c.smartInsert(s);
        }
    }

    private boolean allowed(KeyStroke stroke) {
        // TODO: option to allow arrows
        SpecialKey specialKey = stroke.getSpecialKey();
        if (specialKey != null) {
            return VimConstants.SPECIAL_KEYS_ALLOWED_FOR_INSERT
                    .contains(specialKey);
        }
        return true;
    }
    
    //just a convenience method to reduce duplicated code
    private void executeCommand(Command command) {
    	try {
    		command.execute(editorAdaptor);
    	} catch (CommandExecutionException e) {
    		editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
    	}
    }

    public KeyMap resolveKeyMap(KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
    }

    public static class MoveRightOverLineBreak extends
            CountIgnoringNonRepeatableCommand {

        private final int offset;

        public MoveRightOverLineBreak(int offset) {
            this.offset = offset;
        }

        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            editorAdaptor.setPosition(editorAdaptor.getPosition()
                    .addModelOffset(offset), true);
        }
    }

    public static class SimpleInsertCommandSequence extends VimCommandSequence {

        public SimpleInsertCommandSequence(Command... commands) {
            super(commands);
        }

        @Override
        public Command withCount(final int count) {
            return new CountIgnoringNonRepeatableCommand() {

                public void execute(EditorAdaptor editorAdaptor)
                        throws CommandExecutionException {
                    SimpleInsertCommandSequence.this.execute(editorAdaptor);
                    for (int i = 1; i < count; i++) {
                        Position pos = editorAdaptor.getPosition();
                        editorAdaptor.setPosition(pos.addModelOffset(1), false);
                        SimpleInsertCommandSequence.this.execute(editorAdaptor);
                    }
                }
            };
        }

    }
}
