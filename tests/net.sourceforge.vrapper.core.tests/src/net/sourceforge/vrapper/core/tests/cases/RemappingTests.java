package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.platform.AbstractPlatformSpecificModeProvider;
import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.testutil.CommandTestCase;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.AbstractMode;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

/**
 * Tests for remapping in different modes.
 */
public class RemappingTests extends CommandTestCase {

    /**
     * Special EditorMode implementation which decorates another mode so as to record mapped keys.
     */
    public static class KeyStrokeRecordingMode extends AbstractMode {
        public static final List<KeyStroke> INPUT = new ArrayList<KeyStroke>();
        protected String fakeName;
        protected EditorMode originalMode;

        public KeyStrokeRecordingMode(EditorAdaptor editorAdaptor, EditorMode originalMode,
                String fakeName) {
            super(editorAdaptor);
            this.fakeName = fakeName;
            this.originalMode = originalMode;
        }
        @Override
        public String getName() {
            return fakeName;
        }
        @Override
        public String getDisplayName() {
            return "";
        }
        @Override
        public void enterMode(ModeSwitchHint... hints) throws CommandExecutionException {
            super.enterMode(hints);
            originalMode.enterMode(hints);
        }
        @Override
        public void leaveMode(ModeSwitchHint... hints) throws CommandExecutionException {
            super.leaveMode(hints);
            originalMode.leaveMode(hints);
        }
        @Override
        public boolean handleKey(KeyStroke stroke) {
            INPUT.add(stroke);
            return originalMode.handleKey(stroke);
        }
        @Override
        public String resolveKeyMap(KeyStroke stroke) {
            // Use resolver from original mode. More or less a hack, but it works.
            return originalMode.resolveKeyMap(stroke);
        }
        @Override
        public boolean isRemapBacktracking() {
            return originalMode.isRemapBacktracking();
        }
    }

    public static final String NAME_PREFIX = "keystrokestub.";
    public static final String TEST_NORMAL = NAME_PREFIX + NormalMode.NAME;
    public static final String TEST_VISUAL = NAME_PREFIX + VisualMode.NAME;
    public static final String TEST_INSERT = NAME_PREFIX + InsertMode.NAME;

    public static class KeyStrokeRecordingModeProvider extends AbstractPlatformSpecificModeProvider{
        public final static PlatformSpecificModeProvider INSTANCE = new KeyStrokeRecordingModeProvider("keyrecordingstub");

        public KeyStrokeRecordingModeProvider(String name) {
            super(name);
        }
        @Override
        public List<EditorMode> getModes(EditorAdaptor editorAdaptor) throws CommandExecutionException {
            return Arrays.asList(new EditorMode[]{
                    new KeyStrokeRecordingMode(editorAdaptor, editorAdaptor.getMode(NormalMode.NAME), TEST_NORMAL),
                    new KeyStrokeRecordingMode(editorAdaptor, editorAdaptor.getMode(InsertMode.NAME), TEST_INSERT),
                    new KeyStrokeRecordingMode(editorAdaptor, editorAdaptor.getMode(VisualMode.NAME), TEST_VISUAL)
                });
        }
    }

    @Override
    public void setUp() {
        super.setUp();
        Mockito.when(platform.getPlatformSpecificModeProvider()).thenReturn(KeyStrokeRecordingModeProvider.INSTANCE);
        reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
    }

    @Override
    protected void reloadEditorAdaptor() {
        super.reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
    }

    /** Clears the mapping before and after all tests to make sure other tests aren't impacted. */
    @Before
    @After
    public void clearMappings() {
        super.cleanUp();
        type(parseKeyStrokes(":nmapclear<CR>"));
        type(parseKeyStrokes(":omapclear<CR>"));
        type(parseKeyStrokes(":vmapclear<CR>"));
        type(parseKeyStrokes(":imapclear<CR>"));
    }

    @Test
    public void testCountingRemap() {
        type(parseKeyStrokes(":nmap L dd<CR>"));
        checkRemap(TEST_NORMAL, "3L", "3dd");
    }

    @Test
    public void testOmap() {
        // Sanity checks
        checkRemap(TEST_NORMAL, "d$", "d$");
        checkRemap(TEST_NORMAL, "g~$", "g~$");

        type(parseKeyStrokes(":onoremap L $<CR>"));
        checkRemap(TEST_NORMAL, "dL", "d$");
        checkRemap(TEST_NORMAL, "g~L", "g~$");
    }

    @Test
    public void testCountingOmap() {
        // Sanity check
        checkRemap(TEST_NORMAL, "2\"_2d$", "2\"_2d$");
        checkRemap(TEST_NORMAL, "2g~2$", "2g~2$");

        type(parseKeyStrokes(":onoremap L $<CR>"));
        checkRemap(TEST_NORMAL, "2\"_2dL", "2\"_2d$");
        checkRemap(TEST_NORMAL, "d4L", "d4$");
        checkRemap(TEST_NORMAL, "\"_d4L", "\"_d4$");
        checkRemap(TEST_NORMAL, "2g~2L", "2g~2$");
    }

    @Test
    public void testBlackHoleRegisterRemap() {

        // Sanity check
        checkRemap(TEST_NORMAL, "\"_dd", "\"_dd");

        type(parseKeyStrokes(":nnoremap R \"_dd<CR>"));
        checkRemap(TEST_NORMAL, "R", "\"_dd");

        type(parseKeyStrokes(":nnoremap D \"_d$<CR>"));
        checkRemap(TEST_NORMAL, "D", "\"_d$");

        type(parseKeyStrokes(":nnoremap C d$<CR>"));
        checkRemap(TEST_NORMAL, "\"_C", "\"_d$");
    }

    @Test
    public void testBlackHoleRegisterCountingRemap() {
        // Sanity checks
        checkRemap(TEST_NORMAL, "\"_3dd", "\"_3dd");
        checkRemap(TEST_NORMAL, "3\"_dd", "3\"_dd");

        type(parseKeyStrokes(":noremap dd \"_dd<CR>"));
        checkRemap(TEST_NORMAL, "3dd", "3\"_dd");

        type(parseKeyStrokes(":noremap dd \"_2dd<CR>"));
        checkRemap(TEST_NORMAL, "2dd", "2\"_2dd");

        type(parseKeyStrokes(":nnoremap D \"_d$<CR>"));
        checkRemap(TEST_NORMAL, "D", "\"_d$");
        checkRemap(TEST_NORMAL, "2D", "2\"_d$");
    }

    @Test
    public void testPrefixTextObject() {
        // Sanity checks
        checkRemap(TEST_NORMAL, "di)", "di)");

        type(parseKeyStrokes(":nnoremap )) j<CR>"));
        checkRemap(TEST_NORMAL, "di)", "di)");

        type(parseKeyStrokes(":omap )) j<CR>"));
        checkRemap(TEST_NORMAL, "d))", "dj");
        checkRemap(TEST_NORMAL, "di)", "di)");
    }

    @Test
    public void testEndOfLineRemap() {
        // Original bug report used 'L', but that is too difficult to test as L is 'jump to middle'
        type(parseKeyStrokes(":nnoremap l $<CR>"));
        checkRemap(TEST_NORMAL, "l", "$");

        // 'l' isn't defined with omap yet so it should delete just one char at first.
        checkRemap(TEST_NORMAL, "dl", "dl");

        type(parseKeyStrokes(":onoremap l $<CR>"));
        // Try again now that l is in omap.
        checkRemap(TEST_NORMAL, "dl", "d$");
        // Deletes till end of current line and next line.
        checkRemap(TEST_NORMAL, "d2l", "d2$");
        checkRemap(TEST_NORMAL, "2dl", "2d$");
        // Should delete till end of current line + 7 more lines
        checkRemap(TEST_NORMAL, "2\"_2d2l", "2\"_2d2$");

        // 'l' isn't remapped yet in visual mode. Try before / after.
        checkRemap(TEST_VISUAL, "ld", "ld");
        type(parseKeyStrokes(":vnoremap l $<CR>"));
        checkRemap(TEST_VISUAL, "ld", "$d");
    }

    @Test
    public void testMotionsWithoutOMap() {
        // Check that no omap binds are mixed in with the prefixed motions: f,F,t,T,`,',i,a
        checkRemap(TEST_NORMAL, "fL", "fL");

        type(parseKeyStrokes(":noremap L $<CR>"));
        checkRemap(TEST_NORMAL, "L", "$");
        checkRemap(TEST_NORMAL, "fL", "fL");
        checkRemap(TEST_NORMAL, "dfL", "dfL");
        checkRemap(TEST_NORMAL, "d2fL", "d2fL");

        // Test cursor service doesn't support marks, just check if they're set and get.
        type(parseKeyStrokes(":noremap s \"_s<CR>"));
        type(parseKeyStrokes("ms"));
        verify(cursorAndSelection, times(1)).setMark(Mockito.eq("s"), Mockito.<Position>any());
        type(parseKeyStrokes("'s"));
        verify(cursorAndSelection, times(1)).getMark("s");

        // Delete "all matching/paired 's' chars"
        checkRemap(TEST_NORMAL, "dams", "dams");

        // sanity check
        checkRemap(TEST_NORMAL, "df0", "df0");

        type(parseKeyStrokes(":onoremap 0 0x<CR>"));
        checkRemap(TEST_NORMAL, "d0", "d0x");
        checkRemap(TEST_NORMAL, "df0", "df0");
    }

    @Test
    public void testPrefixCommandMap() {
        // Sanity check
        checkRemap(TEST_NORMAL, "gq2j", "gq2j");

        // 'q' mapping should only be invoked in operator mode or at start of command.
        type(parseKeyStrokes(":noremap q 2j<CR>"));
        checkRemap(TEST_NORMAL, "gq2j", "gq2j");
        checkRemap(TEST_NORMAL, "gqq", "gq2j");
        checkRemap(TEST_NORMAL, "q", "2j");
    }
    
    @Test
    public void testRemapShouldNotShadeOriginalCommand() {
        // Sanity check
        checkRemap(TEST_NORMAL, "gg", "gg");
        checkRemap(TEST_NORMAL, "G", "G");

        // Both commands should work
        type(parseKeyStrokes(":noremap gr G<CR>"));
        checkRemap(TEST_NORMAL, "gg<ESC>", "gg<ESC>");
        checkRemap(TEST_NORMAL, "gr<ESC>", "G<ESC>");
        checkRemap(TEST_NORMAL, "ggrb<ESC>", "ggrb<ESC>");
        checkRemap(TEST_NORMAL, "gg<ESC>", "gg<ESC>");
        checkRemap(TEST_NORMAL, "grgg<ESC>", "Ggg<ESC>");
        checkRemap(TEST_NORMAL, "grgggr<ESC>", "GggG<ESC>");
        checkRemap(TEST_NORMAL, "grxggxgr<ESC>", "GxggxG<ESC>");
    }

    @Test
    public void testRemapZero() {
        // Sanity checks
        checkRemap(TEST_NORMAL, "d0", "d0");
        checkRemap(TEST_NORMAL, "d^", "d^");

        type(parseKeyStrokes(":map 0 ^<CR>"));
        checkRemap(TEST_NORMAL, "d0", "d^");
        checkRemap(TEST_NORMAL, "df0", "df0");
        checkRemap(TEST_NORMAL, "d10l", "d10l");

        /*
         * Copied from NormalModeTests.testPercent
         */
        // Shouldn't do anything with regards to remaps
        checkRemap(TEST_NORMAL, "500%", "500%");
        checkRemap(TEST_NORMAL, "100%", "100%");
    }

    @Test
    public void testInsertModeRemap() {
        // Quick sanity check
        checkRemap(TEST_INSERT, "ya<esc>", "ya<ESC>");

        // Test that 'jj' quits (only after remapping)
        checkRemap(TEST_INSERT, "jkjjlh<ESC>", "jkjjlh<ESC>");

        type(parseKeyStrokes(":inoremap jj <LT>ESC<GT><CR>")); // Double escaping for test key parse
        checkRemap(TEST_INSERT, "jkjj", "jk<ESC>");

        // Sanity check
        checkRemap(TEST_INSERT, "jkklhjj", "jkklh<ESC>");

        type(parseKeyStrokes(":inoremap kk <LT>ESC<GT><CR>"));
        // Test that initially failed 'jj' mapping allows kk mapping to be detected by backtracking
        checkRemap(TEST_INSERT, "jkk", "j<ESC>");
    }

    @Test
    public void testRecursiveRemap() {
        type(parseKeyStrokes(":nnoremap Z ex<CR>"));
        type(parseKeyStrokes(":nmap gz ggZbx<CR>"));
        checkRemap(TEST_NORMAL, "gz", "ggexbx");
    }

    @Test
    public void testRecursiveRemapStops() {
        type(parseKeyStrokes(":nmap Z lxgz<CR>"));
        type(parseKeyStrokes(":nmap gz g_xZ<CR>"));
        checkRemap(TEST_NORMAL, "gz", "g_xlx");
    }

    @Test
    public void testPrefixKeys() {
        type(parseKeyStrokes(":nnoremap ,s gg<CR>"));
        type(parseKeyStrokes(":nnoremap ,ss G<CR>"));
        checkRemap(TEST_NORMAL, ",ss", "G");
        checkRemap(TEST_NORMAL, ",s<SPACE>", "gg<SPACE>");
    }

    @Test
    public void testPrefixKeysInsert() {
        // Use ',' key as leader for easier testing
        type(parseKeyStrokes(":inoremap ,s start<CR>"));
        type(parseKeyStrokes(":inoremap ,ss session<CR>"));
        checkRemap(TEST_INSERT, ",ss", "session");
        checkRemap(TEST_INSERT, ",s<SPACE>", "start<SPACE>");
    }

    @Test
    public void testBacktrackingTriggersRemap() {
        // This will only match partially
        type(parseKeyStrokes(":nnoremap v- v$<CR>"));
        // Remap in visual mode: indent and go to end of line
        type(parseKeyStrokes(":vnoremap > >$<CR>"));

        // This should invoke the '>' visual mapping without any errors or weirdness
        checkCommand(forKeySeq("v>"), "", 's', "tart\n", "\tstar", 't', "\n");
    }

    public void checkRemap(String fakeMode, String inputKeyStrokes, String outputKeyStrokes) {
        try {
            super.adaptor.changeModeSafely(fakeMode);
            KeyStrokeRecordingMode.INPUT.clear();

            Iterable<KeyStroke> inputCollection = parseKeyStrokes(inputKeyStrokes);
            // Do a round-trip to make sure that all keys can be represented
            Iterable<KeyStroke> outputCollection = parseKeyStrokes(outputKeyStrokes);
            if ( ! outputKeyStrokes.equals(ConstructorWrappers.keyStrokesToString(outputCollection))) {
                throw new IllegalArgumentException("Could not convert [" + outputKeyStrokes
                        + "] to keystrokes, round-trip failed.");
            }
            // Set content to some gibberish scratch space
            content.setText("a\nb\nc\nd\ne\nf\ng\nh\ni\nj\nk\nl\n");

            type(inputCollection);

            String capturedKeys = ConstructorWrappers.keyStrokesToString(KeyStrokeRecordingMode.INPUT);
            assertEquals("Remap input [" + inputKeyStrokes + "] did not give expected result",
                    outputKeyStrokes, capturedKeys);
        } finally {
            adaptor.changeModeSafely(NormalMode.NAME);
        }
    }
}
