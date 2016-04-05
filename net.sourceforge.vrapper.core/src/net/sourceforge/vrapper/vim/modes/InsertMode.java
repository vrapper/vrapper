package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.repeat;
import static net.sourceforge.vrapper.vim.commands.CommandWrappers.seq;

import java.util.EnumSet;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.KeyStroke.Modifier;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.DummyTextObject;
import net.sourceforge.vrapper.vim.commands.InsertAdjacentCharacter;
import net.sourceforge.vrapper.vim.commands.InsertLineCommand;
import net.sourceforge.vrapper.vim.commands.InsertShiftWidth;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.PasteAfterCommand;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.PasteRegisterCommand;
import net.sourceforge.vrapper.vim.commands.RepeatInsertionCommand;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveDown;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeftAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveRightAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveUp;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.PasteRegisterMode;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class InsertMode extends AbstractMode {

    public static final String NAME = "insert mode";
    public static final String DISPLAY_NAME = "INSERT";
    public static final String KEYMAP_NAME = "Insert Mode Keymap";
    public static final ModeSwitchHint DONT_MOVE_CURSOR = new ModeSwitchHint() {};
    public static final ModeSwitchHint DONT_LOCK_HISTORY = new ModeSwitchHint() {};
    public static final ModeSwitchHint RESUME_ON_MODE_ENTER = new ModeSwitchHint() {};
    public static final KeyStroke ESC = key(SpecialKey.ESC);
    public static final KeyStroke BACKSPACE = key(SpecialKey.BACKSPACE);
    public static final KeyStroke CTRL_R = ctrlKey('r');
    public static final KeyStroke CTRL_O = ctrlKey('o');
    public static final KeyStroke CTRL_U = ctrlKey('u');
    public static final KeyStroke CTRL_W = ctrlKey('w');
    public static final KeyStroke CTRL_X = ctrlKey('x');

    protected State<Command> currentState = buildState();

    private Position startEditPosition;
    private int numCharsDeleted;
    private boolean cleanupIndent = false;
    private boolean resumeOnEnter = false;

    /**
     * Command to be used before insertion
     */
    private Command repetitionCommand;
    private int count;
    private ExecuteCommandHint mOnLeaveHint;

    public InsertMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    /**
     * @param args
     *            command to perform on entering insert mode
     * @throws CommandExecutionException
     */
    @Override
    public void enterMode(final ModeSwitchHint... args) throws CommandExecutionException {
        boolean initMode = !resumeOnEnter;
        resumeOnEnter = false;
        boolean lockHistory = true;
        for (final ModeSwitchHint hint: args) {
            if (hint == DONT_LOCK_HISTORY) {
                lockHistory = false;
            }
        }
        if (initMode && lockHistory && editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock("insertmode");
        }

        count = 1;
        repetitionCommand = null;
        mOnLeaveHint = null;

        try {
            editorAdaptor.getViewportService().setRepaint(false);

            for (final ModeSwitchHint hint : args) {
                if (hint instanceof WithCountHint) {
                    final WithCountHint cast = (WithCountHint) hint;
                    count = cast.getCount();
                }

                if (hint instanceof ExecuteCommandHint) {
                    if (hint instanceof ExecuteCommandHint.OnLeave) {
                        mOnLeaveHint = (ExecuteCommandHint) hint;
                    } else if (hint instanceof ExecuteCommandHint.OnRepeat) {
                        repetitionCommand = ((ExecuteCommandHint) hint).getCommand();
                    }
                    else { //onEnter, execute command now
                        Command command = ((ExecuteCommandHint) hint).getCommand();
                        editorAdaptor.getListeners().fireCommandAboutToExecute();
                        command.execute(editorAdaptor);
                        editorAdaptor.getListeners().fireCommandExecuted();
                        if(command instanceof InsertLineCommand && editorAdaptor.getConfiguration().get(Options.CLEAN_INDENT)) {
                            //entered insert mode via 'o' or 'O'
                            //cleanup auto-indent if nothing is entered
                            cleanupIndent = true;
                        }
                    }
                }
            }
        } catch (final CommandExecutionException e) {
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock("insertmode");
                editorAdaptor.getHistory().endCompoundChange();
            }
            throw e;
        } finally {
            editorAdaptor.getViewportService().setRepaint(true);
        }

        editorAdaptor.getEditorSettings().setReplaceMode(false);
        editorAdaptor.getCursorService().setCaret(CaretType.VERTICAL_BAR);
        if(initMode) {
            startEditPosition = editorAdaptor.getCursorService().getPosition();
            numCharsDeleted = 0;
        }
        super.enterMode(args);
    }

    @Override
    public void leaveMode(final ModeSwitchHint... hints) {
        boolean moveCursor = true;
        for (final ModeSwitchHint hint: hints) {
            if (hint == InsertMode.DONT_MOVE_CURSOR) {
                moveCursor = false;
            }
            if (hint == InsertMode.RESUME_ON_MODE_ENTER) {
                resumeOnEnter = true;
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
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        e.getMessage());
            }
            repeatInsert();
        } finally {
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock("insertmode");
                editorAdaptor.getHistory().endCompoundChange();
            }
        }
        Position lastInsertOffset = editorAdaptor.getPosition();
        if (lastInsertOffset.getModelOffset() < editorAdaptor.getModelContent().getTextLength()) {
            //Mark is placed one to the right to resume editing where we exited, except for file end
            lastInsertOffset = lastInsertOffset.addModelOffset(1);
        }
        editorAdaptor.getCursorService().setMark(CursorService.LAST_INSERT_MARK, lastInsertOffset);
    }

    private void repeatInsert() {
        if (count > 1) {
            try {
                repeat(count - 1, editorAdaptor.getRegisterManager().getLastInsertion())
                    .execute(editorAdaptor);
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        e.getMessage());
            }
        }
    }

    private void saveTypedText() {
        final Register lastEditRegister = editorAdaptor.getRegisterManager().getLastEditRegister();
        final TextContent content = editorAdaptor.getModelContent();
        Position position = editorAdaptor.getCursorService().getPosition();
        if (startEditPosition.getModelOffset() > editorAdaptor.getModelContent().getTextLength()) {
            //if the file is shorter than where we started,
            //update so we aren't at an invalid position
            startEditPosition = editorAdaptor.getCursorService().newPositionForModelOffset(
                    editorAdaptor.getModelContent().getTextLength());
        }
        else if (cleanupIndent && position.getModelOffset() == startEditPosition.getModelOffset()) {
            //if we entered InsertMode via 'o' or 'O' but didn't enter any text,
            //remove any auto-inserted indentation
            final int startOfLine = content.getLineInformationOfOffset(position.getModelOffset()).getBeginOffset();
            final String indent = content.getText(startOfLine, position.getModelOffset() - startOfLine);
            if(indent.length() > 0 && VimUtils.isBlank(indent)) {
                content.replace(startOfLine, indent.length(), "");
            }
            // Reset positions due to changed indentation. Especially important at the end of a file
            position = editorAdaptor.getPosition();
            startEditPosition = position;
        }
        //reset value in case we re-enter InsertMode
        cleanupIndent = false;

        CursorService cur = editorAdaptor.getCursorService();
        cur.setMark(CursorService.LAST_CHANGE_START, startEditPosition);
        cur.setMark(CursorService.LAST_CHANGE_END, position);

        Position editRangeStart = startEditPosition.addModelOffset(-numCharsDeleted);
        // Sanity check: clip selected text to start of document.
        if (editRangeStart.getModelOffset() < 0) {
            VrapperLog.error("NumCharsDeleted caused us to move out of bounds! Startposition: "
                    + startEditPosition + ", numCharsDeleted: " + numCharsDeleted);
            editRangeStart = editRangeStart.setModelOffset(0);
        }
        final String text = content.getText(new StartEndTextRange(editRangeStart, position));
        final RegisterContent registerContent = new StringRegisterContent(ContentType.TEXT, text);
        lastEditRegister.setContent(registerContent);
        final Command repetition = createRepetition(lastEditRegister, repetitionCommand, count, numCharsDeleted, 0);
        editorAdaptor.getRegisterManager().setLastInsertion(
                count > 1 ? repetition.withCount(count) : repetition);
    }

    /** Create a Command which repeats the last editing operation.
     * @param lastEditRegister Register which contains the last-edited span of text.
     * @param repetitionCommand extra Command which needs to be executed before inserting text.
     * @param count indication of how many times the caller wants to execute the resulting command.
     *     This decides if the edit will happen before or after the cursor.
     * @param deleteCharsToLeft how many characters to the left of the start position to delete.
     * @param deleteCharsToRight how many characters to the right of the start position to delete.
     */
    public static Command createRepetition(Register lastEditRegister, Command repetitionCommand,
            int count, int deleteCharsToLeft, int deleteCharsToRight) {
        Command repetition = null;
        if (repetitionCommand != null)
            repetition = repetitionCommand.repetition();

        Command paste;
        if(count > 1) {
            //insert mode with count
            paste = PasteAfterCommand.CURSOR_ON_TEXT;
        }
        else {
            //'.' command after insert
            paste = PasteBeforeCommand.CURSOR_ON_TEXT;
        }
        
        Command deleteCharsCmd = null;
        if (deleteCharsToLeft > 0) {
            TextObject toDelete = new MotionTextObject(MoveLeft.INSTANCE).withCount(deleteCharsToLeft);
            deleteCharsCmd = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, toDelete);
        }
        if (deleteCharsToRight > 0) {
            TextObject toDelete = new MotionTextObject(MoveLeft.INSTANCE).withCount(deleteCharsToRight);
            deleteCharsCmd = seq(deleteCharsCmd,
                    new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, toDelete));
        }
        return dontRepeat(seq(
                repetition,
                deleteCharsCmd,
                new SwitchRegisterCommand(lastEditRegister),
                paste,
                //LastEdit register is an internal affair, don't keep the register active.
                new SwitchRegisterCommand(SwitchRegisterCommand.DEFAULT_REGISTER)
                ));
    }

    @Override
    public boolean handleKey(final KeyStroke stroke) {
        if(startEditPosition.getModelOffset() - editorAdaptor.getCursorService().getPosition().getModelOffset() > numCharsDeleted) {
            numCharsDeleted = startEditPosition.getModelOffset() - editorAdaptor.getCursorService().getPosition().getModelOffset();
        }
        final Transition<Command> transition = currentState.press(stroke);
        if (transition != null && transition.getValue() != null) {
            try {
                if ( ! editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                    editorAdaptor.getListeners().fireCommandAboutToExecute();
                }
                transition.getValue().execute(editorAdaptor);
                if ( ! editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                    editorAdaptor.getListeners().fireCommandExecuted();
                }
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
            editorAdaptor.getListeners().fireStateReset(true);
            return true;
        } else if (stroke.equals(ESC)) {
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            if (editorAdaptor.getConfiguration().get(Options.IM_DISABLE)) {
                editorAdaptor.getEditorSettings().disableInputMethod();
            }
            if (mOnLeaveHint != null && stroke.equals(ESC)) {
                try {
                    editorAdaptor.getListeners().fireCommandAboutToExecute();
                    mOnLeaveHint.getCommand().execute(editorAdaptor);
                    editorAdaptor.getListeners().fireCommandExecuted();
                } catch (final CommandExecutionException e) {
                    editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
            }
            return true;
        } else if (stroke.equals(CTRL_R)) {
            // move to "paste register" mode, but don't actually perform the
            // "leave insert mode" operations
            editorAdaptor.changeModeSafely(PasteRegisterMode.NAME, RESUME_ON_MODE_ENTER);
            return true;
        } else if (stroke.equals(CTRL_O)) {
            // perform a single NormalMode command then return to InsertMode
            editorAdaptor.changeModeSafely(TempNormalMode.NAME);
            return true;
        } else if (stroke.equals(CTRL_U) || stroke.equals(CTRL_W)) {
            Motion motion;
            Position pos;
            try {
                CursorService cur = editorAdaptor.getCursorService();
                int cursorPos = cur.getPosition().getModelOffset();
                TextContent txt = editorAdaptor.getModelContent();
                int startEditPos = startEditPosition.getModelOffset();
                LineInformation line = txt.getLineInformationOfOffset(cursorPos);
                if (stroke.equals(CTRL_U)) {
                    motion = LineStartMotion.NON_WHITESPACE;
                } else {
                    motion = MoveWordLeft.INSTANCE;
                }
                pos = motion.destination(editorAdaptor);
                if (pos.getModelOffset() < line.getBeginOffset()
                        || pos.getModelOffset() == cursorPos) {
                    motion = LineStartMotion.COLUMN0;
                    pos = motion.destination(editorAdaptor);
                }
                int position = pos.getModelOffset();
                if (cursorPos == line.getBeginOffset()) {
                    position = txt.getLineInformation(line.getNumber() - 1)
                            .getEndOffset();
                } else {
                    if (cursorPos > startEditPos && position < startEditPos) {
                        position = startEditPos;
                    }
                }
                int length = cursorPos - position;
                txt.replace(position, length, "");
                if (position < startEditPos) {
                    startEditPosition = startEditPosition.setModelOffset(position);
                    numCharsDeleted = 0;
                }
            } catch (CommandExecutionException e) {
            }
            return true;
        // Check if editor is unmodifiable.
        } else if (Boolean.FALSE.equals(editorAdaptor.getConfiguration().get(Options.MODIFIABLE))
                && (stroke.getSpecialKey() == null
                    || ! VimConstants.SPECIAL_KEYS_ALLOWED_FOR_UNMODIFIABLE_INSERT
                            .contains(stroke.getSpecialKey()))) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("Cannot modify contents, " +
            		"'modifiable' is off!");
            // Mark as handled (= ignored)
            return true;
        } else if (!allowed(stroke)) {
            resetEditingSession();
        } else if (stroke.isVirtual()) {
            // stroke was generated by Vrapper, it will not be passed to the
            // editor
            handleVirtualStroke(stroke);
            return true;
        } else if (stroke.equals(BACKSPACE)
                && editorAdaptor.getConfiguration().get(Options.SOFT_TAB) > 1) {
            // soft tab stop is enabled, check to see if there are spaces
            return softTabDelete();
        }
        return false;
    }

    public void resetEditingSession() {
        startEditPosition = editorAdaptor.getCursorService().getPosition();
        numCharsDeleted = 0;
        count = 1;
        // Forget about counted "insert line" when mode is reset
        repetitionCommand = null;
        editorAdaptor.getRegisterManager().setLastEdit(new RepeatInsertionCommand(null));
        if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
            editorAdaptor.getHistory().unlock("insertmode");
            editorAdaptor.getHistory().endCompoundChange();
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock("insertmode");
        }
    }

    /**
     * If there are <softTabStop> number of spaces before the cursor
     * delete that many spaces as if it was a single tab character.
     */
    private boolean softTabDelete() {
    	final TextContent model = editorAdaptor.getModelContent();
    	final int softTabStop = editorAdaptor.getConfiguration().get(Options.SOFT_TAB);
    	final int pos = editorAdaptor.getPosition().getModelOffset();
    	final int start = model.getLineInformationOfOffset(pos).getBeginOffset();
    	//text before cursor
    	final String text = model.getText(start, pos - start);

    	//get number of consecutive spaces
    	int spaceCount = 0;
    	for(int i = text.length() -1; i >= 0; i--) {
    		if(text.charAt(i) != ' ') {
    			break;
    		}
    		spaceCount++;
    	}

    	if(spaceCount < softTabStop) {
    		//backspace key behaves as normal
    		return false;
    	}

    	int toDelete = 0;
    	if(spaceCount % softTabStop == 0) {
    		//exactly <softTabStop> space characters
    		//delete them as if they were a single tab character
    		toDelete = softTabStop;
    	}
    	else {
    		//not a <softTabStop> divisible number of spaces
    		//delete up to the closest soft tab stop
    		toDelete = spaceCount % softTabStop;
    	}

    	model.replace(pos - toDelete, toDelete, "");
    	editorAdaptor.setPosition(editorAdaptor.getCursorService()
    			.newPositionForModelOffset(pos - toDelete), StickyColumnPolicy.NEVER);

    	return true;
    }

    private void handleVirtualStroke(final KeyStroke stroke) {
        final TextContent c = editorAdaptor.getModelContent();
        if (SpecialKey.BACKSPACE.equals(stroke.getSpecialKey())) {
            final int pos = editorAdaptor.getPosition().getModelOffset();
            int pos2;
            final LineInformation line = c.getLineInformationOfOffset(pos);
            if (pos > 0) {
                if (pos > line.getBeginOffset()) {
                    pos2 = pos - 1;
                } else {
                    pos2 = c.getLineInformation(line.getNumber() - 1)
                            .getEndOffset();
                }
                c.replace(pos2, pos - pos2, "");
                editorAdaptor.setPosition(editorAdaptor.getCursorService()
                        .newPositionForModelOffset(pos2), StickyColumnPolicy.NEVER);
            }
        } else if (SpecialKey.ARROW_LEFT.equals(stroke.getSpecialKey())
                || SpecialKey.ARROW_RIGHT.equals(stroke.getSpecialKey())
                || SpecialKey.ARROW_UP.equals(stroke.getSpecialKey())
                || SpecialKey.ARROW_DOWN.equals(stroke.getSpecialKey())) {
            Motion direction;
            switch (stroke.getSpecialKey()) {
            case ARROW_LEFT:
                direction = MoveLeftAcrossLines.INSTANCE; break;
            case ARROW_RIGHT:
               direction = MoveRightAcrossLines.INSTANCE_BEHIND_CHAR; break;
            case ARROW_UP:
                direction = MoveUp.INSTANCE; break;
            case ARROW_DOWN:
                direction = MoveDown.INSTANCE; break;
            default:
                throw new RuntimeException("No matching direction!");
            }
            try {
                Position destination = direction.destination(editorAdaptor);
                editorAdaptor.setPosition(destination, direction.stickyColumnPolicy());
            } catch (CommandExecutionException e) {
                VrapperLog.error("Failed to navigate in editor", e);
            }
        } else {
            String s;
            if (SpecialKey.RETURN.equals(stroke.getSpecialKey())) {
                s = editorAdaptor.getConfiguration().getNewLine();
            } else if (SpecialKey.TAB.equals(stroke.getSpecialKey())) {
                s = "\t";
            } else {
                s = String.valueOf(stroke.getCharacter());
            }
            handleVirtualInsert(c, s);
        }
    }
    
    /**
     * This method only exists for ReplaceMode to override it. I know, I know,
     * it's a horrible hack.  I couldn't find a way for smartInsert to know when
     * Eclipse is in overwrite mode though.  So, if we're in InsertMode, perform
     * the virtual insert.  If we're in ReplaceMode, the ReplaceMode class will
     * handle the insert (by actually performing a replace).
     */
    protected void handleVirtualInsert(TextContent content, String str) {
        content.smartInsert(str);
    }

    /**
     * Check if a ' special key' should be handled by {@link #handleVirtualStroke(KeyStroke)}.
     * <p>
     * This code could be rolled into {@link #handleVirtualStroke(KeyStroke)} if really wanted,
     * though in that case it should return a boolean.
     */
    private boolean allowed(final KeyStroke stroke) {
        final SpecialKey specialKey = stroke.getSpecialKey();
        if (specialKey != null) {
            return VimConstants.SPECIAL_KEYS_ALLOWED_FOR_INSERT.contains(specialKey);
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected State<Command> buildState() {
        State<Command> platformSpecificState = editorAdaptor.getPlatformSpecificStateProvider().getState(NAME);
        if(platformSpecificState == null) {
            platformSpecificState = EmptyState.getInstance();
        }
        return RegisterState.wrap(union(
            platformSpecificState,
            state(
                    // Alt+O - temporary go into command mode
                    leafBind(new SimpleKeyStroke('o', EnumSet.of(Modifier.ALT)),
                            (Command)new ChangeModeCommand(CommandLineMode.NAME, RESUME_ON_MODE_ENTER)),
            		leafCtrlBind('a', (Command)PasteRegisterCommand.PASTE_LAST_INSERT),
            		leafCtrlBind('e', (Command)InsertAdjacentCharacter.LINE_BELOW),
            		leafCtrlBind('y', (Command)InsertAdjacentCharacter.LINE_ABOVE),
            		leafCtrlBind('t', (Command)new TextOperationTextObjectCommand(InsertShiftWidth.INSERT, new DummyTextObject(null))),
            		leafCtrlBind('d', (Command)new TextOperationTextObjectCommand(InsertShiftWidth.REMOVE, new DummyTextObject(null)))
            )
        ));
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return KEYMAP_NAME;
    }

    public static class MoveRightOverLineBreak extends
            CountIgnoringNonRepeatableCommand {

        private final int offset;

        public MoveRightOverLineBreak(final int offset) {
            this.offset = offset;
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            editorAdaptor.setPosition(editorAdaptor.getPosition().addModelOffset(offset),
                    StickyColumnPolicy.ON_CHANGE);
        }
    }

    public static class SimpleInsertCommandSequence extends VimCommandSequence {

        public SimpleInsertCommandSequence(final Command... commands) {
            super(commands);
        }

        @Override
        public Command withCount(final int count) {
            return new CountIgnoringNonRepeatableCommand() {

                @Override
                public void execute(final EditorAdaptor editorAdaptor)
                        throws CommandExecutionException {
                    SimpleInsertCommandSequence.this.execute(editorAdaptor);
                    for (int i = 1; i < count; i++) {
                        final Position pos = editorAdaptor.getPosition();
                        editorAdaptor.setPosition(pos.addModelOffset(1), StickyColumnPolicy.NEVER);
                        SimpleInsertCommandSequence.this.execute(editorAdaptor);
                    }
                }
            };
        }

    }
}
