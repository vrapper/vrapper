package net.sourceforge.vrapper.core.tests.cases;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;

import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

public class SearchModeTests extends CommandTestCase {

    @Override
    public void setUp() {
        super.setUp();
        registerManager = new DefaultRegisterManager();
        when(platform.getSearchAndReplaceService()).thenReturn(searchService);
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
}
