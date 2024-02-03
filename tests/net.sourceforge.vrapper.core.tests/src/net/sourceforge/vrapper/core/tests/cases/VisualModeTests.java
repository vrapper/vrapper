package net.sourceforge.vrapper.core.tests.cases;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.VisualTestCase;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;

public class VisualModeTests extends VisualTestCase {

    @Test
    public void testCommandsInVisualMode() throws Exception {
        checkCommand(forKeySeq("o"),
                false, "A","la"," ma kota",
                true,  "A","la"," ma kota");
        checkCommand(forKeySeq("o"),
                true,  "A","la"," ma kota",
                false, "A","la"," ma kota");

        // FIXME:
        // it's broken in test case, works quite well
        // in real eclipse
        checkCommand(forKeySeq("x"),
                false, "A","la"," ma kota",
                false, "A",""," ma kota");
        verify(adaptor).changeMode(NormalMode.NAME);

        checkCommand(forKeySeq("d"),
                true,  "A","LA"," MA kota",
                true,  "A",""," MA kota");
        verify(adaptor, times(2)).changeMode(NormalMode.NAME);

        checkLeavingCommand(forKeySeq("y"), true,
                "A", "LA", " MA kota",
                "A", 'L', "A MA kota");
        verify(adaptor, times(3)).changeMode(NormalMode.NAME);

        checkCommand(forKeySeq("s"),
                true,  "A","LA"," MA kota",
                true,  "A",""," MA kota");
        // TODO: obtain correct arguments used by by ChangeOperation when changing mode
        //		verify(adaptor).changeMode(InsertMode.NAME);
    }

    @Test
    public void testPastingInVisualMode() throws Exception {
        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkLeavingCommand(forKeySeq("p"),
                false, "The internet is ","awesome","!",
                "The internet is a series of tube",'s',"!");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "\t\ta series of tubes\n"));
        checkLeavingCommand(forKeySeq("p"),
                true, "The internet is ","awesome","!",
                "The internet is \n\t\t",'a'," series of tubes\n!");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "a series of tubes\n"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("2p"),
                false, "The internet is ","awesome","!",
                false, "The internet is a series of tubesa series of tube","","s!");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        assertYanked(ContentType.TEXT, "awesome");
    }

    @Test
    public void visualModeShouldHaveAName() {
        adaptor.changeModeSafely(VisualMode.NAME);
        assertEquals("visual mode", adaptor.getCurrentModeName());
    }

    @Test
    public void visualModeShouldEnterPainlesslyAndDeselectOnLeave() throws Exception {
        CursorService cursorService = platform.getCursorService();
        Position position = cursorService.newPositionForModelOffset(42);
        cursorService.setPosition(position, StickyColumnPolicy.ON_CHANGE);
        adaptor.changeMode(NormalMode.NAME);
        adaptor.changeMode(VisualMode.NAME);
        adaptor.handleKey(new SimpleKeyStroke(SpecialKey.ESC));
        // Verify that selection has been cleared. getSelection will return non-null!
        // setSelection must have been called 2 times, once for entering, once for leaving visual.
        verify(adaptor, times(2)).setSelection(null);
    }

    @Test
    public void visualModeShouldEnterPainlesslyAndDeselectOnLeaveVisualCommand() throws Exception {
        CursorService cursorService = platform.getCursorService();
        Position position = cursorService.newPositionForModelOffset(42);
        cursorService.setPosition(position, StickyColumnPolicy.ON_CHANGE);
        adaptor.changeMode(NormalMode.NAME);
        adaptor.changeMode(VisualMode.NAME);
        adaptor.handleKey(new SimpleKeyStroke(':'));
        adaptor.handleKey(new SimpleKeyStroke(SpecialKey.ESC));
        // Verify that selection has been cleared. getSelection will return non-null!
        // Again, setSelection must be called 2 times, once for entering, once for leaving visual.
        verify(adaptor, times(2)).setSelection(null);
    }

    @Test
    public void testTextObjects() {
        checkCommand(forKeySeq("iw"),
                false,  "It's Some","th","ing interesting.",
                false,  "It's ","Something"," interesting.");
        checkCommand(forKeySeq("aw"),
                false,  "It's Some","th","ing interesting.",
                false,  "It's ","Something ","interesting.");
        checkCommand(forKeySeq("i'"),
                false,  "It's 'Some","th","ing' interesting.",
                false,  "It's '","Something","' interesting.");
        checkCommand(forKeySeq("2i'"),
                false,  "It's 'Some","th","ing' interesting.",
                false,  "It's ","'Something'"," interesting.");
        checkCommand(forKeySeq("3i'"),
                false,  "It's 'Some","th","ing' interesting.",
                false,  "It's ","'Something'"," interesting.");
        checkCommand(forKeySeq("a'"),
                false,  "It's 'Some","th","ing' interesting.",
                false,  "It's ","'Something' ","interesting.");
        checkCommand(forKeySeq("a'"),
                false,  "It's 'Some","th","ing'  interesting.",
                false,  "It's ","'Something'  ","interesting.");
        checkCommand(forKeySeq("a'"),
                false,  "It's Something 'i","nt","eresting'\nisn't it?",
                false,  "It's Something ","'interesting'","\nisn't it?");
        checkCommand(forKeySeq("a'"),
                false,  "It's Something 'i","nt","eresting'  \nisn't it?",
                false,  "It's Something ","'interesting'  ","\nisn't it?");
        checkCommand(forKeySeq("a'"),
                false,  "It's Something 'i","nt","eresting'",
                false,  "It's Something ","'interesting'","");
        checkCommand(forKeySeq("3a'"),
                false,  "It's Something 'i","nt","eresting'  isn't it?",
                false,  "It's Something ","'interesting'  ","isn't it?");
    }

    @Test
    public void test_fMotions() {
        checkCommand(forKeySeq("fs"),
                false,  "Ther","e"," was a bug about it",
                false,  "Ther","e was"," a bug about it");
    }

    @Test
    public void test_tMotions() {
        // Check repeated use of t motion in visual mode.
        registerManager = new DefaultRegisterManager();
        reloadEditorAdaptor();

        checkCommand(forKeySeq("t)"),
                false,  "getText(","line.getEndOffset","() - line.getBeginOffset()));",
                false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));");
        checkCommand(forKeySeq("t)"),
                false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));",
                false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));");
        checkCommand(forKeySeq(";"),
                false,  "getText(","line.getEndOffset(",") - line.getBeginOffset()));",
                false,  "getText(","line.getEndOffset() - line.getBeginOffset(",")));");
    }

    @Test
    public void testSwitchSelectionSides() throws CommandExecutionException {
        checkCommand(forKeySeq("o"),
                false,  "getText(","line.getEndOffset","() - line.getBeginOffset()));",
                true,  "getText(","line.getEndOffset","() - line.getBeginOffset()));");
        
        checkCommand(forKeySeq("o"),
                true,  "getText(","line.getEndOffset","() - line.getBeginOffset()));",
                false,  "getText(","line.getEndOffset","() - line.getBeginOffset()));");

        // Sanity check: see if switch to line-wise visual works as expected.
        checkCommand(forKeySeq("V"),
                false,  "Hell","o,","\nWorld!\n;-)",
                false,  "","Hello,\n","World!\n;-)");

        // Switch sides does nothing for single line.
        // Always change back to visual mode to fix the selection.
        adaptor.changeMode(VisualMode.NAME);
        checkCommand(forKeySeq("Vo"),
                false, "Hell","o,","\nWorld!\n;-)",
                false, "","Hello,\n","World!\n;-)");

        adaptor.changeMode(VisualMode.NAME);
        checkCommand(forKeySeq("Vj"),
                false, "Hell","o,","\nWorld!\n;-)",
                false, "","Hello,\nWorld!\n",";-)");
        adaptor.changeMode(VisualMode.NAME);
        checkCommand(forKeySeq("Vjo"),
                false, "Hell","o,","\nWorld!\n;-)",
                true, "","Hello,\nWorld!\n",";-)");
        adaptor.changeMode(VisualMode.NAME);
        checkCommand(forKeySeq("Vjoo"),
                false, "Hell","o,","\nWorld!\n;-)",
                false, "","Hello,\nWorld!\n",";-)");
    }

    @Test
    public void test_J() {
        checkLeavingCommand(forKeySeq("J"),
                false,  "Hell","o,\nW","orld!\n;-)",
                "Hello,",' ',"World!\n;-)");
        checkLeavingCommand(forKeySeq("gJ"),
                false,  "new Hell","o\nW","orld();\n//;-)",
                "new Hello",'W',"orld();\n//;-)");
        checkLeavingCommand(forKeySeq("J"),
                false,  "","\n\nh","ello",
                "",'h',"ello");
    }

    @Test
    public void test_tilde() {
        checkLeavingCommand(forKeySeq("~"),
                false,  "with ","some CAPITAL"," letters",
                "with ",'S',"OME capital letters");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());

        checkLeavingCommand(forKeySeq("~"),
                false,  "with ","some\nCAPITAL"," letters",
                "with ",'S',"OME\ncapital letters");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());

        checkLeavingCommand(forKeySeq("~"),
                true,  "with ","some CAPITAL"," letters",
                "with ",'S',"OME capital letters");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());

        checkLeavingCommand(forKeySeq("~"),
                true,  "with ","some\nCAPITAL"," letters",
                "with ",'S',"OME\ncapital letters");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    @Test
    public void test_CtrlC_exits() {
        checkLeavingCommand(forKeySeq("<C-c>"), true,
                "test", "123", "test",
                "test", '1', "23test");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    @Test
    public void test_ShiftWidth() {
        when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
        when(configuration.get(Options.TAB_STOP)).thenReturn(4);
        when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(4);

        checkLeavingCommand(forKeySeq(">"),
                false, "","    Hello,\n    W","orld!\n;-)",
                "        ",'H',"ello,\n        World!\n;-)");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        checkLeavingCommand(forKeySeq(">"),
                false, "    ","Hello,\n    W","orld!\n;-)",
                "        ",'H',"ello,\n        World!\n;-)");
        checkLeavingCommand(forKeySeq(">"),
                false, "   "," Hello,\n   "," World!\n;-)",
                "        ",'H',"ello,\n        World!\n;-)");
        checkLeavingCommand(forKeySeq(">"),
                false, "   "," Hello,\n","    World!\n;-)",
                "        ",'H',"ello,\n    World!\n;-)");
        checkLeavingCommand(forKeySeq(">"),
                false, "   "," Hello,\n    World!\n","\n;-)",
                "        ",'H',"ello,\n        World!\n\n;-)");
        checkLeavingCommand(forKeySeq(">"),
                false, "   "," Hello,\n    World!\n  ","\n;-)",
                "        ",'H',"ello,\n        World!\n  \n;-)");
    }

    @Test
    public void test_RepeatToExpand() {
        checkCommand(forKeySeq("a'"),
                false, "'String'Quote'String' ","A","lASim'Quote'String'",
                false, "'String'Quote'String","' AlASim'","Quote'String'");
        // FIXME: Vim actually reacts differently.
        checkCommand(forKeySeq("a'a'"),
                false, "'String'Quote'String' ","A","lASim'Quote'String'",
                false, "'String'Quote'String' AlASim","'Quote'","String'");
        checkCommand(forKeySeq("i'"),
                false, "'String'Quote'String' ","Al","ASim'Quote'String'",
                false, "'String'Quote'String'"," AlASim","'Quote'String'");
        // There was a bug where repeatedly running i' would cause an endless loop.
        // FIXME: Vim actually reacts differently - it might select the quotes as well.
        checkCommand(forKeySeq("i'i'"),
                false, "'String'Quote'String' ","Al","ASim'Quote'String'",
                false, "'String'Quote'String'"," AlASim","'Quote'String'");

        checkCommand(forKeySeq("i}"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{String{Quote{String{"," AlASim","}Quote}String}There}");
        checkCommand(forKeySeq("i}i}"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{String{Quote{","String{ AlASim}Quote","}String}There}");
        checkCommand(forKeySeq("i}i}i}"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{String{","Quote{String{ AlASim}Quote}String","}There}");
        checkCommand(forKeySeq("i}i}i}i}"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{","String{Quote{String{ AlASim}Quote}String}There","}");

        checkCommand(forKeySeq("a{"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{String{Quote{String","{ AlASim}","Quote}String}There}");
        checkCommand(forKeySeq("a{a{"),
                false, "{String{Quote{String{ ","Al","ASim}Quote}String}There}",
                false, "{String{Quote","{String{ AlASim}Quote}","String}There}");
    }

    @Test
    public void test_Indent() throws Exception {
        // Expandtab is false by default in the tests.
        checkLeavingCommand(forKeySeq(">"),
                false, "Okay\n\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t\t", 'T', "his\nShould be indented");
        checkLeavingCommand(forKeySeq("2>"),
                false, "Okay\n\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t\t\t", 'T', "his\nShould be indented");
        checkLeavingCommand(forKeySeq("4>"),
                false, "Okay\n\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t\t\t\t\t", 'T', "his\nShould be indented");

        checkLeavingCommand(forKeySeq(">"),
                false, "Oka", "y\n\t\tThis\nShould b", "e indented",
                "\t", 'O', "kay\n\t\t\tThis\n\tShould be indented");
        checkLeavingCommand(forKeySeq("2>"),
                false, "Oka", "y\n\t\tThis\nShould b", "e indented",
                "\t\t", 'O', "kay\n\t\t\t\tThis\n\t\tShould be indented");
        checkLeavingCommand(forKeySeq("4>"),
                false, "Oka", "y\n\t\tThis\nShould b", "e indented",
                "\t\t\t\t", 'O', "kay\n\t\t\t\t\t\tThis\n\t\t\t\tShould be indented");
        
        when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
        when(configuration.get(Options.TAB_STOP)).thenReturn(4);
        when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(2);
        checkLeavingCommand(forKeySeq(">"),
                false, "Oka", "y\n\t\tThis\nShould b", "e indented",
                "  ", 'O', "kay\n          This\n  Should be indented");
        checkLeavingCommand(forKeySeq("2>"),
                false, "Oka", "y\n\t\tThis\nShould b", "e indented",
                "    ", 'O', "kay\n            This\n    Should be indented");
    }

    @Test
    public void test_Unindent() throws Exception {
        // Expandtab is false by default in the tests.
        checkLeavingCommand(forKeySeq("<"),
                false, "Okay\n\t\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t", 'T', "his\nShould be indented");
        checkLeavingCommand(forKeySeq("2<"),
                false, "Okay\n\t\t\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t", 'T', "his\nShould be indented");
        checkLeavingCommand(forKeySeq("4<"),
                false, "Okay\n\t\t\t\t\t\tTh", "i", "s\nShould be indented",
                "Okay\n\t\t", 'T', "his\nShould be indented");

        checkLeavingCommand(forKeySeq("<"),
                false, "\tOka", "y\n\t\t\tThis\n\tShould b", "e indented",
                "", 'O', "kay\n\t\tThis\nShould be indented");
        checkLeavingCommand(forKeySeq("2<"),
                false, "\t\tOka", "y\n\t\t\t\tThis\n\t\tShould b", "e indented",
                "", 'O', "kay\n\t\tThis\nShould be indented");
        checkLeavingCommand(forKeySeq("4<"),
                false, "\t\t\t\tOka", "y\n\t\t\t\t\t\tThis\n\t\t\t\tShould b", "e indented",
                "", 'O', "kay\n\t\tThis\nShould be indented");
        
        when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
        when(configuration.get(Options.TAB_STOP)).thenReturn(4);
        when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(2);
        checkLeavingCommand(forKeySeq("<"),
                false, "  Oka", "y\n\t\tThis\n  Should b", "e indented",
                "", 'O', "kay\n      This\nShould be indented");
        checkLeavingCommand(forKeySeq("2<"),
                false, "      Oka", "y\n\t\tThis\n      Should b", "e indented",
                "  ", 'O', "kay\n    This\n  Should be indented");
    }
}
