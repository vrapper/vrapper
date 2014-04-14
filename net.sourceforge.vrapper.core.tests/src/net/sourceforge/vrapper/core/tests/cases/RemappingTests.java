package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for remapping in different modes.
 */
public class RemappingTests extends CommandTestCase {

    @Override
    public void setUp() {
        super.setUp();
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
        type(parseKeyStrokes(":nmapclear<CR>"));
        type(parseKeyStrokes(":omapclear<CR>"));
        type(parseKeyStrokes(":vmapclear<CR>"));
        type(parseKeyStrokes(":imapclear<CR>"));
    }

    @Test
    public void testCounting() {
        type(parseKeyStrokes(":nmap L dd<CR>"));
        checkCommand(forKeySeq("3L"),
                "a", 'b', "c\ndef\nghi\njkl",
                "", 'j', "kl");
    }

    @Test
    @Ignore // This has more to do with the way registers are implemented than keymapping.
    public void testCountingBlackHole() {
        checkCommand(forKeySeq("\"_3dd"),
                "a", 'b', "c\ndef\nghi\njkl",
                "", 'j', "kl");
        checkCommand(forKeySeq("3\"_dd"),
                "a", 'b', "c\ndef\nghi\njkl",
                "", 'j', "kl");
        type(parseKeyStrokes(":noremap dd \"_dd<CR>"));
        checkCommand(forKeySeq("3dd"),
                "a", 'b', "c\ndef\nghi\njkl",
                "", 'j', "kl");
    }

    @Test
    @Ignore // Needs some work in KeyStrokeTranslator and related code to do it nicely.
    public void testPrefixTextObject() {
        checkCommand(forKeySeq("di]"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[", ']', "\njkl");
        type(parseKeyStrokes(":noremap ]] j<CR>"));
        VimUtils.BREAKPOINT_TRIGGER++;
        checkCommand(forKeySeq("di]"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[", ']', "\njkl");
        type(parseKeyStrokes(":map ]] j<CR>"));
        checkCommand(forKeySeq("di]"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[", ']', "\njkl");
    }

    @Test
    public void testEndOfLineRemap() {
        // Original bug report used 'L', but that is too difficult to test as L is 'jump to middle'
        type(parseKeyStrokes(":nnoremap l $<CR>"));
        checkCommand(forKeySeq("l"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[ab", 'c', "\ndef\ngh]\njkl");

        // Should use omap, where 'l' isn't defined yet.
        checkCommand(forKeySeq("dl"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[", 'b', "c\ndef\ngh]\njkl");
        type(parseKeyStrokes(":onoremap l $<CR>"));
        checkCommand(forKeySeq("dl"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "", '[', "\ndef\ngh]\njkl");

        // 'l' isn't remapped yet in visual mode. Try before / after.
        checkCommand(forKeySeq("vld"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[", 'c', "\ndef\ngh]\njkl");
        type(parseKeyStrokes(":vnoremap l $<CR>"));
        checkCommand(forKeySeq("vld"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "", '[', "\ndef\ngh]\njkl");
    }

    @Test
    public void testFTMotionsWithoutOMap() {
        // Check that no omap binds are mixed in with the f/F or t/T commands.
        checkCommand(forKeySeq("fL"),
                "old", ' ', "McDonnaLD had some $",
                "old McDonna", 'L', "D had some $");
        type(parseKeyStrokes(":noremap L $<CR>"));
        checkCommand(forKeySeq("L"),
                "[", 'a', "bc\ndef\ngh]\njkl",
                "[ab", 'c', "\ndef\ngh]\njkl");
        checkCommand(forKeySeq("fL"),
                "old", ' ', "McDonnaLD had some $",
                "old McDonna", 'L', "D had some $");
    }
}
