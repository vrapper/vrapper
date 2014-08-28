package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.changeCaret;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmdsWithUpperCase;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorKeyMap;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.prefixedOperatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.KeyMapInfo;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountConsumingKeyMapState;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.AsciiCommand;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.DotCommand;
import net.sourceforge.vrapper.vim.commands.FindFileCommand;
import net.sourceforge.vrapper.vim.commands.FormatOperation;
import net.sourceforge.vrapper.vim.commands.IncrementDecrementCommand;
import net.sourceforge.vrapper.vim.commands.InsertLineCommand;
import net.sourceforge.vrapper.vim.commands.InsertShiftWidth;
import net.sourceforge.vrapper.vim.commands.JoinLinesCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.NormalLineRangeOperation;
import net.sourceforge.vrapper.vim.commands.PasteAfterCommand;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.PlaybackMacroCommand;
import net.sourceforge.vrapper.vim.commands.RecordMacroCommand;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.RepeatLastSubstitutionCommand;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.RestoreSelectionCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;
import net.sourceforge.vrapper.vim.commands.motions.GoToMarkMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRightForChange;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRightForChange;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;

public class NormalMode extends CommandBasedMode {

    public static final String KEYMAP_NAME = "Normal Mode Keymap";
    public static final String NAME = "normal mode";
    public static final String DISPLAY_NAME = "NORMAL";

    public NormalMode(final EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected KeyMapResolver buildKeyMapResolver() {
        final State<KeyMapInfo> state = union(
                state(
                    operatorKeyMap('c'),
                    operatorKeyMap('d'),
                    operatorKeyMap('y'),
                    operatorKeyMap('!'),
                    operatorKeyMap('<'),
                    operatorKeyMap('>'),
                    transitionBind('g', 
                        operatorKeyMap('q'),
                        operatorKeyMap('~'))),
                editorAdaptor.getPlatformSpecificStateProvider().getKeyMaps(NAME));
        final State<KeyMapInfo> countEater = new CountConsumingKeyMapState(
                                                        KEYMAP_NAME, "innercount", state);
        final State<KeyMapInfo> registerKeymapState = new RegisterKeymapState(KEYMAP_NAME, countEater);
        final State<KeyMapInfo> outerCountEater = new CountConsumingKeyMapState(
                                                    KEYMAP_NAME, "outercount", registerKeymapState);
        return new KeyMapResolver(outerCountEater, KEYMAP_NAME);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        final Command resumeInsertMode = new ChangeToInsertModeCommand(
                new MotionCommand(new GoToMarkMotion(false, CursorService.LAST_INSERT_MARK)));
        
        final Command visualMode = new ChangeModeCommand(VisualMode.NAME);
        final Command linewiseVisualMode = new ChangeModeCommand(LinewiseVisualMode.NAME);
        final Command blockwiseVisualMode = new ChangeModeCommand(BlockwiseVisualMode.NAME);

        final Motion moveLeft = MoveLeft.INSTANCE;
        final Motion moveRight = MoveRight.INSTANCE;
        final Motion wordEndRightForChange = MoveWordEndRightForChange.INSTANCE;
        final Motion bigWordEndRightForChange = MoveBigWORDEndRightForChange.INSTANCE;
        final Motion bol = LineStartMotion.NON_WHITESPACE;
        final Motion eol = new LineEndMotion(BorderPolicy.EXCLUSIVE);
        final Motion wholeLineEol = new LineEndMotion(BorderPolicy.LINE_WISE);

        final State<Motion> motions = motions();
        final TextObject wordForCw = new MotionTextObject(wordEndRightForChange);
        final TextObject wordForCW = new MotionTextObject(bigWordEndRightForChange);
        final TextObject toEol = new MotionTextObject(eol);
        final TextObject toEolForY = new MotionTextObject(wholeLineEol);

        final State<TextObject> textObjects = editorAdaptor.getTextObjectProvider().textObjects();
        State<TextObject> textObjectsForChange = union(
                state(
                        leafBind('w', wordForCw),
                        leafBind('W', wordForCW)),
                textObjects);
        textObjectsForChange = CountingState.wrap(textObjectsForChange);

        final TextOperation delete = DeleteOperation.INSTANCE;
        final TextOperation change = ChangeOperation.INSTANCE;
        final TextOperation yank   = YankOperation.INSTANCE;
        final TextOperation format = FormatOperation.INSTANCE;
        final Command undo = UndoCommand.INSTANCE;
        final Command redo = RedoCommand.INSTANCE;
        final Command pasteAfter  = PasteAfterCommand.CURSOR_ON_TEXT;
        final Command pasteBefore = PasteBeforeCommand.CURSOR_ON_TEXT;
        final Command pasteAfterWithG  = PasteAfterCommand.CURSOR_AFTER_TEXT;
        final Command pasteBeforeWithG = PasteBeforeCommand.CURSOR_AFTER_TEXT;
        final Command deleteNext = new TextOperationTextObjectCommand(delete, new MotionTextObject(moveRight));
        final Command deletePrevious = new TextOperationTextObjectCommand(delete, new MotionTextObject(moveLeft));
        final Command repeatLastOne = DotCommand.INSTANCE;
        final Command tildeCmd = SwapCaseCommand.INSTANCE;
        final LineEndMotion lineEndMotion = new LineEndMotion(BorderPolicy.LINE_WISE);
        final Command substituteLine = new TextOperationTextObjectCommand(change, new MotionTextObject(lineEndMotion));
        final Command substituteChar = new TextOperationTextObjectCommand(change, new MotionTextObject(moveRight));
        final Command incrementNum = IncrementDecrementCommand.INCREMENT;
        final Command decrementNum = IncrementDecrementCommand.DECREMENT;
        final Command joinLines = JoinLinesCommand.INSTANCE;
        final Command joinLinesDumbWay = JoinLinesCommand.DUMB_INSTANCE;
        final Command centerLine = CenterLineCommand.CENTER;
        final Command centerBottomLine = CenterLineCommand.BOTTOM;
        final Command centerTopLine = CenterLineCommand.TOP;
        final Command findFile = FindFileCommand.INSTANCE;
        final Command repeatSubLine = RepeatLastSubstitutionCommand.CURRENT_LINE_ONLY;
        final Command repeatSubGlobal = RepeatLastSubstitutionCommand.GLOBALLY;
        final Command saveAndClose = new VimCommandSequence(SaveCommand.INSTANCE, CloseCommand.CLOSE);

        final State<Command> motionCommands = new GoThereState(motions);
        final Command nextResult = motionCommands.press(key('n')).getValue();

        final State<Command> platformSpecificState = getPlatformSpecificState(NAME);
        return CountingState.wrap(RegisterState.wrap(CountingState.wrap(union(
                platformSpecificState,
                operatorCmdsWithUpperCase('d', delete, toEol,     textObjects),
                operatorCmdsWithUpperCase('y', yank,   toEolForY, textObjects),
                operatorCmdsWithUpperCase('c', change, toEol,     textObjectsForChange),
                operatorCmds('!', NormalLineRangeOperation.INSTANCE, textObjects),
                operatorCmds('>', InsertShiftWidth.INSERT, textObjects),
                operatorCmds('<', InsertShiftWidth.REMOVE, textObjects),
                prefixedOperatorCmds('g', 'q', format, textObjects),
                prefixedOperatorCmds('g', '~', SwapCaseCommand.TEXT_OBJECT_INSTANCE, textObjects),
                motionCommands,
                state(
                        leafBind('i', (Command) new ChangeToInsertModeCommand()),
                        leafBind('a', (Command) new ChangeToInsertModeCommand(new MotionCommand(moveRight))),
                        leafBind('I', (Command) new ChangeToInsertModeCommand(new MotionCommand(bol))),
                        leafBind('A', (Command) new ChangeToInsertModeCommand(new MotionCommand(eol))),
                        leafBind(':', (Command) new ChangeModeCommand(CommandLineMode.NAME)),
                        leafBind('?', (Command) new ChangeToSearchModeCommand(true, nextResult)),
                        leafBind('/', (Command) new ChangeToSearchModeCommand(false, nextResult)),
                        leafBind('R', (Command) new ReplaceMode.ChangeToReplaceModeCommand()),
                        leafBind('o', (Command) new ChangeToInsertModeCommand(InsertLineCommand.POST_CURSOR)),
                        leafBind('O', (Command) new ChangeToInsertModeCommand(InsertLineCommand.PRE_CURSOR)),
                        leafBind('v', seq(visualMode, AfterVisualEnterCommand.INSTANCE)),
                        leafBind('V', seq(linewiseVisualMode, AfterLinewiseVisualEnterCommand.INSTANCE)),
                        leafCtrlBind('v', seq(blockwiseVisualMode, AfterBlockwiseVisualEnterCommand.INSTANCE)),
                        leafCtrlBind('q', seq(blockwiseVisualMode, AfterBlockwiseVisualEnterCommand.INSTANCE)),
                        leafBind('p', pasteAfter),
                        leafBind('.', repeatLastOne),
                        leafBind('P', pasteBefore),
                        leafBind('x', deleteNext),
                        leafBind(SpecialKey.DELETE, deleteNext),
                        leafBind('X', deletePrevious),
                        leafBind('~', tildeCmd),
                        leafBind('S', substituteLine),
                        leafBind('s', substituteChar),
                        leafBind('J', joinLines),
                        leafBind('&', repeatSubLine),
                        transitionBind('g',
                        		leafBind('a', (Command)AsciiCommand.INSTANCE),
                                leafBind('f', findFile),
                                leafBind('&', repeatSubGlobal),
                                leafBind('i', resumeInsertMode),
                                leafBind('J', joinLinesDumbWay),
                                leafBind('p', pasteAfterWithG),
                                leafBind('P', pasteBeforeWithG),
                                leafBind('v', RestoreSelectionCommand.INSTANCE)),
                        transitionBind('q',
                                convertKeyStroke(
                                        RecordMacroCommand.KEYSTROKE_CONVERTER,
                                        VimConstants.PRINTABLE_KEYSTROKES)),
                        transitionBind('@',
                                convertKeyStroke(
                                        PlaybackMacroCommand.KEYSTROKE_CONVERTER,
                                        VimConstants.PRINTABLE_KEYSTROKES)),
                        transitionBind('r', changeCaret(CaretType.UNDERLINE),
                                convertKeyStroke(
                                        ReplaceCommand.KEYSTROKE_CONVERTER,
                                        VimConstants.PRINTABLE_KEYSTROKES_WITH_NL)),
                        transitionBind('m',
                                convertKeyStroke(
                                        SetMarkCommand.KEYSTROKE_CONVERTER,
                                        VimConstants.PRINTABLE_KEYSTROKES)),
                        leafBind('u', undo),
                        leafCtrlBind('r', redo),
                        leafCtrlBind('a', incrementNum),
                        leafCtrlBind('x', decrementNum),
                        transitionBind('Z',
                            leafBind('Z', saveAndClose),
                            leafBind('Q', (Command)CloseCommand.FORCED_CLOSE)),
                        transitionBind('z',
                            leafBind('z', centerLine),
                            leafBind('.', centerLine),
                            leafBind('-', centerBottomLine),
                            leafBind('b', centerBottomLine),
                            leafBind('t', centerTopLine),
                            leafBind(SpecialKey.RETURN, centerTopLine)
                        ))))));
    }

    protected static class AfterVisualEnterCommand extends CountIgnoringNonRepeatableCommand {
        public static final Command INSTANCE = new AfterVisualEnterCommand();
        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            String selectionVal = editorAdaptor.getConfiguration().get(Options.SELECTION);
            if ("exclusive".equals(selectionVal)) {
                final Position position = editorAdaptor.getPosition();
                editorAdaptor.setSelection(new SimpleSelection(new StartEndTextRange(position, position)));
            } else if ("inclusive".equals(selectionVal)) {
                new VisualMotionCommand(MoveRight.INSTANCE).execute(editorAdaptor);
            }
        }
    }

    protected static class AfterLinewiseVisualEnterCommand extends CountIgnoringNonRepeatableCommand {
        public static final Command INSTANCE = new AfterLinewiseVisualEnterCommand();
        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            final Position position = editorAdaptor.getPosition();
            editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, position, position));
        }
    }

    protected static class AfterBlockwiseVisualEnterCommand extends CountIgnoringNonRepeatableCommand {
        public static final Command INSTANCE = new AfterBlockwiseVisualEnterCommand();
        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            final Position position = editorAdaptor.getPosition();
//            final Position next = editorAdaptor.getCursorService().newPositionForModelOffset(position.getModelOffset()+1);
            editorAdaptor.setSelection(new BlockWiseSelection(editorAdaptor, position, position));
        }
    }

    /**
     * Fix the cursor position so that our rectangle caret doesn't go past the last character.
     * Only changes the position if NormalMode is enabled.
     */
    @Override
    public void placeCursor() {
        final Position pos = editorAdaptor.getPosition();
        final int offset = pos.getViewOffset();
        final LineInformation line = editorAdaptor.getViewContent().getLineInformationOfOffset(offset);
        if (isEnabled && line.getEndOffset() == offset && line.getLength() > 0) {
            editorAdaptor.setPosition(pos.addViewOffset(-1), StickyColumnPolicy.NEVER);
        }
    }

    @Override
    protected void commandDone() {
        super.commandDone();
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
        editorAdaptor.getRegisterManager().activateDefaultRegister();
    }

    @Override
    public void enterMode(final ModeSwitchHint... args) throws CommandExecutionException {
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
        super.enterMode(args);
        placeCursor();
        if (args.length > 0) {
            if(args[0] instanceof ExecuteCommandHint) {
                try {
                    executeCommand(((ExecuteCommandHint.OnEnter) args[0]).getCommand());
                } catch (final CommandExecutionException e) {
                    editorAdaptor.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }
}
