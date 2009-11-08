package net.sourceforge.vrapper.core.tests.cases;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

import org.junit.Test;

// FIXME: needs testing with different values of 'selection' variable
// (it affects most of the tests)

public class VisualModeTests extends CommandTestCase {

    @Override
	public void setUp() {
		super.setUp();
		mode = new VisualMode(adaptor);
	};


	private void checkCommand(Command command,
			boolean inverted1, String beforeSelection1, String selected1, String afterSelection1,
			boolean inverted2, String beforeSelection2, String selected2, String afterSelection2) {

		String initialContent = beforeSelection1 + selected1 + afterSelection1;
		String expectedFinalContent = beforeSelection2 + selected2 + afterSelection2;

		content.setText(initialContent);
		int selectFrom, selectTo;
		selectFrom = selectTo = beforeSelection1.length();
		if (!inverted1) {
            selectTo += selected1.length();
        } else {
            selectFrom += selected1.length();
        }

		adaptor.changeMode(VisualMode.NAME);
		CursorService cursorService = platform.getCursorService();
		SelectionService selectionService = platform.getSelectionService();
		selectionService.setSelection(new SimpleSelection(new StartEndTextRange(
		                cursorService.newPositionForModelOffset(selectFrom),
		                cursorService.newPositionForModelOffset(selectTo))));
		try {
            command.execute(adaptor);
        } catch (CommandExecutionException e) {
            fail("exception during command execution: " + e.getMessage());
        }
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
		expSelFrom = expSelTo = beforeSelection2.length();
		if (!inverted2) {
            expSelTo += selected2.length();
        } else {
            expSelFrom += selected2.length();
        }

		String msg = "";
		boolean selectionMishmash = false;
		if (expSelFrom != actSelFrom || expSelTo != actSelTo) {
			msg = "selection mishmash\n" + expSelFrom + " " + expSelTo + " but got " + actSelFrom + " " + actSelTo + "\n";
			selectionMishmash = true;
		}

//		int offset = mockEditorAdaptor.getCaretOffset();
		String  initialLine = formatLine(beforeSelection1, selected1, afterSelection1) + "\n"; // + cursorLine(selectTo);
		String expectedLine = formatLine(beforeSelection2, selected2, afterSelection2) + "\n";// + cursorLine(expSelTo);
		String   actualLine = formatLine(actualFinalContent,
				min(actSelFrom, actSelTo),
				max(actSelFrom, actSelTo)) + "\n";// + cursorLine(offset);

		msg += String.format("STARTING FROM:\n%s\nEXPECTED:\n%s\nGOT:\n%s\n", initialLine, expectedLine, actualLine);
		if (!actualFinalContent.equals(expectedFinalContent) || selectionMishmash) {
            fail(msg);
        }
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

	@Test public void testCommandsInVisualMode() {
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

		checkCommand(forKeySeq("y"),
				false,  "A","LA"," MA kota",
				false,  "A","","LA MA kota");
		verify(adaptor, times(3)).changeMode(NormalMode.NAME);
		assertEquals(1, adaptor.getCursorService().getPosition().getModelOffset());

		checkCommand(forKeySeq("s"),
				true,  "A","LA"," MA kota",
				true,  "A",""," MA kota");
		// TODO: obtain correct arguments used by by ChangeOperation when changing mode
//		verify(adaptor).changeMode(InsertMode.NAME);
	}

    @Test public void visualModeShouldHaveAName() {
		assertEquals("visual mode", mode.getName());
	}
    
	@Test public void visualModeShouldEnterPainlesslyAndDeselectOnLeave() {
	    CursorService cursorService = platform.getCursorService();
	    Position position = cursorService.newPositionForModelOffset(42);
	    cursorService.setPosition(position, true);
		mode.enterMode();
		mode.leaveMode();
		assertNull(adaptor.getSelection());
	}
	
	@Test public void testTextObjects() {
		checkCommand(forKeySeq("iw"),
				false,  "It's Some","th","ing interesting.",
				false,  "It's ","Something"," interesting.");
    }

}
