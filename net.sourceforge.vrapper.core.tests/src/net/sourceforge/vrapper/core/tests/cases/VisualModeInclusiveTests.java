package net.sourceforge.vrapper.core.tests.cases;

import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.VisualTestCase;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Before;
import org.junit.Test;

public class VisualModeInclusiveTests extends VisualTestCase {
    
    @Before
    public void makeSelectionInclusive() {
        super.configuration.set(Options.SELECTION, Selection.INCLUSIVE);
    }

    @Test
    public void testMotionsInVisualMode() {
        // FIXME Should always include the first character of the selection
        checkCommand(forKeySeq("w"),
                false, "","Al","a ma kota",
                false, "","Ala m","a kota");
        checkCommand(forKeySeq("w"),
                true,  "","Ala ma k","ota",
                true, "Ala ","ma k","ota");
        checkCommand(forKeySeq("w"),
                true,  "A","lamak","ota i psa",
                false, "Alama","kota i"," psa");
        checkCommand(forKeySeq("e"),
                true,  "A","lamak","ota i psa",
                false, "Alama","kota"," i psa");
        checkCommand(forKeySeq("b"),
                false, "Alama","kota ","i psa",
                true,  "","Alamak","ota i psa");
        checkCommand(forKeySeq("h"),
                false, " ktoto","t","aki ",
                true,  " ktot","ot","aki ");
        checkCommand(forKeySeq("h"),
                true,  " ktoto","t","aki ",
                true,  " ktot","ot","aki ");
        checkCommand(forKeySeq("l"),
                true,  " ktot","ot","aki ",
                false,  " ktoto","t","aki ");
        // undefined behavior, inverse selection over 1 character should not
        // happen anymore
        //		checkCommand(forKeySeq("l"),
        //				true,  " ktoto","t","aki ",
        //				false, " ktotot","","aki ");
        checkCommand(forKeySeq("l"),
                false, " ktoto","t","aki ",
                false, " ktoto","ta","ki ");
        checkCommand(forKeySeq("llll"),
                false, " k","t","ototaki ",
                false, " k","totot","aki ");
        checkCommand(forKeySeq("gE"),
                true, "Alama kot", "a", " i psa",
                true,  "Alam", "a kota", " i psa");
        checkCommand(forKeySeq("gE"),
                false, "Alama kot", "a ", "i psa",
                false,  "Alama kot", "a", " i psa");
    }

    @Test
    public void testMotionWordForwardBackward() {
        checkCommand(forKeySeq("w"),
                false, "Here we ","g","o again.",
                false, "Here we ","go a","gain.");
        executeCommand(forKeySeq("b"));
        assertVisualResult(content.getText(),
                false, "Here we ","g","o again.");
        executeCommand(forKeySeq("b"));
        assertVisualResult(content.getText(),
                true, "Here ","we g","o again.");
        executeCommand(forKeySeq("b"));
        assertVisualResult(content.getText(),
                true, "","Here we g","o again.");
        executeCommand(forKeySeq("w"));
        assertVisualResult(content.getText(),
                true, "Here ","we g","o again.");

        checkCommand(forKeySeq("w"),
                false, "Here we ","g","o again.",
                false, "Here we ","go a","gain.");
        executeCommand(forKeySeq("ge"));
        assertVisualResult(content.getText(),
                false, "Here we ","go"," again.");
        executeCommand(forKeySeq("ge"));
        assertVisualResult(content.getText(),
                true, "Here w","e g","o again.");
        executeCommand(forKeySeq("ge"));
        assertVisualResult(content.getText(),
                true, "Her","e we g","o again.");
    }
    
    @Test
    public void testCursorPosAfterTempVisualModeLeave() throws CommandExecutionException {
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("<ESC>"),
                false,  "He","llo,\nW","orld!\n;-)",
                "Hello,\n",'W',"orld!\n;-)");

        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("<ESC>"),
                true,  "He","llo,\nW","orld!\n;-)",
                "He",'l',"lo,\nWorld!\n;-)");

        
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("y"),
                false,  "He","llo,\nW","orld!\n;-)",
                "He",'l',"lo,\nWorld!\n;-)");

        // s and c are the same in visual
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("s"),
                false,  "He","llo,\nW","orld!\n;-)",
                "He",'o',"rld!\n;-)");

        // d / X and x are the same in visual
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("d"),
                false,  "He","llo,\nW","orld!\n;-)",
                "He",'o',"rld!\n;-)");

        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("gJ"),
                false,  "He","llo,\nW","orld!\n;-)",
                "Hello,",'W',"orld!\n;-)");

        when(defaultRegister.getContent())
                .thenReturn(new StringRegisterContent(ContentType.TEXT, "Here we go again"));

        // Temp visual to insert mode -> put cursor behind "again"
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("P"),
                false,  "He","llo,\nW","orld!\n;-)",
                "HeHere we go again",'o',"rld!\n;-)");

        when(defaultRegister.getContent())
                .thenReturn(new StringRegisterContent(ContentType.LINES, "There she goes!\n"));

        // Temp visual to insert mode -> line paste puts cursor on first non-whitespace character.
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("P"),
                false,  "He","llo,\nW","orld!\n;-)",
                "He\n",'T',"here she goes!\norld!\n;-)");
    }

    @Test
    public void testCursorPosAfterVisualModeLeave() {
        checkLeavingCommand(forKeySeq("<Esc>"), true,
                "test", "123", "test",
                "test", '1', "23test");
        checkLeavingCommand(forKeySeq("<Esc>"), false,
                "test", "123", "test",
                "test12", '3', "test");
        checkLeavingCommand(forKeySeq("<Esc>"), true,
                "test", "1", "23test",
                "test", '1', "23test");
        checkLeavingCommand(forKeySeq("<Esc>"), false,
                "test", "1", "23test",
                "test", '1', "23test");
    }

    @Test
    public void testExtendSelectionTextObject() {
        checkCommand(forKeySeq("iw"),
                false, "","Al","a ma kota",
                false, "","Ala"," ma kota");
        checkCommand(forKeySeq("iw"),
                false, "Al","a"," ma kota",
                false, "","Ala"," ma kota");
        // FIXME Should only select whitespace!
        checkCommand(forKeySeq("iw"),
                false, "Ala"," ","   ma kota",
                false, "","Ala    ma"," kota");
    }
}
