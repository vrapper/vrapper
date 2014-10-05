package net.sourceforge.vrapper.core.tests.cases;

import static org.mockito.Mockito.*;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.vrapper.core.tests.utils.TestSearchService;
import net.sourceforge.vrapper.core.tests.utils.VisualTestCase;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

public class SearchModeTests extends VisualTestCase {

    @Override
    public void setUp() {
        super.setUp();
        registerManager = new DefaultRegisterManager();
        when(platform.getSearchAndReplaceService()).thenReturn(new TestSearchService(content));
        reloadEditorAdaptor();
    }

    @Override
    protected void reloadEditorAdaptor() {
        super.reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
    }

    @Before
    public void clearSearchHistory() {
        registerManager.setSearch(null);
    }

    @Test
    public void testForwardsSearch() {
        checkCommand(forKeySeq("/th<CR>"),
                "I ", 'c', "ouldn't live without this\nfull-range three-linear variable.",
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.");
        checkCommand(forKeySeq("n"),
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.",
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.");
        checkCommand(forKeySeq("N"),
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.",
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.");
    }

    @Test
    public void testRepeatSearch() {
        checkCommand(forKeySeq("/th<CR>"),
                "I ", 'c', "ouldn't live without this\nfull-range three-linear variable.",
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.");
        checkCommand(forKeySeq("/<CR>"),
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.",
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.");
        checkCommand(forKeySeq("?<CR>"),
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.",
                "I couldn't live wi", 't', "hout this\nfull-range three-linear variable.");
    }

    /**
     * Test that the current selection is used if a new search is started, not the previous one.
     * See issue <a href="https://github.com/vrapper/vrapper/issues/500">500</a>.
     */
    @Test
    public void testVisualSearchRepeated() {
        checkCommand(forKeySeq("/th<CR>"),
                false, "I ", "c", "ouldn't live without this\nfull-range three-linear variable.",
                false, "I ", "couldn't live wit", "hout this\nfull-range three-linear variable.");
        // Don't use checkCommand again, it resets a lot of internal state, which is unwanted here.
        type(parseKeyStrokes("<ESC>0fcv/th<CR>"));
        assertVisualResult(content.getText(),
                false, "I ", "couldn't live wit", "hout this\nfull-range three-linear variable.");
        type(parseKeyStrokes("/<CR>"));
        assertVisualResult(content.getText(),
                false, "I ", "couldn't live without t", "his\nfull-range three-linear variable.");
        type(parseKeyStrokes("/<CR>"));
        assertVisualResult(content.getText(),
                false, "I ", "couldn't live without this\nfull-range t", "hree-linear variable.");
    }

    @Test
    public void testSearchFlags() {
        checkCommand(forKeySeq("/th/e<CR>"),
                "I ", 'c', "ouldn't live without this\nfull-range three-linear variable.",
                "I couldn't live wit", 'h', "out this\nfull-range three-linear variable.");
        checkCommand(forKeySeq("n"),
                "I couldn't live wit", 'h', "out this\nfull-range three-linear variable.",
                "I couldn't live without t", 'h', "is\nfull-range three-linear variable.");

        checkCommand(forKeySeq("/th/e+2<CR>"),
                "I ", 'c', "ouldn't live without this\nfull-range three-linear variable.",
                "I couldn't live witho", 'u', "t this\nfull-range three-linear variable.");
        checkCommand(forKeySeq("n"),
                "I couldn't live witho", 'u', "t this\nfull-range three-linear variable.",
                "I couldn't live without thi", 's', "\nfull-range three-linear variable.");
    }

    @Test
    public void testSearchCase() {
        checkCommand(forKeySeq("/vrap\\C<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, vRaPpEr, vRAPPER, ", 'v', "rapper, Vrapper");
        checkCommand(forKeySeq("/vrap\\c<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, ", 'v', "RaPpEr, vRAPPER, vrapper, Vrapper");

        when(configuration.get(Options.IGNORE_CASE)).thenReturn(false);
        checkCommand(forKeySeq("/vrap<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, vRaPpEr, vRAPPER, ", 'v', "rapper, Vrapper");

        when(configuration.get(Options.IGNORE_CASE)).thenReturn(true);
        checkCommand(forKeySeq("/vrap<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, ", 'v', "RaPpEr, vRAPPER, vrapper, Vrapper");
        
        when (configuration.get(Options.SMART_CASE)).thenReturn(true);
        checkCommand(forKeySeq("/vrap<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, ", 'v', "RaPpEr, vRAPPER, vrapper, Vrapper");

        checkCommand(forKeySeq("/vRAP<CR>"),
                "v", 'r', "apper, vRaPpEr, vRAPPER, vrapper, Vrapper",
                "vrapper, vRaPpEr, ", 'v', "RAPPER, vrapper, Vrapper");
    }

    @Test
    public void testBackslashesInSearch() {
        checkCommand(forKeySeq("/th\\/e<CR>"),
                "I ", 'c', "ouldn't live without this sixth/e\nfull-range three-linear variable.",
                "I couldn't live without this six", 't', "h/e\nfull-range three-linear variable.");
        checkCommand(forKeySeq("/th\\\\c<CR>"),
                "I ", 'c', "ouldn't live without th\\cis\nfull-range three-linear variable.",
                "I couldn't live without ", 't', "h\\cis\nfull-range three-linear variable.");

        // Combination search flag and backslash
        checkCommand(forKeySeq("/t\\/h/e+2<CR>"),
                "I ", 'c', "ouldn't live wit/hout this\nfull-range t/hree-linear variable.",
                "I couldn't live wit/ho", 'u', "t this\nfull-range t/hree-linear variable.");
        checkCommand(forKeySeq("n"),
                "I couldn't live wit/ho", 'u', "t this\nfull-range t/hree-linear variable.",
                "I couldn't live wit/hout this\nfull-range t/hr", 'e', "e-linear variable.");
    }

    @Test
    public void testBackwardsSearch() {
        checkCommand(forKeySeq("?th<CR>"),
                "I couldn't live without this\nfull-range three-linear ", 'v', "ariable.",
                "I couldn't live without this\nfull-range ", 't', "hree-linear variable.");
        checkCommand(forKeySeq("n"),
                "I couldn't live without this\nfull-range ", 't', "hree-linear variable.",
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.");
        checkCommand(forKeySeq("N"),
                "I couldn't live without ", 't', "his\nfull-range three-linear variable.",
                "I couldn't live without this\nfull-range ", 't', "hree-linear variable.");
    }

    @Test
    public void testFindCurrentWord() {
        checkCommand(forKeySeq("*"),
                "Ala ",'m', "a kota ma duo",
                "Ala ma kota ",'m', "a duo");
        // Word not found
        checkCommand(forKeySeq("*"),
                "Ala ",'m', "a kota",
                "Ala ",'m', "a kota");
    }

    @Test
    public void testFindCurrentWordBackwards() {
        checkCommand(forKeySeq("#"),
                "Ala ma kota ",'m', "a duo",
                "Ala ",'m', "a kota ma duo");
        // Word not found
        checkCommand(forKeySeq("#"),
                "Ala ",'m', "a kota",
                "Ala ",'m', "a kota");
    }

    @Test
    public void testFindCurrentWordSearchCase() {
        when(configuration.get(Options.IGNORE_CASE)).thenReturn(false);
        checkCommand(forKeySeq("*"),
                "",'c', "amelCase CamelCase camelcase",
                "",'c', "amelCase CamelCase camelcase");
        checkCommand(forKeySeq("*"),
                "",'c', "amelCase CamelCase camelcase camelCase",
                "camelCase CamelCase camelcase ",'c', "amelCase");

        when(configuration.get(Options.IGNORE_CASE)).thenReturn(true);

        when (configuration.get(Options.SMART_CASE)).thenReturn(false);
        checkCommand(forKeySeq("*"),
                "",'c', "amelCase CamelCase camelcase",
                "camelCase ",'C', "amelCase camelcase");

        // This command should ignore smart case
        when (configuration.get(Options.SMART_CASE)).thenReturn(true);
        checkCommand(forKeySeq("*"),
                "",'c', "amelCase CamelCase camelcase",
                "camelCase ",'C', "amelCase camelcase");
    }
}
