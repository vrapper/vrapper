package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
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
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.InsertAdjacentCharacter;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.PasteRegisterCommand;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
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
    public static final ModeSwitchHint DONT_SAVE_STATE = new ModeSwitchHint() {};
    public static final KeyStroke ESC = key(SpecialKey.ESC);
    public static final KeyStroke BACKSPACE = key(SpecialKey.BACKSPACE);
    public static final KeyStroke CTRL_C = ctrlKey('c');
    public static final KeyStroke CTRL_R = ctrlKey('r');
    
    protected State<Command> currentState = buildState();

    private Position startEditPosition;

    /**
     * Command to be used before insertion
     */
    private Command command;
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
    	boolean initMode = true;
    	boolean lockHistory = true;
        for (final ModeSwitchHint hint: args) {
        	if(hint == DONT_SAVE_STATE) {
        		initMode = false;
        	}
        	if(hint == DONT_LOCK_HISTORY) {
        		lockHistory = false;
        	}
        }
        if (initMode && lockHistory && editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
            editorAdaptor.getHistory().beginCompoundChange();
            editorAdaptor.getHistory().lock();
        }

        count = 1;
        command = null;
        mOnLeaveHint = null;

        try {
            editorAdaptor.getViewportService().setRepaint(false);

            for (final ModeSwitchHint hint : args) {
                if (hint instanceof WithCountHint) {
                    final WithCountHint cast = (WithCountHint) hint;
                    count = cast.getCount();
                }
                
                final boolean isOnLeave = hint instanceof ExecuteCommandHint.OnLeave;
                if (hint instanceof ExecuteCommandHint
                        && !isOnLeave) { // this SEEMS correct...?
                    final ExecuteCommandHint cast = (ExecuteCommandHint) hint;
                    cast.getCommand().execute(editorAdaptor);
                } else if (isOnLeave) {
                    mOnLeaveHint = (ExecuteCommandHint) hint;
                }
            }
        } catch (final CommandExecutionException e) {
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

    @Override
    public void leaveMode(final ModeSwitchHint... hints) {
        boolean moveCursor = true;
        for (final ModeSwitchHint hint: hints) {
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
            } catch (final CommandExecutionException e) {
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
            } catch (final CommandExecutionException e) {
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        e.getMessage());
            }
        }
    }

    private void saveTypedText() {
        final Register lastEditRegister = editorAdaptor.getRegisterManager().getLastEditRegister();
        final TextContent content = editorAdaptor.getModelContent();
        final Position position = editorAdaptor.getCursorService().getPosition();
        if(startEditPosition.getModelOffset() > editorAdaptor.getModelContent().getTextLength()) {
        	//if the file is shorter than where we started,
        	//update so we aren't at an invalid position
        	startEditPosition = editorAdaptor.getCursorService().newPositionForModelOffset(
				        			editorAdaptor.getModelContent().getTextLength()
			        			);
        }
        final String text = content.getText(new StartEndTextRange(startEditPosition, position));
        final RegisterContent registerContent = new StringRegisterContent(ContentType.TEXT, text);
        lastEditRegister.setContent(registerContent);
        final Command repetition = createRepetition(lastEditRegister, text);
        editorAdaptor.getRegisterManager().setLastInsertion(
                count > 1 ? repetition.withCount(count) : repetition);
    }

    protected Command createRepetition(final Register lastEditRegister, final String text) {
        Command repetition = null;
        if (command != null)
            repetition = command.repetition();
        return dontRepeat(seq(
                repetition,
                new SwitchRegisterCommand(lastEditRegister),
                PasteBeforeCommand.CURSOR_ON_TEXT
        ));
    }

    @Override
    public boolean handleKey(final KeyStroke stroke) {
        final Transition<Command> transition = currentState.press(stroke);
        if (transition != null && transition.getValue() != null) {
        	try {
        		transition.getValue().execute(editorAdaptor);
        	} catch (final CommandExecutionException e) {
        		editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
        	}
        }
        else if (stroke.equals(ESC) || stroke.equals(CTRL_C)) {
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            if (editorAdaptor.getConfiguration().get(Options.IM_DISABLE)) {
            	editorAdaptor.getEditorSettings().disableInputMethod();
            }
            if (mOnLeaveHint != null && stroke.equals(ESC)) {
                // ctrl+c is "cancel," it seems
                try {
                    mOnLeaveHint.getCommand().execute(editorAdaptor);
                } catch (final CommandExecutionException e) {
                    editorAdaptor.getUserInterfaceService().setErrorMessage(
                            e.getMessage());
                }
            }
            return true;
		} else if (stroke.equals(CTRL_R)) {
			//move to "paste register" mode, but don't actually perform the
			//"leave insert mode" operations
			editorAdaptor.changeModeSafely(PasteRegisterMode.NAME, DONT_SAVE_STATE);
        } else if (!allowed(stroke)) {
            startEditPosition = editorAdaptor.getCursorService().getPosition();
            count = 1;
            if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
                editorAdaptor.getHistory().unlock();
                editorAdaptor.getHistory().endCompoundChange();
                editorAdaptor.getHistory().beginCompoundChange();
                editorAdaptor.getHistory().lock();
            }
        } else if (stroke.equals(BACKSPACE) 
        		&& editorAdaptor.getConfiguration().get(Options.SOFT_TAB) > 1) {
        	//soft tab stop is enabled, check to see if there are spaces
        	return softTabDelete();
        } else if (stroke.isVirtual()) {
            // stroke was generated by Vrapper, it will not be passed to the
            // editor
            handleVirtualStroke(stroke);
            return true;
        }
        return false;
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
    			.newPositionForModelOffset(pos - toDelete), false);
    	
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

    private boolean allowed(final KeyStroke stroke) {
        // TODO: option to allow arrows
        final SpecialKey specialKey = stroke.getSpecialKey();
        if (specialKey != null) {
            return VimConstants.SPECIAL_KEYS_ALLOWED_FOR_INSERT
                    .contains(specialKey);
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
            		leafCtrlBind('w', (Command)new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, new MotionTextObject(MoveWordLeft.INSTANCE))),
            		leafCtrlBind('a', (Command)PasteRegisterCommand.PASTE_LAST_INSERT),
            		leafCtrlBind('e', (Command)InsertAdjacentCharacter.LINE_BELOW),
            		leafCtrlBind('y', (Command)InsertAdjacentCharacter.LINE_ABOVE)
            )
        ));
    }

    @Override
    public KeyMap resolveKeyMap(final KeyMapProvider provider) {
        return provider.getKeyMap(KEYMAP_NAME);
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
            editorAdaptor.setPosition(editorAdaptor.getPosition()
                    .addModelOffset(offset), true);
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
                        editorAdaptor.setPosition(pos.addModelOffset(1), false);
                        SimpleInsertCommandSequence.this.execute(editorAdaptor);
                    }
                }
            };
        }

    }
}
