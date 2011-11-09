package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.changeCaret;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmdsWithUpperCase;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.dontRepeat;
import static net.sourceforge.vrapper.vim.commands.ConstructorWrappers.seq;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.keymap.vim.DelimitedTextObjectState;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.keymap.vim.RegisterState;
import net.sourceforge.vrapper.keymap.vim.TextObjectState;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CenterLineCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.DotCommand;
import net.sourceforge.vrapper.vim.commands.InsertLineCommand;
import net.sourceforge.vrapper.vim.commands.JoinLinesCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.MotionPairTextObject;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.OptionDependentCommand;
import net.sourceforge.vrapper.vim.commands.OptionDependentTextObject;
import net.sourceforge.vrapper.vim.commands.PasteAfterCommand;
import net.sourceforge.vrapper.vim.commands.PasteBeforeCommand;
import net.sourceforge.vrapper.vim.commands.PlaybackMacroCommand;
import net.sourceforge.vrapper.vim.commands.RecordMacroCommand;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.ReplaceCommand;
import net.sourceforge.vrapper.vim.commands.SetMarkCommand;
import net.sourceforge.vrapper.vim.commands.SimpleDelimitedText;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.StickToEOLCommand;
import net.sourceforge.vrapper.vim.commands.SwapCaseCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.YankOperation;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRightForChange;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRightForChange;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRightForUpdate;
import net.sourceforge.vrapper.vim.commands.motions.ParagraphMotion;

public class NormalMode extends CommandBasedMode {

    public static final String KEYMAP_NAME = "Normal Mode Keymap";
    public static final String NAME = "normal mode";
    private static State<TextObject> textObjects;
    private static State<DelimitedText> delimitedTexts;
    private static State<Motion> textMotions;

    public NormalMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected KeyMapResolver buildKeyMapResolver() {
        State<String> state = union(
                state(
                    leafBind('r', KeyMapResolver.NO_KEYMAP),
                    leafBind('z', KeyMapResolver.NO_KEYMAP),
                    leafBind('q', KeyMapResolver.NO_KEYMAP),
                    leafBind('@', KeyMapResolver.NO_KEYMAP)),
                getKeyMapsForMotions(),
                editorAdaptor.getPlatformSpecificStateProvider().getKeyMaps(NAME));
        final State<String> countEater = new CountConsumingState<String>(state);
        State<String> registerKeymapState = new RegisterKeymapState(KEYMAP_NAME, countEater);
        return new KeyMapResolver(registerKeymapState, KEYMAP_NAME);
    }
    @SuppressWarnings("unchecked")
    public static synchronized State<DelimitedText> delimitedTexts() {
        if (delimitedTexts == null) {
        final DelimitedText inBracket = new SimpleDelimitedText('(', ')');
        final DelimitedText inSquareBracket = new SimpleDelimitedText('[', ']');
        final DelimitedText inBrace = new SimpleDelimitedText('{', '}');
        final DelimitedText inAngleBrace = new SimpleDelimitedText('<', '>');
        final DelimitedText inString = new SimpleDelimitedText('"');
        final DelimitedText inGraveString = new SimpleDelimitedText('`');
        final DelimitedText inChar = new SimpleDelimitedText('\'');


        delimitedTexts = state(
                leafBind('b', inBracket),
                leafBind('(', inBracket),
                leafBind(')', inBracket),
                leafBind('[', inSquareBracket),
                leafBind(']', inSquareBracket),
                leafBind('B', inBrace),
                leafBind('{', inBrace),
                leafBind('}', inBrace),
                leafBind('<', inAngleBrace),
                leafBind('>', inAngleBrace),
                leafBind('"', inString),
                leafBind('\'', inChar),
                leafBind('`', inGraveString));
        }
        return delimitedTexts;

    }
    
    @SuppressWarnings("unchecked")
    public static synchronized State<Motion> textMotions() {
        if (textMotions == null) {

            //override the default motions for a few motions that act differently in text mode
            textMotions = union(
            				state(
            					leafBind('w', MoveWordRightForUpdate.MOVE_WORD_RIGHT_INSTANCE),
            					leafBind('W', MoveWordRightForUpdate.MOVE_BIG_WORD_RIGHT_INSTANCE)
            				),
            				motions()
            			);
        }
        return textMotions;
    }


    @SuppressWarnings("unchecked")
    public static synchronized State<TextObject> textObjects() {
        if (textObjects == null) {
            final TextObject innerWord = new MotionPairTextObject(MoveWordLeft.BAILS_OFF, MoveWordEndRight.BAILS_OFF);
            final TextObject aWord = new MotionPairTextObject(MoveWordLeft.BAILS_OFF, MoveWordRight.BAILS_OFF);
            final TextObject innerWORD = new MotionPairTextObject(MoveBigWORDLeft.BAILS_OFF, MoveBigWORDEndRight.BAILS_OFF);
            final TextObject aWORD = new MotionPairTextObject(MoveBigWORDLeft.BAILS_OFF, MoveBigWORDRight.BAILS_OFF);
            final TextObject innerParagraph = new MotionPairTextObject(ParagraphMotion.TO_BACKWARD, ParagraphMotion.FORWARD);
            final TextObject aParagraph = new MotionPairTextObject(ParagraphMotion.TO_BACKWARD, ParagraphMotion.TO_FORWARD);

            textObjects = union(
                        state(
                            transitionBind('i', union(
                                    state(  leafBind('w', innerWord),
                                            leafBind('W', innerWORD),
                                            leafBind('p', innerParagraph)
                                    ),
                                    new DelimitedTextObjectState(delimitedTexts(), DelimitedTextObjectState.INNER))),
                            transitionBind('a', union(
                                    state(
                                            leafBind('w', aWord),
                                            leafBind('W', aWORD),
                                            leafBind('p', aParagraph)
                                    ),
                                    new DelimitedTextObjectState(delimitedTexts(), DelimitedTextObjectState.OUTER)))),
                        new TextObjectState(textMotions()));

            textObjects = CountingState.wrap(textObjects);
        }
        return textObjects;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> buildInitialState() {
        Command visualMode = new ChangeModeCommand(VisualMode.NAME);
        Command linewiseVisualMode = new ChangeModeCommand(LinewiseVisualMode.NAME);

        final Motion moveLeft = MoveLeft.INSTANCE;
        final Motion moveRight = MoveRight.INSTANCE;
        final Motion wordRight = MoveWordRight.INSTANCE;
        final Motion wordEndRightForChange = MoveWordEndRightForChange.INSTANCE;
        final Motion bigWordRight = MoveBigWORDRight.INSTANCE;
        final Motion bigWordEndRightForChange = MoveBigWORDEndRightForChange.INSTANCE;
        final Motion bol = LineStartMotion.NON_WHITESPACE;
        final Motion eol = new LineEndMotion(BorderPolicy.EXCLUSIVE);
        final Motion wholeLineEol = new LineEndMotion(BorderPolicy.LINE_WISE);

        final State<Motion> motions = motions();
        final TextObject wordForCw = new OptionDependentTextObject(Options.SANE_CW, wordRight, wordEndRightForChange);
        final TextObject wordForCW = new OptionDependentTextObject(Options.SANE_CW, bigWordRight, bigWordEndRightForChange);
        final TextObject toEol = new MotionTextObject(eol);
        final TextObject toEolForY = new OptionDependentTextObject(Options.SANE_Y, eol, wholeLineEol);

        State<TextObject> textObjects = textObjects();
        State<TextObject> textObjectsForChange = union(
                state(
                        leafBind('w', wordForCw),
                        leafBind('W', wordForCW)),
                textObjects);
        textObjectsForChange = CountingState.wrap(textObjectsForChange);

        TextOperation delete = DeleteOperation.INSTANCE;
        TextOperation change = ChangeOperation.INSTANCE;
        TextOperation yank   = YankOperation.INSTANCE;
        Command undo = UndoCommand.INSTANCE;
        Command redo = RedoCommand.INSTANCE;
        Command pasteAfter  = PasteAfterCommand.CURSOR_ON_TEXT;
        Command pasteBefore = PasteBeforeCommand.CURSOR_ON_TEXT;
        Command pasteAfterWithG  = PasteAfterCommand.CURSOR_AFTER_TEXT;
        Command pasteBeforeWithG = PasteBeforeCommand.CURSOR_AFTER_TEXT;
        Command deleteNext = new TextOperationTextObjectCommand(delete, new MotionTextObject(moveRight));
        Command deletePrevious = new TextOperationTextObjectCommand(delete, new MotionTextObject(moveLeft));
        Command repeatLastOne = DotCommand.INSTANCE;
        Command tildeCmd = SwapCaseCommand.INSTANCE;
        Command stickToEOL = StickToEOLCommand.INSTANCE;
        LineEndMotion lineEndMotion = new LineEndMotion(BorderPolicy.LINE_WISE);
        Command substituteLine = new TextOperationTextObjectCommand(change, new MotionTextObject(lineEndMotion));
        Command substituteChar = new TextOperationTextObjectCommand(change, new MotionTextObject(moveRight));
        Command joinLines = JoinLinesCommand.INSTANCE;
        Command joinLinesDumbWay = JoinLinesCommand.DUMB_INSTANCE;
        Command centerLine = CenterLineCommand.CENTER;
        Command centerBottomLine = CenterLineCommand.BOTTOM;
        Command centerTopLine = CenterLineCommand.TOP;

        Command afterEnteringVisualInc = new OptionDependentCommand<String>(Options.SELECTION, "inclusive",
                new VisualMotionCommand(moveRight));
        Command afterEnteringVisualExc = new OptionDependentCommand<String>(Options.SELECTION, "exclusive",
                new CountIgnoringNonRepeatableCommand() {
                    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
                        Position position = editorAdaptor.getPosition();
                        editorAdaptor.setSelection(new SimpleSelection(new StartEndTextRange(position, position)));
                    }
                });
        Command selectLine = new CountIgnoringNonRepeatableCommand() {
            public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
                        Position position = editorAdaptor.getPosition();
                        editorAdaptor.setSelection(new LineWiseSelection(editorAdaptor, position, position));
            }
        };
        Command afterEnteringVisual = seq(afterEnteringVisualInc, afterEnteringVisualExc);

        State<Command> motionCommands = new GoThereState(motions);
        Command nextResult = motionCommands.press(key('n')).getValue();

        State<Command> platformSpecificState = getPlatformSpecificState(NAME);
        return RegisterState.wrap(CountingState.wrap(union(
                platformSpecificState,
                operatorCmdsWithUpperCase('d', delete, toEol,     textObjects),
                operatorCmdsWithUpperCase('y', yank,   toEolForY, textObjects),
                operatorCmdsWithUpperCase('c', change, toEol,     textObjectsForChange),
                state(leafBind('$', stickToEOL)),
                motionCommands,
                state(
                        leafBind('i', (Command) new ChangeToInsertModeCommand()),
                        leafBind('a', (Command) new ChangeToInsertModeCommand(new MotionCommand(moveRight))),
                        leafBind('I', (Command) new ChangeToInsertModeCommand(new MotionCommand(bol))),
                        leafBind('A', (Command) new ChangeToInsertModeCommand(new MotionCommand(eol))),
                        leafBind(':', (Command) new ChangeModeCommand(CommandLineMode.NAME)),
                        leafBind('?', (Command) new ChangeToSearchModeCommand(true, nextResult)),
                        leafBind('/', (Command) new ChangeToSearchModeCommand(false, nextResult)),
                        leafBind('R', (Command) new ChangeModeCommand(ReplaceMode.NAME)),
                        leafBind('o', (Command) new ChangeToInsertModeCommand(InsertLineCommand.POST_CURSOR)),
                        leafBind('O', (Command) new ChangeToInsertModeCommand(InsertLineCommand.PRE_CURSOR)),
                        leafBind('v', seq(visualMode, afterEnteringVisual)),
                        leafBind('V', dontRepeat(seq(linewiseVisualMode, selectLine))),
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
                        transitionBind('g',
                                leafBind('J', joinLinesDumbWay),
                                leafBind('p', pasteAfterWithG),
                                leafBind('P', pasteBeforeWithG)),
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
                        transitionBind('z',
                            leafBind('z', centerLine),
                            leafBind('.', centerLine),
                            leafBind('-', centerBottomLine),
                            leafBind('b', centerBottomLine),
                            leafBind('t', centerTopLine),
                            leafBind(SpecialKey.RETURN, centerTopLine)
                        )))));
    }

    @Override
    protected void placeCursor() {
        Position pos = editorAdaptor.getPosition();
        int offset = pos.getViewOffset();
        LineInformation line = editorAdaptor.getViewContent().getLineInformationOfOffset(offset);
        if (isEnabled && line.getEndOffset() == offset && line.getLength() > 0) {
            editorAdaptor.setPosition(pos.addViewOffset(-1), false);
        }
    }

    @Override
    protected void commandDone() {
        super.commandDone();
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
        editorAdaptor.getRegisterManager().activateDefaultRegister();
    }

    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        placeCursor();
        editorAdaptor.getCursorService().setCaret(CaretType.RECTANGULAR);
        super.enterMode(args);
        if (args.length > 0) {
	        executeCommand(((ExecuteCommandHint.OnEnter) args[0]).getCommand());
        }
    }

    public String getName() {
        return NAME;
    }
}
