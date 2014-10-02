package net.sourceforge.vrapper.core.tests.cases;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.VisualTestCase;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.TempVisualMode;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Before;
import org.junit.Test;

public class VisualModeExclusiveTests extends VisualTestCase {
    
    @Before
    public void makeSelectionInclusive() {
        super.configuration.set(Options.SELECTION, Selection.EXCLUSIVE);
    }

    @Test
    public void testMotionsInVisualMode() {
        checkCommand(forKeySeq("w"),
                false, "","Al","a ma kota",
                false, "","Ala ","ma kota");
        checkCommand(forKeySeq("w"),
                false, "","Ala"," ma kota",
                false, "","Ala ","ma kota");
        checkCommand(forKeySeq("w"),
                false, "","Ala ","ma kota",
                false, "","Ala ma ","kota");
        checkCommand(forKeySeq("w"),
                true,  "","Ala ma k","ota",
                true, "Ala ","ma k","ota");
        checkCommand(forKeySeq("w"),
                true,  "A","lamak","ota i psa",
                false, "Alamak","ota ","i psa");
        checkCommand(forKeySeq("e"),
                true,  "A","lamak","ota i psa",
                false, "Alamak","ota"," i psa");
        checkCommand(forKeySeq("b"),
                false, "Alama","kota ","i psa",
                true,  "","Alama","kota i psa");
        checkCommand(forKeySeq("h"),
                false, " ktoto","t","aki ",
                false,  " ktoto","","taki ");
        checkCommand(forKeySeq("h"),
                true,  " ktoto","t","aki ",
                true,  " ktot","ot","aki ");
        checkCommand(forKeySeq("l"),
                true,  " ktot","ot","aki ",
                true,  " ktoto","t","aki ");
        checkCommand(forKeySeq("l"),
                true,  " ktoto","t","aki ",
                false, " ktotot","","aki ");
        checkCommand(forKeySeq("l"),
                false, " ktoto","t","aki ",
                false, " ktoto","ta","ki ");
        checkCommand(forKeySeq("llll"),
                false, " k","t","ototaki ",
                false, " k","totot","aki ");
    }
    
    @Test
    public void testMotionWordForwardBackward() {
        checkCommand(forKeySeq("w"),
                false, "Here we ","","go again.",
                false, "Here we ","go ","again.");
        executeCommand(forKeySeq("b"));
        assertVisualResult(content.getText(),
                false, "Here we ","","go again.");
    }
    
    @Test
    public void testCursorPosAfterTempVisualModeLeave() throws CommandExecutionException {
        adaptor.changeMode(TempVisualMode.NAME);
        checkLeavingCommand(forKeySeq("<ESC>"),
                false,  "He","llo,\nW","orld!\n;-)",
                "Hello,\nW",'o',"rld!\n;-)");

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
                "test123", 't', "est");
        checkLeavingCommand(forKeySeq("<Esc>"), true,
                "test", "", "123test",
                "test", '1', "23test");
        checkLeavingCommand(forKeySeq("<Esc>"), true,
                "test", "123test", "",
                "test", '1', "23test");
        checkLeavingCommand(forKeySeq("<Esc>"), false,
                "test", "123test", "",
                "test123tes", 't', "");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    @Test
    public void testExtendSelectionTextObject() {
        checkCommand(forKeySeq("iw"),
                false, "Al","","a ma kota",
                false, "","Ala"," ma kota");
        // FIXME Should only select whitespace, now wrongly selects first word!
        checkCommand(forKeySeq("iw"),
                false, "Ala",""," ma kota",
                false, "","Ala"," ma kota");
        // FIXME Should select 'ma', instead !
        checkCommand(forKeySeq("iw"),
                false, "Ala ","","ma kota",
                false, "","Ala ma"," kota");
    }
}
