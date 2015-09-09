package net.sourceforge.vrapper.plugin.sneak.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CommandLineUI;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.plugin.sneak.commands.motions.JumpMotionDecorator;
import net.sourceforge.vrapper.plugin.sneak.commands.motions.SneakMotion;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.EditorAdaptorStateManager;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakState;
import net.sourceforge.vrapper.plugin.sneak.commands.utils.SneakStateManager;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.VisualMotionCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ExecuteCommandHint;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.WithCountHint;

public class SneakInputMode extends AbstractMode {
    
    public static final String NAME = SneakInputMode.class.getName();
    private static final String DISPLAY_NAME = "SNEAK INPUT";
    public static final ModeSwitchHint SNEAK_BACKWARDS = new ModeSwitchHint() {};
    public static final ModeSwitchHint FROM_VISUAL = new ModeSwitchHint() {};

    public static final SneakStateManager STATEMANAGER = new EditorAdaptorStateManager();

    protected static final KeyStroke KEY_RETURN = ConstructorWrappers.key(SpecialKey.RETURN);
    protected static final KeyStroke KEY_ESCAPE = ConstructorWrappers.key(SpecialKey.ESC);
    protected static final KeyStroke KEY_BACKSP = ConstructorWrappers.key(SpecialKey.BACKSPACE);

    /** Holds the most recent Sneak motion. */
    public static SneakMotion LAST_SNEAK_MOTION;

    protected CommandLineUI commandLine;
    protected int sneakInputLength = 2;
    protected boolean sneakBackwards;
    protected boolean fromVisual;

    /**
     * Current sneak sub-mode state. This is kept in this class to make sure it reflects the current
     * editor as modes are normally created once per EditorAdaptor.
     */
    protected SneakState previousState;
    private int countValue;
    private int motionCountValue;

    public SneakInputMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
        previousState = new SneakState();
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    public SneakState getPreviousState() {
        return previousState;
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        if (stroke.equals(KEY_ESCAPE)) {
            editorAdaptor.changeModeSafely(editorAdaptor.getLastModeName());

        } else if (stroke.equals(KEY_BACKSP)) {
            commandLine.erase();
            if (commandLine.getContents().length() <= 0) {
                editorAdaptor.changeModeSafely(editorAdaptor.getLastModeName(),
                        AbstractVisualMode.RECALL_SELECTION_HINT);
            }

        } else if (stroke.equals(KEY_RETURN)) {
            if (commandLine.getContents().length() > 0) {
                startSneaking(commandLine.getContents());
            } else {
                // Check if we can reuse an existing SneakMotion stored in the last motion field.
                // This way we sync the last sneak keyword from a different editor to this one.
                SneakMotion sneakMotion = LAST_SNEAK_MOTION;
                if (sneakMotion != null) {
                    if (sneakMotion.isBackward() != sneakBackwards) {
                        sneakMotion = sneakMotion.reverse();
                        saveSneakMotion(sneakMotion);
                    }
                    executeMotionInLastMode(sneakMotion, previousState.getSneakSearch().getKeyword());
                } else if (previousState == null) {
                    editorAdaptor.changeModeSafely(editorAdaptor.getLastModeName());
                } else {
                    startSneaking(previousState.getSneakSearch().getKeyword());
                }
            }

        } else if (stroke.getCharacter() == KeyStroke.SPECIAL_KEY) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("Sneak only accepts printable "
                    + "characters");
                editorAdaptor.changeModeSafely(editorAdaptor.getLastModeName(),
                        AbstractVisualMode.RECALL_SELECTION_HINT);

        } else {
            commandLine.type(Character.toString(stroke.getCharacter()));
            if (commandLine.getContents().length() >= sneakInputLength) {
                startSneaking(commandLine.getContents());
            }
        }
        return true;
    }

    protected void startSneaking(String searchString) {

        Search searchKeyword = constructSearchKeyword(editorAdaptor, searchString, sneakBackwards);

        SneakMotion sneakMotion = buildSneakMotion(searchKeyword);

        saveSneakMotion(sneakMotion);

        // Clear previous sneak highlights before adding new ones.
        SneakState previousState = STATEMANAGER.getSneakState(editorAdaptor);
        if (previousState.isSneaking()) {
            previousState.deactivateSneak(editorAdaptor.getHighlightingService());
        }

        // The initial sneak invocation is treated as a jump but following invocations shouldn't.
        // This decorator lets MotionCommand adjust the jump list and last jump mark.
        Motion motion = new JumpMotionDecorator(sneakMotion);

        executeMotionInLastMode(motion, searchString);
    }

    private SneakMotion buildSneakMotion(Search searchKeyword) {
        int columnLeft = -1;
        int columnRight = -1;

        //calculate columns
        if (countValue != Command.NO_COUNT_GIVEN && countValue > 1) {
            int currentOffset = editorAdaptor.getPosition().getModelOffset();
            TextContent textContent = editorAdaptor.getModelContent();
            LineInformation currentLine = textContent.getLineInformationOfOffset(currentOffset);
            String currentLineContent = textContent.getText(currentLine.getBeginOffset(), currentLine.getLength());
            int currentOffsetInLine = currentOffset - currentLine.getBeginOffset();
            Integer tabstop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);

            int[] visualOffsets = StringUtils.calculateVisualOffsets(currentLineContent,
                    currentLineContent.length(), tabstop);
            int currentColumn = visualOffsets[currentOffsetInLine];
            columnLeft = Math.max(0, currentColumn - countValue);
            columnRight = currentColumn + countValue;
        }
        return new SneakMotion(STATEMANAGER, searchKeyword, columnLeft, columnRight);
    }

    public static Search constructSearchKeyword(EditorAdaptor editorAdaptor, String searchString,
            boolean sneakBackwards) {
        // vim-sneak made this configurable, this port assumes we reuse the existing options
        boolean caseSensitive = ! editorAdaptor.getConfiguration().get(Options.IGNORE_CASE)
            || (editorAdaptor.getConfiguration().get(Options.SMART_CASE)
                && StringUtils.containsUppercase(searchString));
        Search searchKeyword = new Search(searchString, sneakBackwards, caseSensitive);
        return searchKeyword;
    }

    protected void executeMotionInLastMode(Motion motion, String searchStringForLogging) {
        try {
            MotionCommand motionCommand;
            if (fromVisual) {
                motionCommand = new VisualMotionCommand(motion);
            } else {
                motionCommand = new MotionCommand(motion);
            }
            Command effectiveCommand = motionCommand;
            if (motionCountValue > 1) {
                effectiveCommand = motionCommand.withCount(motionCountValue);
            }
            editorAdaptor.changeMode(editorAdaptor.getLastModeName(),
                            AbstractVisualMode.RECALL_SELECTION_HINT,
                            new ExecuteCommandHint.OnEnter(effectiveCommand));
        } catch (CommandExecutionException e) {
            VrapperLog.error("Last mode raised an error when trying to sneak to '"
                + searchStringForLogging + "'", e);
            editorAdaptor.changeModeSafely(NormalMode.NAME);
        } catch (RuntimeException e) {
            VrapperLog.error("Last mode raised an error when trying to sneak to '"
                    + searchStringForLogging + "'", e);
            editorAdaptor.changeModeSafely(NormalMode.NAME);
        }
    }

    @Override
    public void enterMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        super.enterMode(hints);

        // reset defaults
        sneakInputLength = 2;
        sneakBackwards = false;
        fromVisual = false;
        countValue = 0;
        motionCountValue = 0;

        for (ModeSwitchHint hint : hints) {
            if (hint == SNEAK_BACKWARDS) {
                sneakBackwards = true;
            } else if (hint == FROM_VISUAL) {
                fromVisual = true;
            } else if (hint instanceof WithCountHint) {
                countValue = ((WithCountHint) hint).getCount();
            }
        }

        // If sneak is active, keep it active until the user forcefully quits this mode
        if (previousState.isSneaking()) {
            STATEMANAGER.markSneakActive(editorAdaptor);
        }

        commandLine = editorAdaptor.getCommandLine();
        commandLine.setPrompt(">");
        commandLine.open();
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints)
            throws CommandExecutionException {
        // If sneak is active, keep it active until the user forcefully quits this mode
        if (previousState.isSneaking()) {
            STATEMANAGER.markSneakActive(editorAdaptor);
        }

        super.leaveMode(hints);
        commandLine.close();
    }

    /**
     * Sneak input mode doesn't accept keymaps.
     */
    @Override
    public String resolveKeyMap(KeyStroke stroke) {
        return null;
    }

    private void saveSneakMotion(SneakMotion motion) {
        LAST_SNEAK_MOTION = motion;
    }
}
