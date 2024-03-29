package net.sourceforge.vrapper.core.tests.cases;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.Mockito;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.testutil.CommandTestCase;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.BlockwiseVisualMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

// FIXME: needs testing with different values of 'selection' variable
// (it affects most of the tests)

public class BlockwiseVisualModeTests extends CommandTestCase {

    @Override
	public void setUp() {
		super.setUp();
		registerManager = new DefaultRegisterManager();
		reloadEditorAdaptor();
	};

	private void prepareEditor(final boolean inverted,
            final String... block) {
        final String initialContent = joinLinewise(block);

    	content.setText(initialContent);
    	int selectFrom, selectTo;
//    	selectFrom = selectTo = beforeSelection.length();
//    	if (!inverted) {
//            selectTo += selected.length();
//        } else {
//            selectFrom += selected.length();
//        }
    	selectFrom = block[0].length();
    	selectTo = initialContent.length() - block[block.length-1].length() - 1;
    	if (inverted) {
    	    final int temp = selectTo;
    	    selectTo = selectFrom;
    	    selectFrom = temp;
    	}

        adaptor.changeModeSafely(BlockwiseVisualMode.NAME);

    	final CursorService cursorService = platform.getCursorService();
    	final SelectionService selectionService = platform.getSelectionService();
        final Position from = cursorService.newPositionForModelOffset(selectFrom);
        final Position to = cursorService.newPositionForModelOffset(selectTo);
        selectionService.setSelection(new BlockWiseSelection(adaptor, from, to));
//        if (selected.endsWith("\n")) {
//            final Position to = cursorService.newPositionForModelOffset(selectTo - 1);
//            selectionService.setSelection(new LineWiseSelection(adaptor, from, to));
//        } else {
//            final Position to = cursorService.newPositionForModelOffset(selectTo);
//            selectionService.setSelection(new SimpleSelection(new StartEndTextRange(from, to)));
//        }
    }
	
	private static String joinLinewise(final String[] block) {
	    final StringBuilder buf = new StringBuilder();
	    for (int i=0; i < block.length; i += 3) {
	        buf.append(join(i, i+3, block));
	        if (i+3 < block.length)
    	        buf.append('\n');
	    }
        return buf.toString();
    }

    private static String[] block(final String... entries) {
	    if (entries.length % 3 != 0)
	        throw new IllegalArgumentException("Number of entries must be divisible by 3");
	    
	    return entries;
	}


    private void assertCommandResult(final String initialLine,
            final boolean inverted, final String[] block) {
            final String expectedFinalContent = join(block);
    		final String actualFinalContent = content.getText();
    		int actSelFrom;
    		int actSelTo;
            final TextRange selection = adaptor.getSelection();
            if (selection != null) {
                actSelFrom = selection.getStart().getModelOffset();
                actSelTo = selection.getEnd().getModelOffset();
            } else {
                actSelFrom = actSelTo = adaptor.getCursorService().getPosition().getModelOffset();
            }
//    		final int expSelTo, expSelFrom;
//    		expSelFrom = expSelTo = beforeSelection.length();
//    		if (!inverted) {
//                expSelTo += selected.length();
//            } else {
//                expSelFrom += selected.length();
//            }

    		// TODO test selection
    		String msg = "";
    		final boolean selectionMishmash = false;
//    		if (expSelFrom != actSelFrom || expSelTo != actSelTo) {
//    			msg = "selection mishmash\n" + expSelFrom + " " + expSelTo + " but got " + actSelFrom + " " + actSelTo + "\n";
//    			selectionMishmash = true;
//    		}

    //		int offset = mockEditorAdaptor.getCaretOffset();
    		final String expectedLine = formatLine(block) + "\n";// + cursorLine(expSelTo);
    		final String   actualLine = formatLine(actualFinalContent,
    				min(actSelFrom, actSelTo),
    				max(actSelFrom, actSelTo)) + "\n";// + cursorLine(offset);

    		msg += String.format("STARTING FROM:\n'%s'\nEXPECTED:\n'%s'\nGOT:\n'%s'\n", initialLine, expectedLine, actualLine);
    		if (!actualFinalContent.equals(expectedFinalContent) || selectionMishmash) {
                fail(msg);
            }
        }


    private void checkCommand(final Command command,
			final boolean inverted1, final String[] block1,
			final boolean inverted2, final String[] block2) {

		final String  initialLine = formatLine(block1) + "\n"; // + cursorLine(selectTo);

		prepareEditor(inverted1, block1);
		executeCommand(command);
		assertCommandResult(initialLine, inverted2, block2);
	}

    private void checkLeavingCommand(final Command command,
			final boolean inverted, final String beforeSelection, final String selected, final String afterSelection,
			final String beforeCursor, final char atCursor, final String afterCursor) {
		final String  initialLine = formatLine(beforeSelection, selected, afterSelection) + "\n"; // + cursorLine(selectTo);

		prepareEditor(inverted, beforeSelection, selected, afterSelection);
		executeCommand(command);
        assertCommandResult(initialLine, beforeCursor, atCursor, afterCursor);
    }

	@Test public void testCommandsInVisualMode() throws Exception {

		// FIXME:
		// it's broken in test case, works quite well
		// in real eclipse
		checkCommand(forKeySeq("x"),
				false, block(
				        "A","la"," ma kota",
				        "T","es","t text",
				        "T","es","t text"
				        ),
				false, block(
				        "A",""," ma kota\n" +
				        "Tt text\n" +
				        "Tt text"
				        ));
		verify(adaptor).changeMode(NormalMode.NAME);

		checkCommand(forKeySeq("d"),
				false, block(
				        "A","la"," ma kota",
				        "T","es","t text",
				        "T","es","t text"
				        ),
				false, block(
				        "A",""," ma kota\n" +
				        "Tt text\n" +
				        "Tt text"
				        ));
		verify(adaptor, times(2)).changeMode(NormalMode.NAME);

		// TODO yank is incomplete
//		checkLeavingCommand(forKeySeq("y"), true,
//				"A", "LA", " MA kota",
//				"A", 'L', "A MA kota");
//		verify(adaptor, times(3)).changeMode(NormalMode.NAME);

		checkCommand(forKeySeq("s"),
				true,  block("A","LA"," MA kota"),
				true,  block("A",""," MA kota"));
		// TODO: obtain correct arguments used by by ChangeOperation when changing mode
//		verify(adaptor).changeMode(InsertMode.NAME);
	}

	/* TODO
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
    */

    @Test public void visualModeShouldHaveAName() {
		adaptor.changeModeSafely(BlockwiseVisualMode.NAME);
		assertEquals("block visual mode", adaptor.getCurrentModeName());
	}

	@Test public void visualModeShouldEnterPainlesslyAndDeselectOnLeave() throws Exception {
	    final CursorService cursorService = platform.getCursorService();
	    final Position position = cursorService.newPositionForModelOffset(42);
	    cursorService.setPosition(position, StickyColumnPolicy.ON_CHANGE);
		adaptor.changeMode(NormalMode.NAME);
		adaptor.changeMode(BlockwiseVisualMode.NAME);
		assertEquals(adaptor.getSelection().getModelLength(), 0);
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
				"with ",'S',"Ome\nCAPITal letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some CAPITAL"," letters",
				"with ",'S',"OME capital letters");
		
		checkLeavingCommand(forKeySeq("~"),
				true,  "with ","some\nCAPITAL"," letters",
				"with ",'S',"Ome\nCAPITal letters");
	}
	
	@Test
	public void test_CtrlC_exits() {
	    // TODO works fine in eclipse....
		checkLeavingCommand(forKeySeq("<C-c>"), true,
				"test", "123", "test",
				"test", '1', "23test");
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}

	@Test
	public void test_c() {
		prepareEditor(false, block(
				"Sta", "rt by making", " a block",
				"The", "n continue a", "nd select it",
				"Fin", "ally stop at", " that."
				));
		String initial = content.getText();
		executeCommand(forKeySeq("cedit<ESC>"));
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
		// [FIXME] Cursor position should be on 't'
		assertCommandResult(initial,
			    "Sta", 'e', "dit a block\n"
			  + "Theeditnd select it\n"
			  + "Finedit that.");
	}

	@Test
	public void test_insertShiftWidth() {
		Mockito.when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(4);

		prepareEditor(false, block(
				"Sta", "rt by making", " a block",
				"The", "n continue a", "nd select it",
				"Fin", "ally stop at", " that."
				));
		String initial = content.getText();
		executeCommand(forKeySeq(">"));
		assertCommandResult(initial,
				"Sta", ' ', "   rt by making a block\n"
			+ "The    n continue and select it\n"
			+ "Fin    ally stop at that.");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block",
				"The   ", " n continue a", "nd select it",
				"Fin   ", " ally stop at", " that."
				));
		executeCommand(forKeySeq(">"));
		assertCommandResult(initial,
				"Sta\t  ", ' ', "rt by making a block\n"
			+ "The\t   n continue and select it\n"
			+ "Fin\t   ally stop at that.");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block"
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta\t\t ", ' ', " rt by making a block");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block",
				"The   ", " n continue a", "nd select it",
				"Fin   ", " ally stop at", " that."
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta\t\t ", ' ', " rt by making a block\n"
				+ "The\t\t   n continue and select it\n"
				+ "Fin\t\t   ally stop at that.");

		prepareEditor(false, block(
				"Sta  \t", " rt by making", " a block",
				"The  \t", " n continue a", "nd select it",
				"Fin  \t", " ally stop at", " that."
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta\t\t ", ' ', "   rt by making a block\n"
				+ "The\t\t     n continue and select it\n"
				+ "Fin\t\t     ally stop at that.");
	}

	@Test
	public void test_insertShiftWidthExpandTab() {
		Mockito.when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(4);
		Mockito.when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);

		prepareEditor(false, block(
				"Sta", "rt by making", " a block",
				"The", "n continue a", "nd select it",
				"Fin", "ally stop at", " that."
				));
		String initial = content.getText();
		executeCommand(forKeySeq(">"));
		assertCommandResult(initial,
				"Sta", ' ', "   rt by making a block\n"
			+ "The    n continue and select it\n"
			+ "Fin    ally stop at that.");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block",
				"The   ", " n continue a", "nd select it",
				"Fin   ", " ally stop at", " that."
				));
		executeCommand(forKeySeq(">"));
		assertCommandResult(initial,
				"Sta   ", ' ', "    rt by making a block\n"
			+ "The        n continue and select it\n"
			+ "Fin        ally stop at that.");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block"
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta   ", ' ', "            rt by making a block");

		prepareEditor(false, block(
				"Sta   ", " rt by making", " a block",
				"The   ", " n continue a", "nd select it",
				"Fin   ", " ally stop at", " that."
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta   ", ' ', "            rt by making a block\n"
				+ "The                n continue and select it\n"
				+ "Fin                ally stop at that.");

		prepareEditor(false, block(
				"Sta  \t", " rt by making", " a block",
				"The  \t", " n continue a", "nd select it",
				"Fin  \t", " ally stop at", " that."
				));
		executeCommand(forKeySeq("3>"));
		assertCommandResult(initial,
				"Sta   ", ' ', "              rt by making a block\n"
				+ "The                  n continue and select it\n"
				+ "Fin                  ally stop at that.");
	}

	@Test
	public void test_removeShiftWidth() {
		Mockito.when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(4);
		String initial;

		prepareEditor(false, block(
				"\t aha\t\t    ", "\t      p", "atterna baz4"));
		initial = content.getText();
		executeCommand(forKeySeq("<"));
		assertCommandResult(initial,
				"\t aha\t\t    ", '\t', "  patterna baz4");

		prepareEditor(false, block(
				"\t aha\t\t    ", "\t  p", "atterna baz4"));
		initial = content.getText();
		executeCommand(forKeySeq("<"));
		assertCommandResult(initial,
				"\t aha\t\t    ", ' ', " patterna baz4");

		// Removing indent doesn't change whitespace before block
		prepareEditor(false, block(
				"\t aha\t\t    ", "\t  p", "atterna baz4"));
		initial = content.getText();
		executeCommand(forKeySeq("3<"));
		assertCommandResult(initial,
				"\t aha\t\t    ", 'p', "atterna baz4");

		// Removing indent doesn't change whitespace before block
		prepareEditor(false, block(
				"\t aha\t\t    ", "p", "atterna baz4"));
		initial = content.getText();
		executeCommand(forKeySeq("3<"));
		assertCommandResult(initial,
				"\t aha\t\t    ", 'p', "atterna baz4");

		// Removing indent doesn't change whitespace before block
		prepareEditor(false, block(
				"\t aha  \t\t ", "  p", "atterna baz4"));
		initial = content.getText();
		executeCommand(forKeySeq("3<"));
		assertCommandResult(initial,
				"\t aha  \t\t ", 'p', "atterna baz4");
	}
}
