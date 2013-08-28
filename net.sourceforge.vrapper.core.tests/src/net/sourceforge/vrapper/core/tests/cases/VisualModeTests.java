package net.sourceforge.vrapper.core.tests.cases;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;

// FIXME: needs testing with different values of 'selection' variable
// (it affects most of the tests)

public class VisualModeTests extends CommandTestCase {

    @Override
	public void setUp() {
		super.setUp();
	};

	private void prepareEditor(boolean inverted,
            String beforeSelection, String selected, String afterSelection) {
        String initialContent = beforeSelection + selected + afterSelection;

    	content.setText(initialContent);
    	int selectFrom, selectTo;
    	selectFrom = selectTo = beforeSelection.length();
    	if (!inverted) {
            selectTo += selected.length();
        } else {
            selectFrom += selected.length();
        }

        adaptor.changeModeSafely(VisualMode.NAME);

    	CursorService cursorService = platform.getCursorService();
    	SelectionService selectionService = platform.getSelectionService();
        Position from = cursorService.newPositionForModelOffset(selectFrom);
        if (selected.endsWith("\n")) {
            Position to = cursorService.newPositionForModelOffset(selectTo - 1);
            selectionService.setSelection(new LineWiseSelection(adaptor, from, to));
        } else {
            Position to = cursorService.newPositionForModelOffset(selectTo);
            selectionService.setSelection(new SimpleSelection(new StartEndTextRange(from, to)));
        }
    }


    private void assertCommandResult(String initialLine,
            boolean inverted, String beforeSelection, String selected, String afterSelection) {
            String expectedFinalContent = beforeSelection + selected + afterSelection;
    		String actualFinalContent = content.getText();
    		int actSelFrom;
    		int actSelTo;
            TextRange selection = adaptor.getSelection();
            if (selection != null) {
                actSelFrom = selection.getStart().getModelOffset();
                actSelTo = selection.getEnd().getModelOffset();
            } else {
                actSelFrom = actSelTo = adaptor.getCursorService().getPosition().getModelOffset();
            }
    		int expSelTo, expSelFrom;
    		expSelFrom = expSelTo = beforeSelection.length();
    		if (!inverted) {
                expSelTo += selected.length();
            } else {
                expSelFrom += selected.length();
            }

    		String msg = "";
    		boolean selectionMishmash = false;
    		if (expSelFrom != actSelFrom || expSelTo != actSelTo) {
    			msg = "selection mishmash\n" + expSelFrom + " " + expSelTo + " but got " + actSelFrom + " " + actSelTo + "\n";
    			selectionMishmash = true;
    		}

    //		int offset = mockEditorAdaptor.getCaretOffset();
    		String expectedLine = formatLine(beforeSelection, selected, afterSelection) + "\n";// + cursorLine(expSelTo);
    		String   actualLine = formatLine(actualFinalContent,
    				min(actSelFrom, actSelTo),
    				max(actSelFrom, actSelTo)) + "\n";// + cursorLine(offset);

    		msg += String.format("STARTING FROM:\n%s\nEXPECTED:\n%s\nGOT:\n%s\n", initialLine, expectedLine, actualLine);
    		if (!actualFinalContent.equals(expectedFinalContent) || selectionMishmash) {
                fail(msg);
            }
        }


    private void checkCommand(Command command,
			boolean inverted1, String beforeSelection1, String selected1, String afterSelection1,
			boolean inverted2, String beforeSelection2, String selected2, String afterSelection2) {

		String  initialLine = formatLine(beforeSelection1, selected1, afterSelection1) + "\n"; // + cursorLine(selectTo);

		prepareEditor(inverted1, beforeSelection1, selected1, afterSelection1);
		executeCommand(command);
		assertCommandResult(initialLine, inverted2, beforeSelection2, selected2, afterSelection2);
	}

    private void checkLeavingCommand(Command command,
			boolean inverted, String beforeSelection, String selected, String afterSelection,
			String beforeCursor, char atCursor, String afterCursor) {
		String  initialLine = formatLine(beforeSelection, selected, afterSelection) + "\n"; // + cursorLine(selectTo);

		prepareEditor(inverted, beforeSelection, selected, afterSelection);
		executeCommand(command);
        assertCommandResult(initialLine, beforeCursor, atCursor, afterCursor);
    }

	protected static String cursorLine(int offset) {
		StringBuilder cursorLine = new StringBuilder(">");
		int start = offset == 0 ? 1 : 0;
		for (int i=start; i<=offset; i++) {
            cursorLine.append(' ');
        }
		cursorLine.append("^\n");
		return cursorLine.toString();
	}

	@Test public void testMotionsInVisualMode() {
		checkCommand(forKeySeq("w"),
				false, "","Al","a ma kota",
				false, "","Ala ","ma kota");
		checkCommand(forKeySeq("w"),
				true,  "","Ala ma k","ota",
				true, "Ala ","ma k","ota");
		checkCommand(forKeySeq("w"),
				true,  "A","lamak","ota i psa",
				false, "Alama","kota ","i psa");
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
	}

	@Test public void testCommandsInVisualMode() throws Exception {
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
        checkCommand(forKeySeq("p"),
                false, "The internet is ","awesome","!",
                false, "The internet is a series of tube","","s!");
        verify(adaptor).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "\t\ta series of tubes\n"));
        checkCommand(forKeySeq("p"),
                true, "The internet is ","awesome","!",
                true, "The internet is \n\t\t","","a series of tubes\n!");
        verify(adaptor, times(2)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");

        defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "a series of tubes\n"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        verify(adaptor, times(3)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("p"),
                false, "The internet is \n","awesome\n","!",
                false, "The internet is \n","","a series of tubes\n!");
        verify(adaptor, times(4)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.LINES, "awesome\n");

        defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, "a series of tubes"));
        checkCommand(forKeySeq("2p"),
                false, "The internet is ","awesome","!",
                false, "The internet is a series of tubesa series of tube","","s!");
        verify(adaptor, times(5)).changeMode(NormalMode.NAME);
        assertYanked(ContentType.TEXT, "awesome");
    }

    @Test public void visualModeShouldHaveAName() {
		adaptor.changeModeSafely(VisualMode.NAME);
		assertEquals("visual mode", adaptor.getCurrentModeName());
	}

	@Test public void visualModeShouldEnterPainlesslyAndDeselectOnLeave() throws Exception {
	    CursorService cursorService = platform.getCursorService();
	    Position position = cursorService.newPositionForModelOffset(42);
	    cursorService.setPosition(position, true);
		adaptor.changeMode(NormalMode.NAME);
		adaptor.changeMode(VisualMode.NAME);
		// verify that selection has been cleared. getSelection will return non-null!
		verify(adaptor).setSelection(null);
	}

	@Test public void testTextObjects() {
		checkCommand(forKeySeq("iw"),
				false,  "It's Some","th","ing interesting.",
				false,  "It's ","Something"," interesting.");
    }

	@Test
	public void test_fMotions() {
		checkCommand(forKeySeq("fs"),
				false,  "Ther","e"," was a bug about it",
				false,  "Ther","e was"," a bug about it");
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
		
		checkLeavingCommand(forKeySeq("~"),
				false,  "with ","some\nCAPITAL"," letters",
				"with ",'S',"OME\ncapital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some CAPITAL"," letters",
				"with ",'S',"OME capital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some\nCAPITAL"," letters",
				"with ",'S',"OME\ncapital letters");
	}
	
	@Test
	public void test_CtrlC_exits() {
		checkLeavingCommand(forKeySeq("<C-c>"), true,
				"test", "123", "test",
				"test", '1', "23test");
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}

}
