package net.sourceforge.vrapper.vim.modes;

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
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;

public class InsertMode extends AbstractMode {

    public static class WithCountHint implements ModeSwitchHint {
        private final int count;

        public WithCountHint(int count) {
            this.count = count;
        }

        public int getCount() {
            return count;
        }
    }

    public static final String NAME = "insert mode";
    public static final String KEYMAP_NAME = "Insert Mode Keymap";
    public static final ModeSwitchHint DONT_MOVE_CURSOR = new ModeSwitchHint() {};

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

    /**
     * @param args
     *            command to perform on entering insert mode
     * @throws CommandExecutionException
     */
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        if (editorAdaptor.getConfiguration().get(Options.ATOMIC_INSERT)) {
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
                if (hint instanceof ExecuteCommandOnEnterHint) {
                    ExecuteCommandOnEnterHint cast = (ExecuteCommandOnEnterHint) hint;
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
        startEditPosition = editorAdaptor.getCursorService().getPosition();
        super.enterMode(args);
    }

    public void leaveMode(ModeSwitchHint... hints) {
        boolean moveCursor = true;
        for (ModeSwitchHint hint: hints)
            if (hint == InsertMode.DONT_MOVE_CURSOR)
                moveCursor = false;
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
                PasteBeforeCommand.INSTANCE,
                new MoveRightOverLineBreak(text.length() - 1)
        ));
    }

    public boolean handleKey(KeyStroke stroke) {
        if (stroke.equals(key(SpecialKey.ESC))) {
            editorAdaptor.changeModeSafely(NormalMode.NAME);
            PlatformUI.getWorkbench()  
                .getActiveWorkbenchWindow()
                .getShell().setImeInputMode(SWT.NULL);
            return true;
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
