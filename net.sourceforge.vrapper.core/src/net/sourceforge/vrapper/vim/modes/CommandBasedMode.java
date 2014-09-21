package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.convertKeyStroke;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafCtrlBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.EXCLUSIVE;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SwitchRegisterCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.ContinueFindingMotion;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToEditLocation;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToMarkMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.LineStartMotion;
import net.sourceforge.vrapper.vim.commands.motions.MethodDeclarationMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveDown;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeftAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveRightAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveToColumn;
import net.sourceforge.vrapper.vim.commands.motions.MoveUp;
import net.sourceforge.vrapper.vim.commands.motions.MoveUpDownNonWhitespace;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.commands.motions.PageScrollMotion;
import net.sourceforge.vrapper.vim.commands.motions.ParagraphMotion;
import net.sourceforge.vrapper.vim.commands.motions.ParenthesesMove;
import net.sourceforge.vrapper.vim.commands.motions.PercentMotion;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;
import net.sourceforge.vrapper.vim.commands.motions.SectionMotion;
import net.sourceforge.vrapper.vim.commands.motions.SentenceMotion;
import net.sourceforge.vrapper.vim.commands.motions.ViewPortMotion;
import net.sourceforge.vrapper.vim.commands.motions.WordSearchMotion;
import net.sourceforge.vrapper.vim.register.RegisterManager;

/** Base class for normal and visual modes. */
public abstract class CommandBasedMode extends AbstractMode {

    private static State<Motion> motions;

    protected final State<Command> initialState;
    protected State<Command> currentState;
    private final KeyMapResolver keyMapResolver;
    private final StringBuilder commandBuffer;
    private static Map<String, State<Command>> initialStateCache = new HashMap<String, State<Command>>();

    public CommandBasedMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        currentState = initialState = getInitialState();
        keyMapResolver = buildKeyMapResolver();
        commandBuffer = new StringBuilder();
    }

    /** Reset cursor position if it is at the end of the line and the current mode won't allow it. */
    public abstract void placeCursor();
    protected abstract State<Command> buildInitialState();
    protected abstract KeyMapResolver buildKeyMapResolver();

    public State<Command> getInitialState() {
        String key = getName();
        PlatformSpecificStateProvider platformSpecificStateProvider = editorAdaptor.getPlatformSpecificStateProvider();
        if (platformSpecificStateProvider != null)
            key += " for " + platformSpecificStateProvider.getName();
        if (!initialStateCache.containsKey(key))
            initialStateCache.put(key, buildInitialState());
        return initialStateCache.get(key);
    }

    @SuppressWarnings("unchecked")
    public static State<Motion> motions() {
        if (motions == null) {
            final Motion moveLeft = MoveLeft.INSTANCE;
            final Motion moveRight = MoveRight.INSTANCE;
            final Motion moveUp = MoveUp.INSTANCE;
            final Motion moveDown = MoveDown.INSTANCE;
            final Motion moveDownNonWhitespace = MoveUpDownNonWhitespace.MOVE_DOWN;
            final Motion moveDownLessOneNonWhitespace = MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE;
            final Motion moveUpNonWhitespace = MoveUpDownNonWhitespace.MOVE_UP;
            final Motion moveToColumn = MoveToColumn.INSTANCE;
            final Motion findNext = SearchResultMotion.FORWARD;
            final Motion findPrevious = SearchResultMotion.BACKWARD;
            final Motion findWordNext = WordSearchMotion.FORWARD;
            final Motion findWordPrevious = WordSearchMotion.BACKWARD;
            final Motion findWordForwardLenient = WordSearchMotion.LENIENT_FORWARD;
            final Motion findWordBackwardLenient = WordSearchMotion.LENIENT_BACKWARD;
            final Motion wordRight = MoveWordRight.INSTANCE;
            final Motion WORDRight = MoveBigWORDRight.INSTANCE;
            final Motion wordLeft = MoveWordLeft.INSTANCE;
            final Motion WORDLeft = MoveBigWORDLeft.INSTANCE;
            final Motion wordEndRight = MoveWordEndRight.INSTANCE;
            final Motion WORDEndRight = MoveBigWORDEndRight.INSTANCE;
            final Motion wordEndLeft = MoveWordEndLeft.INSTANCE;
            final Motion WORDEndLeft = MoveBigWORDEndLeft.INSTANCE;
            final Motion paragraphForward = ParagraphMotion.FORWARD;
            final Motion paragraphBackward = ParagraphMotion.BACKWARD;
            final Motion sentenceForward = SentenceMotion.FORWARD;
            final Motion sentenceBackward = SentenceMotion.BACKWARD;
            final Motion lineStart = LineStartMotion.NON_WHITESPACE;
            final Motion column0 = LineStartMotion.COLUMN0;
            final Motion lineEnd = new LineEndMotion(EXCLUSIVE); // NOTE: it's
                                                                 // not
                                                                 // INCLUSIVE;
                                                                 // bug in Vim
                                                                 // documentation
            final Motion percentMotion = PercentMotion.INSTANCE;
            final Motion matchOpenParen = ParenthesesMove.MATCH_OPEN_PAREN;
            final Motion matchCloseParen = ParenthesesMove.MATCH_CLOSE_PAREN;
            final Motion matchOpenCurly = ParenthesesMove.MATCH_OPEN_CURLY;
            final Motion matchCloseCurly = ParenthesesMove.MATCH_CLOSE_CURLY;
            final Motion methodNextStart = MethodDeclarationMotion.NEXT_START;
            final Motion methodPrevStart = MethodDeclarationMotion.PREV_START;
            final Motion methodNextEnd = MethodDeclarationMotion.NEXT_END;
            final Motion methodPrevEnd = MethodDeclarationMotion.PREV_END;
            final Motion sectionNextStart = SectionMotion.NEXT_START;
            final Motion sectionPrevStart = SectionMotion.PREV_START;
            final Motion sectionNextEnd = SectionMotion.NEXT_END;
            final Motion sectionPrevEnd = SectionMotion.PREV_END;
            
            final Motion findForward = ContinueFindingMotion.NORMAL;
            final Motion findBackward = ContinueFindingMotion.REVERSE;

            final Motion highMove = ViewPortMotion.HIGH;
            final Motion middleMove = ViewPortMotion.MIDDLE;
            final Motion lowMove = ViewPortMotion.LOW;

            motions = state(
                    leafBind('h', moveLeft),
                    leafBind('j', moveDown),
                    leafBind('k', moveUp),
                    leafBind('l', moveRight),
                    leafBind('|', moveToColumn),
                    leafBind(SpecialKey.RETURN, moveDownNonWhitespace),
                    leafBind('+', moveDownNonWhitespace),
                    leafBind('-', moveUpNonWhitespace),
                    leafBind('_', moveDownLessOneNonWhitespace),
                    leafBind(' ', (Motion) MoveRightAcrossLines.INSTANCE),
                    leafBind(SpecialKey.BACKSPACE, (Motion) MoveLeftAcrossLines.INSTANCE),
                    leafBind(SpecialKey.ARROW_LEFT, moveLeft),
                    leafBind(SpecialKey.ARROW_DOWN, moveDown),
                    leafBind(SpecialKey.ARROW_UP, moveUp),
                    leafBind(SpecialKey.ARROW_RIGHT, moveRight),
                    leafCtrlBind('f', PageScrollMotion.SCROLL_PGDN),
                    leafCtrlBind('b', PageScrollMotion.SCROLL_PGUP),
                    leafCtrlBind('d', PageScrollMotion.SCROLL_HALF_PGDN),
                    leafCtrlBind('u', PageScrollMotion.SCROLL_HALF_PGUP),
                    leafBind(SpecialKey.PAGE_DOWN, PageScrollMotion.SCROLL_PGDN),
                    leafBind(SpecialKey.PAGE_UP, PageScrollMotion.SCROLL_PGUP),
                    leafBind(';', findForward),
                    leafBind(',', findBackward),
                    transitionBind('t', convertKeyStroke(FindMotion
                            .keyConverter(false, false),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('T', convertKeyStroke(FindMotion
                            .keyConverter(false, true),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('f', convertKeyStroke(FindMotion
                            .keyConverter(true, false),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('F', convertKeyStroke(FindMotion
                            .keyConverter(true, true),
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('\'', convertKeyStroke(
                            GoToMarkMotion.LINEWISE_CONVERTER,
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    transitionBind('`', convertKeyStroke(
                            GoToMarkMotion.CHARWISE_CONVERTER,
                            VimConstants.PRINTABLE_KEYSTROKES)),
                    leafBind('w', wordRight),
                    leafBind('W', WORDRight),
                    leafBind('e', wordEndRight),
                    leafBind('E', WORDEndRight),
                    leafBind('b', wordLeft),
                    leafBind('B', WORDLeft),
                    leafBind('}', paragraphForward),
                    leafBind('{', paragraphBackward),
                    leafBind(')', sentenceForward),
                    leafBind('(', sentenceBackward),
                    leafBind('G', GoToLineMotion.LAST_LINE), // XXX: counts
                    leafBind('H', highMove),
                    leafBind('M', middleMove),
                    leafBind('L', lowMove),
                    leafBind('n', findNext),
                    leafBind('N', findPrevious),
                    leafBind('*', findWordNext),
                    leafBind('#', findWordPrevious),
                    leafBind('0', column0),
                    leafBind('$', lineEnd),
                    leafBind(SpecialKey.HOME, lineStart),
                    leafBind(SpecialKey.END, lineEnd),
                    leafBind('%', percentMotion),
                    leafBind('^', lineStart),
                    transitionBind('[', 
                    		leafBind('m', methodPrevStart),
                    		leafBind('M', methodPrevEnd),
                    		leafBind('[', sectionPrevStart),
                    		leafBind(']', sectionPrevEnd),
                    		leafBind('(', matchOpenParen),
                    		leafBind('{', matchOpenCurly)),
                    transitionBind(']', 
                    		leafBind('m', methodNextStart),
                    		leafBind('M', methodNextEnd),
                    		leafBind(']', sectionNextStart),
                    		leafBind('[', sectionNextEnd),
                    		leafBind(')', matchCloseParen),
                    		leafBind('}', matchCloseCurly)),
                    transitionBind('g',
                            leafBind('g', GoToLineMotion.FIRST_LINE),
                            leafBind('*', findWordForwardLenient),
                            leafBind('#', findWordBackwardLenient),
                            leafBind(';', GoToEditLocation.BACKWARDS),
                            leafBind(',', GoToEditLocation.FORWARD),
                            leafBind('e', wordEndLeft),
                            leafBind('E', WORDEndLeft)));
        }
        return motions;
    }

    public void executeCommand(Command command)
            throws CommandExecutionException {
        editorAdaptor.getListeners().fireCommandAboutToExecute();
        command.execute(editorAdaptor);
        editorAdaptor.getListeners().fireCommandExecuted();
        Command repetition = command.repetition();
        if (repetition != null) {
            RegisterManager registerManager = editorAdaptor .getRegisterManager();
            if (!registerManager.isDefaultRegisterActive()) {
                repetition = new VimCommandSequence(new SwitchRegisterCommand(
                        registerManager.getActiveRegister()), repetition);
            }
            registerManager.setLastEdit(repetition);
            editorAdaptor.getCursorService().setMark(
                    CursorService.LAST_EDIT_MARK, editorAdaptor.getPosition());
            //restore default register if we just did a SwitchRegisterCommand
            //(if we were already on default register, this is a no-op)
            registerManager.activateDefaultRegister();
        }
    }

    public boolean handleKey(KeyStroke keyStroke) {
        if (editorAdaptor == null) {
            return false;
        }

        if (currentState == null) {
            VrapperLog
                    .error("current state was null - this shouldn't have happened!");
            reset();
            editorAdaptor.getListeners().fireStateReset(false);
        }
        if ( ! keyStroke.isVirtual()) {
            editorAdaptor.getUserInterfaceService().setErrorMessage(null);
        }

        Transition<Command> transition = currentState.press(keyStroke);
        
        /* If no transition was found, check if an AltGr modifier was pressed and try with no mods.
         * For example, the user presses AltGr + Q which means @ with the user's keyboard layout,
         * but Vrapper on Windows saw this as Control + Alt + @ which it treats as another KeyStroke
         */
        if (transition == null && VimUtils.fixAltGrKey(keyStroke) != null) {
            KeyStroke key = VimUtils.fixAltGrKey(keyStroke);
            transition = currentState.press(key);
            keyMapResolver.storeKey(key);
        } else {
            keyMapResolver.storeKey(keyStroke);
        }
        commandBuffer.append(keyStroke.getCharacter());
        boolean recognized = false;
        if (transition != null) {
            Command command = transition.getValue();
            currentState = transition.getNextState();
            if (command != null) {
                recognized = true;
                try {
                    executeCommand(command);
                } catch (CommandExecutionException e) {
                    setErrorMessage(e.getMessage());
                    reset();
                    editorAdaptor.getListeners().fireStateReset(true);
                    commandDone();
                    isEnabled = true;
                }
            }
        }
        if (transition == null || currentState == null) {
            reset();
            editorAdaptor.getListeners().fireStateReset(recognized);
            if (isEnabled) {
                commandDone();
            }
        }

        /* If you're setting the info bar as the result of a command, 
         * we don't want to immediately clear that status out. For example, 
         * "ga" shows the ASCII code for the character under the cursor. 
         * If we don't do this check, it will not get a chance to show the
         * info text.
         */
        if(editorAdaptor.getUserInterfaceService().isInfoSet()) {
            // Currently, the Info bar probably has the letters of the command we were typing
            resetCommandBuffer();
            
            // Now reset the info bar with the results from the command we just executed
            editorAdaptor.getUserInterfaceService().setInfoMessage(
                    editorAdaptor.getUserInterfaceService().getLastCommandResultValue());
            
            // Setting this to false makes sure we don't come back here until
            // we execute another command
            editorAdaptor.getUserInterfaceService().setInfoSet(false);
        } else {
            editorAdaptor.getUserInterfaceService().setInfoMessage(commandBuffer.toString());
        }

        // FIXME: has some issues with sticky column
        placeCursor();

        return true;
    }

    private void setErrorMessage(String message) {
        editorAdaptor.getUserInterfaceService().setErrorMessage(message);
    }

    private void reset() {
        currentState = initialState;
        keyMapResolver.reset();
    }

    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return keyMapResolver.getKeyMapName(stroke);
    }

    public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
        super.leaveMode(hints);
        resetCommandBuffer();
    }

    /**
     * this is a hook method which is called when command execution is done
     */
    protected void commandDone() {
        resetCommandBuffer();
    }

    private void resetCommandBuffer() {
        commandBuffer.delete(0, commandBuffer.length());
        editorAdaptor.getUserInterfaceService().setInfoMessage("");
    }

    protected State<Command> getPlatformSpecificState(String mode) {
        State<Command> platformSpecificState = editorAdaptor
                .getPlatformSpecificStateProvider().getState(mode);
        if (platformSpecificState == null) {
            platformSpecificState = EmptyState.getInstance();
        }
        return platformSpecificState;
    }

}
