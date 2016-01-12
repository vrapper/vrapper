package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.platform.PlatformSpecificModeProvider;
import net.sourceforge.vrapper.plugin.surround.provider.SurroundModesProvider;
import net.sourceforge.vrapper.plugin.surround.provider.SurroundStateProvider;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.TextObjectProvider;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.modes.CommandBasedMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

public class NormalModeTests extends CommandTestCase {
	@Override
	public void setUp() {
		super.setUp();
		adaptor.changeModeSafely(NormalMode.NAME);
	};

	@Override
	protected void reloadEditorAdaptor() {
	    super.reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
        reset(adaptor);
	};

	@Test public void testEnteringNormalModeChangesCaret() throws Exception {
		adaptor.changeMode(NormalMode.NAME);
		assertEquals(CaretType.RECTANGULAR, cursorAndSelection.getCaret());
	}
	
	@Test public void test_w() {
		checkCommand(forKeySeq("w"),
				"Ala ",'m', "a kota",
				"Ala ma ", 'k', "ota");
		checkCommand(forKeySeq("w"),
				"Ala ",'m', "a\nkota",
				"Ala ma\n", 'k', "ota");
		checkCommand(forKeySeq("w"),
				"Ala ",'m', "a\n\nkota",
				"Ala ma\n", '\n', "kota");
		checkCommand(forKeySeq("w"),
				"Ala ma\n", '\n', "kota",
				"Ala ma\n\n", 'k', "ota");
		// Skip over words with '_', go to next line.
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHAS\nnext",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHAS\n", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHASHERE\nnext",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHASHERE\n", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHASHERE\n    next",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHASHERE\n    ", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHASHERE\r\n    next",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHASHERE\r\n    ", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHASHERE\r\nnext",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHASHERE\r\n", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHA\r\nnext",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHA\r\n", 'n', "ext");
		checkCommand(forKeySeq("w"),
				"suite=", 'T',"LS_ECDHE_RSA_WITH_RC4_128_SHA\r\n\r\nnext",
				"suite=TLS_ECDHE_RSA_WITH_RC4_128_SHA\r\n", '\r', "\nnext");
	}
	
	@Test public void test_2w_newline() {
		checkCommand(forKeySeq("2w"),
				"Ala ", 'm', "a kota\nanother line",
				"Ala ma kota\n", 'a', "nother line");
	}
	
	@Test public void test_2w_blankline_as_word() {
		checkCommand(forKeySeq("2w"),
				"Ala ", 'm', "a kota\n\nthird line",
				"Ala ma kota\n", '\n', "third line");
	}

	@Test public void test_x() {
		checkCommand(forKeySeq("x"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
	}

	@Test public void test_s() throws CommandExecutionException {
		checkCommand(forKeySeq("s"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
		verify(adaptor).changeMode(eq(InsertMode.NAME), (ModeSwitchHint[]) any());
	}

	@Test public void test_S_middle_of_line() throws CommandExecutionException {
		checkCommand(forKeySeq("S"),
				"Al",'a'," ma kota",
				"",'\n',"");
		assertYanked(ContentType.LINES, "Ala ma kota\n");
		verify(adaptor).changeMode(eq(InsertMode.NAME), (ModeSwitchHint[]) any());
	}


	@Test public void test_S_beginning_of_line() throws CommandExecutionException {
		checkCommand(forKeySeq("S"),
				"",'A',"la ma kota",
				"",'\n',"");
		assertYanked(ContentType.LINES, "Ala ma kota\n");
		verify(adaptor).changeMode(eq(InsertMode.NAME), (ModeSwitchHint[]) any());
	}
	
	@Test public void test_S_end_of_line() throws CommandExecutionException {
		checkCommand(forKeySeq("S"),
				"Ala ma kot",'a',"",
				"",'\n',"");
		assertYanked(ContentType.LINES, "Ala ma kota\n");
		verify(adaptor).changeMode(eq(InsertMode.NAME), (ModeSwitchHint[]) any());
	}	
	
	@Test public void test_S_middle_of_file() throws CommandExecutionException {
		checkCommand(forKeySeq("S"), "First Line\nSec", 'o', "nd Line\nThird Line",
				"First Line\n", '\n', "Third Line");
		assertYanked(ContentType.LINES, "Second Line\n");
		verify(adaptor).changeMode(eq(InsertMode.NAME), (ModeSwitchHint[]) any());
	}

	@Test public void test_X() {
		checkCommand(forKeySeq("X"),
				"",'a'," ma kota",
				"",'a'," ma kota");
		checkCommand(forKeySeq("X"),
				"Al",'a'," ma kota",
				"A",'a'," ma kota");
		assertYanked(ContentType.TEXT, "l");
	}
	
	@Test public void test_pipe() {
		checkCommand(forKeySeq("|"),
				"Al",'a'," ma kota",
				"",'A',"la ma kota");
		checkCommand(forKeySeq("1|"),
				"Al",'a'," ma kota",
				"",'A',"la ma kota");
		checkCommand(forKeySeq("3|"),
				"Ala ma",' ',"kota",
				"Al",'a'," ma kota");
		checkCommand(forKeySeq("9|"),
				"Ala ma",' ',"kota",
				"Ala ma k",'o',"ta");
		checkCommand(forKeySeq("11|"),
				"Ala ma",' ',"kota",
				"Ala ma kot",'a',"");
		checkCommand(forKeySeq("999|"),
				"Ala ma",' ',"kota",
				"Ala ma kot",'a',"");
	}
	
	@Test public void test_cw() {
		checkCommand(forKeySeq("cw"),
				"Ala",'m',"a kota",
				"Ala",' ',"kota");
		assertYanked(ContentType.TEXT, "ma");
	}

	@Test public void test_cW() {
		checkCommand(forKeySeq("cW"),
				"Ala",'m',"a;asdf kota",
				"Ala",' ',"kota");
		assertYanked(ContentType.TEXT, "ma;asdf");
	}

	@Test public void test_cw_compilant() {
		checkCommand(forKeySeq("cw"),
			"Ala",' ',"ma kota",
			"Ala",'m',"a kota");
		assertYanked(ContentType.TEXT, " ");
	}
		
	@Test public void test_cw_single_letter() {
		//'cw' on a single letter
	    checkCommand(forKeySeq("cw"),
			"Ala ",'z'," ma kota",
			"Ala ",' ',"ma kota");
		assertYanked(ContentType.TEXT, "z");
	}
	
	@Test public void test_cW_single_letter() {
		//'cW' on a single letter
		checkCommand(forKeySeq("cW"),
				"Ala ",'z'," ma kota",
				"Ala ",' ',"ma kota");
		assertYanked(ContentType.TEXT, "z");
	}
	
	@Test public void test_cw_single_space() {
		//'cw' on a single space character
	    checkCommand(forKeySeq("cw"),
			"Ala",' ',"z ma kota",
			"Ala",'z'," ma kota");
		assertYanked(ContentType.TEXT, " ");
	}
		
	@Test public void test_cw_multiple_spaces() {
		//'cw' with multiple spaces to the next word
	    checkCommand(forKeySeq("cw"),
			"Ala",' ',"     z ma kota",
			"Ala",'z'," ma kota");
		assertYanked(ContentType.TEXT, "      ");
	}
	
	@Test public void test_c2w() {
	    checkCommand(forKeySeq("c2w"),
			"",'A',"la ma kota",
			"",' ',"kota");
		assertYanked(ContentType.TEXT, "Ala ma");
	}
	
	@Test public void test_c3w() {
	    checkCommand(forKeySeq("c3w"),
			"",'A',"la ma z kota",
			"",' ',"kota");
		assertYanked(ContentType.TEXT, "Ala ma z");
	}
	
	@Test public void test_dW_newline() {
	    //delete last word of a line
	    checkCommand(forKeySeq("dW"),
	        "Ala ",'k',"ota\nanother line",
	        "Ala",' ',"\nanother line");
	    assertYanked(ContentType.TEXT, "kota");
	}

	@Test public void test_dw() {
		checkCommand(forKeySeq("dw"),
			"Ala ",'m',"a kota",
			"Ala ",'k',"ota");
		assertYanked(ContentType.TEXT, "ma ");
	}
	
	@Test public void test_dw_newline() {
	    //delete last word of a line
		checkCommand(forKeySeq("dw"),
			"Ala ",'k',"ota\nanother line",
			"Ala",' ',"\nanother line");
		assertYanked(ContentType.TEXT, "kota");
	}
	
	@Test public void test_dw_newline_space() {
	    //delete last word of a line, space starting next line
	    checkCommand(forKeySeq("dw"),
	        "Ala ",'k',"ota\n another line",
	        "Ala",' ',"\n another line");
	    assertYanked(ContentType.TEXT, "kota");
	}
	
	@Test public void test_dw_newline_ws() {
	    //delete last word of a line, multi-space starting next line
		checkCommand(forKeySeq("dw"),
			"Ala ",'k',"ota\n  another line",
			"Ala",' ',"\n  another line");
		assertYanked(ContentType.TEXT, "kota");
	}
	
	@Test public void test_d2w_newline() {
		//delete word spanning a line
		checkCommand(forKeySeq("d2w"),
			"Ala ",'k',"ota\nanother line",
			"Ala ",'l',"ine");
		assertYanked(ContentType.TEXT, "kota\nanother ");
	}
	
	@Test public void test_d3w_newline() {
		//delete word spanning a line, ending with newline
		checkCommand(forKeySeq("d3w"),
			"Ala ",'k',"ota\nanother line\nand again",
			"Ala",' ',"\nand again");
		assertYanked(ContentType.TEXT, "kota\nanother line");
	}
	
	//blank lines act as a word
	@Test public void test_d3w_multiple_newlines() {
		//delete word spanning a line, ending with newline
		checkCommand(forKeySeq("d3w"),
			"Ala ",'k',"ota\nanother\n\nagain\n",
			"Ala",' ',"\nagain\n");
		assertYanked(ContentType.TEXT, "kota\nanother\n");
	}

	//blank lines act as a word
	@Test public void test_d4w_multiple_newlines() {
		//delete word spanning a line, ending with newline
		checkCommand(forKeySeq("d4w"),
			"Ala ",'k',"ota\nanother\n\n\nagain\n",
			"Ala",' ',"\nagain\n");
		assertYanked(ContentType.TEXT, "kota\nanother\n\n");
	}
	
	@Test public void test_dw_newline_beginning() {
	    checkCommand(forKeySeq("dw"),
	        "Ala\n  ",' ',"  kota",
	        "Ala\n  ",'k',"ota");
	    assertYanked(ContentType.TEXT, "   ");
	}
	
	@Test public void test_dw_newline_as_word() {
		checkCommand(forKeySeq("dw"),
			"Ala\n", '\n', "\nkota",
			"Ala\n", '\n', "kota");
		assertYanked(ContentType.TEXT, "\n");
	}

	@Test public void test_diw() {
		checkCommand(forKeySeq("diw"),
				"sin",'g',"le",
				"",EOF,"");
		assertYanked(ContentType.TEXT, "single");

		checkCommand(forKeySeq("diw"),
				"Ala mi",'e',"wa kota",
				"Ala ",' ',"kota");
		assertYanked(ContentType.TEXT, "miewa");

		checkCommand(forKeySeq("diw"),
				"Ala miew",'a'," kota",
				"Ala ",' ',"kota");
		assertYanked(ContentType.TEXT, "miewa");
	}

	@Test public void test_daw() {
		checkCommand(forKeySeq("daw"),
				"Ala mi",'e',"wa kota",
				"Ala ",'k',"ota");
		assertYanked(ContentType.TEXT, "miewa ");
	}

	@Ignore //ignored until we fix #474
	@Test public void test_diw_whitespace() {
		checkCommand(forKeySeq("diw"),
				"before cursor  ",' ',"  after cursor",
				"before cursor",'a',"fter cursor");

		checkCommand(forKeySeq("diw"),
				"before cursor",' ',"after cursor",
				"before cursor",'a',"fter cursor");

		checkCommand(forKeySeq("d2iw"),
				"before cursor",' ',"after cursor",
				"before cursor",' ',"cursor");

		checkCommand(forKeySeq("d3iw"),
				"before cursor  ",' ',"  after   after   cursor",
				"before cursor",'a',"fter   cursor");

		checkCommand(forKeySeq("diW"),
				"before cursor  ",' ',"  after cursor",
				"before cursor",'a',"fter cursor");

		checkCommand(forKeySeq("diW"),
				"before cursor",' ',"after cursor",
				"before cursor",'a',"fter cursor");

		checkCommand(forKeySeq("daw"),
				"before cursor   ",' ',"   after cursor",
				"before cursor",' ',"cursor");

		checkCommand(forKeySeq("daw"),
				"before cursor",' ',"after cursor",
				"before cursor",' ',"cursor");

		checkCommand(forKeySeq("daW"),
				"before cursor   ",' ',"   after cursor",
				"before cursor",' ',"cursor");

		checkCommand(forKeySeq("daW"),
				"before cursor",' ',"after cursor",
				"before cursor",' ',"cursor");
	}

	@Test public void test_p() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala czasam",'i'," ma kota");
	}
	
	@Test public void test_P() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("P"),
				"Al",'a'," ma kota",
				"Al czasam",'i',"a ma kota");
	}
	
	@Test public void test_gp() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("gp"),
				"Al",'a'," ma kota",
				"Ala czasami",' ',"ma kota");

		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "1 2 3\n4 5 6\n"));
		checkCommand(forKeySeq("gp"),
				"1 2 ", '3', "\n4 5 6",
				"1 2 3\n1 2 3\n4 5 6\n", '4', " 5 6");
	}
	
	@Test public void test_gP() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("gP"),
				"Al",'a'," ma kota",
				"Al czasami",'a'," ma kota");
	}
	
	@Test public void test_P_empty_clipboard() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, ""));
		checkCommand(forKeySeq("P"),
				"Al", 'a', " ma kota",
				"Al", 'a', " ma kota");
		// Special case: cursor is on a new line, don't move.
		checkCommand(forKeySeq("gP"),
		        "Al a\n", '\n', "ma kota",
		        "Al a\n", '\n', "ma kota");
		checkCommand(forKeySeq("P"),
				"Al a\n", '\n', "ma kota",
				"Al a\n", '\n', "ma kota");
	}

	@Test public void test_p_empty_clipboard() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, ""));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Al",'a'," ma kota");
		// Special case: cursor is on a new line, don't move.
		checkCommand(forKeySeq("gp"),
				"Al a\n", '\n', "ma kota",
				"Al a\n", '\n', "ma kota");
		checkCommand(forKeySeq("p"),
		        "Al a\n", '\n', "ma kota",
		        "Al a\n", '\n', "ma kota");
	}

	@Test public void test_o() {
		installSaneRegisterManager();
		checkCommand(forKeySeq("omana<Esc>"),
				"Al", ' ', "a\nma kota",
				"Al a\nman", 'a', "\nma kota");
		checkCommand(forKeySeq("o<Esc>"),
				"Al", ' ', "a\nma kota",
				"Al a\n", '\n', "ma kota");
		// Line is empty. Cursor should be before end of that new, empty line, not on previous line.
		checkCommand(forKeySeq("3o<Esc>"),
				"Al", ' ', "a\nma kota",
				"Al a\n\n\n", '\n', "ma kota");
		checkCommand(forKeySeq("5ohey<Esc>"),
				"Al", ' ', "a\nma kota",
				"Al a\nhey\nhey\nhey\nhey\nhe", 'y', "\nma kota");
	}

	@Test public void test_dot_on_o() {
		installSaneRegisterManager();
		checkCommand(forKeySeq("omana<Esc>."),
				"Al", ' ', "a\nma kota",
				"Al a\nmana\nman", 'a', "\nma kota");
		checkCommand(forKeySeq("3oya<Esc>."),
				"Al", ' ', "a\nma kota",
				"Al a\nya\nya\nya\nya\nya\ny", 'a', "\nma kota");
	}

	@Test public void test_O() {
		installSaneRegisterManager();
		checkCommand(forKeySeq("Omana<Esc>"),
				"Al a\nma", ' ', "kota",
				"Al a\nman", 'a', "\nma kota");
		checkCommand(forKeySeq("O<Esc>"),
				"Al a\nma", ' ', "kota",
				"Al a\n", '\n', "ma kota");
		// Line is empty. Cursor should be before end of that new, empty line, not on previous line.
		checkCommand(forKeySeq("3O<Esc>"),
				"Al a\nma", ' ', "kota",
				"Al a\n\n\n", '\n', "ma kota");
		checkCommand(forKeySeq("5Ohey<Esc>"),
				"Al a\nma", ' ', "kota",
				"Al a\nhey\nhey\nhey\nhey\nhe", 'y', "\nma kota");
	}

	@Test public void test_dot_on_O() {
		installSaneRegisterManager();
		checkCommand(forKeySeq("Oya<Esc>."),
				"Al a\nma", ' ', "kota",
				"Al a\ny", 'a', "\nya\nma kota");
		checkCommand(forKeySeq("3Oya<Esc>."),
				"Al a\nma", ' ', "kota",
				"Al a\nya\nya\nya\nya\ny", 'a', "\nya\nma kota");
		checkCommand(forKeySeq("3Oya<Esc>.."),
				"Al a\nma", ' ', "kota",
				"Al a\nya\nya\nya\nya\nya\nya\ny", 'a', "\nya\nya\nma kota");
	}

	@Test public void test_dot_on_insert_with_inital_backspace() {
		installSaneRegisterManager();
		checkCommand(forKeySeq("A<BS><BS>ah123<Esc>j."),
				"blah bl", 'a', "h bleh\nblah blah blxh",
				"blah blah blah123\nblah blah blah12", '3', "");
	}
	
	@Test public void test_p_lines() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota\nanother line",
				"Ala ma kota\n",'A',"s to pies Ali\nanother line");
	}

	@Test public void test_p_lines_end_of_buffer() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala ma kota\n",'A',"s to pies Ali");
	}

	// TODO: I don't like Vim's p behaviour for P with text -- make compatibility optional and THEN test it

	@Test public void test_P_lines() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("P"),
				"Al",'a'," ma kota",
				"",'A',"s to pies Ali\nAla ma kota");
	}

	@Test public void test_newline_conversion() {
		//suppose this is copied from another file in Eclipse which uses Windows newlines...
		//( testing default is \n )
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to\r\npies Ali\r\n"));
		checkCommand(forKeySeq("P"),
				"Al",'a'," ma kota",
				"",'A',"s to\npies Ali\nAla ma kota");

		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala ma kota\n",'A',"s to\npies Ali");
	}

	@Test public void test_dot() {
		Command dw = new TextOperationTextObjectCommand(DeleteOperation.INSTANCE, new MotionTextObject(MoveWordRight.INSTANCE));
		when(registerManager.getLastEdit()).thenReturn(dw);
		checkCommand(forKeySeq(".."),
				"A",'l',"a ma kota i psa",
				"A",'k',"ota i psa");
	}

	@Test public void test_r() {
	    checkCommand(forKeySeq("ry"),
	            "Ala ma kot",'a',"",
	            "Ala ma kot",'y',"");
	    verify(cursorAndSelection).setCaret(CaretType.UNDERLINE);
	    verify(historyService).beginCompoundChange();
	    verify(historyService).endCompoundChange();

	    checkCommand(forKeySeq("3rx"),
	            "This is ",'b',"ad",
	            "This is xx",'x',"");
	    verify(cursorAndSelection, times(2)).setCaret(CaretType.UNDERLINE);
	    verify(historyService, times(2)).beginCompoundChange();
	    verify(historyService, times(2)).endCompoundChange();

	    checkCommand(forKeySeq("10rx"),
	            "This is ",'b',"ad",
	            "This is ",'b',"ad");
    }

	@Test public void test_tilde() {
	    checkCommand(forKeySeq("~"),
	            "",'P',"ropertyName",
	            "p",'r',"opertyName");
	    verify(historyService).beginCompoundChange();
	    verify(historyService).endCompoundChange();

	    checkCommand(forKeySeq("2~"),
	            "T",'c',"pConnection",
	            "TCP",'C',"onnection");
	    verify(historyService, times(2)).beginCompoundChange();
	    verify(historyService, times(2)).endCompoundChange();

	    checkCommand(forKeySeq("10~"),
	            "A",'l',"a\nma\nkota",
	            "AL",'A',"\nma\nkota");
	    verify(historyService, times(3)).beginCompoundChange();
	    verify(historyService, times(3)).endCompoundChange();
	}

	// TODO: deactivate repaint selectively and test it
//	@Test public void testThereIsNoRedrawsWhenCommandIsExecuted() throws CommandExecutionException {
//		Command checkIt = new CountIgnoringNonRepeatableCommand() {
//			public void execute(EditorAdaptor editorAdaptor) {
//				assertSame(adaptor, editorAdaptor);
//				verify(viewportService).setRepaint(false);
//			}
//		};
//		CommandBasedMode normalMode = (CommandBasedMode) mode;
//		normalMode.executeCommand(checkIt);
//	}

	@Test public void testLastCommandRegistrationWhenThereIsRepetition() throws CommandExecutionException {
		Command repetition = mock(Command.class);
		Command cmd = mock(Command.class);
		when(cmd.repetition()).thenReturn(repetition);
		when(registerManager.isDefaultRegisterActive()).thenReturn(true);
		CommandBasedMode normalMode = new NormalMode(adaptor);
		normalMode.executeCommand(cmd);
		verify(registerManager).setLastEdit(repetition);
	}

	@Test public void testThereIsNoCommandRegistrationWhenThereIsNoRepetition() throws CommandExecutionException {
		Command cmd = mock(Command.class);
		when(cmd.repetition()).thenReturn(null);
		CommandBasedMode normalMode = new NormalMode(adaptor);
		normalMode.executeCommand(cmd);
		verify(registerManager, never()).setLastEdit(null);
		verify(registerManager, never()).setLastEdit(any(Command.class));
	}

	@Test
    public void testStickyColumnOnDelete() {
        checkCommand(forKeySeq("dbj"),
                "a",'b',"c\nabc",
                "bc\n",'a',"bc");
        // this one made snapshot tests fail:
        checkCommand(forKeySeq("dFej"),
                "bcde ",'g',"\nabcde gabcde",
                "bcdg\nabc",'d',"e gabcde");
    }

	@Test
    public void testMultipliedCount() {
	    checkCommand(forKeySeq("2d3d"),
	            "0\n",'1',"\n2\n3\n4\n5\n6\n7\n",
	            "0\n",'7',"\n");
	    checkCommand(forKeySeq("2\"_2d2d"),
	            "0\n",'1',"\n2\n3\n4\n5\n6\n7\n8\n9\n",
	            "0\n",'9',"\n");
    }

	@Test
    public void test_dib_simple() {
        checkCommand(forKeySeq("dib"),
                "call(so",'m',"ething, funny);",
                "call(",')',";");
	}
	
	@Test
	public void test_dib_innerBlock() {
        checkCommand(forKeySeq("dib"),
                "call(so",'m',"ething(), funny);",
                "call(",')',";");
	}
	
	@Test
	public void test_dib_innerBlock_ws() {
        checkCommand(forKeySeq("dib"),
                "call(something(),",' ',"funny);",
                "call(",')',";");
	}
	
	/*
	 * This test fails, but fixing it is a very low priority.
	 * At least this documents the error.
	@Test
	public void test_dib_innerBlock_newlines() {
        checkCommand(forKeySeq("dib"),
                "call(\nsomething(),",'\n',"funny()\n);",
                "call",'(',"\n);");
	}
	*/
	
	@Test
	public void test_d2ib() {
        checkCommand(forKeySeq("d2ib"),
                "call(something very(fu",'n',"ny));",
                "call(",')',";");
	}
	
	@Test
	public void test_2dib() {
        checkCommand(forKeySeq("2dib"),
                "call(something, very(fu",'n',"ny));",
                "call(",')',";");
	}
	
	@Test 
	public void test_dib_on_openParen() {
        // as strange as it may look, this is the actual Vim behaviour
        checkCommand(forKeySeq("dib"),
                "call",'(',"something);",
                "call(",')',";");
	}
	
	@Test
	public void test_dib_on_closeParen() {
        checkCommand(forKeySeq("dib"),
                "call(something",')',";",
                "call(",')',";");
	}
	
	@Test
	public void test_2dib_on_innerOpenParen() {
        checkCommand(forKeySeq("2dib"),
                "call(something, very",'(',"funny));",
                "call(",')',";");
    }

	@Test
    public void test_di_() {
        checkCommand(forKeySeq("di'"),
                "'abc",'\'',"def'",
                "'",'\'',"def'");
        
        checkCommand(forKeySeq("di'"),
        		"foo'f",'o',"o'foo",
        		"foo'",'\'',"foo");
        
        checkCommand(forKeySeq("di'"),
        		"'foo'foo",'\'',"foo'",
        		"'foo'foo'",'\'',"");
        
        checkCommand(forKeySeq("di'"),
        		"'foofoo",'\'',"foo'",
        		"'",'\'',"foo'");
        
        checkCommand(forKeySeq("di'"),
        		"no quotes",' ',"to be found",
        		"no quotes",' ',"to be found");
        
        checkCommand(forKeySeq("di'"),
        		"something ",'b',"efore quotes 'foo' after",
        		"something before quotes '",'\''," after");
        
        checkCommand(forKeySeq("di'"),
        		"some'thing' ",'b',"efore quotes after",
        		"some'thing' ",'b',"efore quotes after");
    }

	@Test
    public void test_dab() {
        checkCommand(forKeySeq("dab"),
                "call(so",'m',"ething, funny);",
                "call",';',"");
        checkCommand(forKeySeq("dab"),
                "call(\nsomething(),",'\n',"funny()\n);",
                "call",';',"");
    }

	@Test
    public void test_dt() {
        checkCommand(forKeySeq("dta"),
                "A",'l',"a ma kota.",
                "A",'a'," ma kota.");
        checkCommand(forKeySeq("d2ta"),
                "A",'l',"a ma kota.",
                "A",'a'," kota.");
        checkCommand(forKeySeq("3dta"),
                "A",'l',"a ma kota.",
                "A",'a',".");

        checkCommand(forKeySeq("2d2ta"),
                "A",'l',"a ma kota.",
                "A",'l',"a ma kota.");
        checkCommand(forKeySeq("3dta"),
                "A",'l',"a ma\nkota.",
                "A",'l',"a ma\nkota.");
    }
	
	@Test
    public void test_dap() {
	    // "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac"
        checkCommand(forKeySeq("dap"),
                "",'1',"ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n",'\n',"3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n",'\n',"  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n\n",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'7',"ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n",'4',"ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'7',"ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'1',"1ac\n12ac");
        
        // Special cases for file end sections ("do nothing")
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac\n",'\n',"\n",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac\n",'\n',"\n");
        
        checkCommand(forKeySeq("dap"),
                "\n\n\n",'f',"oo\nbar\n\n\n\n",
                "\n\n\n",EOF,"");
        checkCommand(forKeySeq("dap"),
                "\n",'\n',"",
                "\n",'\n',"");
        checkCommand(forKeySeq("dap"),
                "hello\n",'w',"orld",
                "",EOF,"");
        checkCommand(forKeySeq("dap"),
                "hello\n",'w',"orld\n",
                "",EOF,"");
        checkCommand(forKeySeq("dap"),
                "",'\n',"\nhello\nworld\n",
                "",EOF,"");
        checkCommand(forKeySeq("dap"),
                "\n\n",'h',"ello\nworld",
                "",EOF,"");
        checkCommand(forKeySeq("dap"),
                "",'h',"ello",
                "",EOF,"");
        checkCommand(forKeySeq("dap"),
                "",EOF,"",
                "",EOF,"");
                
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n",' '," \n\n11ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n",'\n',"11ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac");
        
        // Special cases for file end sections
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n",'1',"1ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac");
        checkCommand(forKeySeq("dap"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n",'1',"2ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac");
	}
	
	@Test
    public void test_2dap() {
        checkCommand(forKeySeq("2dap"),
                "",'1',"ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "",'7',"ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("2dap"),
                "1ac\n",'\n',"3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n",'\n',"  \n\n11ac\n12ac");
        checkCommand(forKeySeq("2dap"),
                "1ac\n\n",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'1',"1ac\n12ac");
        
        checkCommand(forKeySeq("2dap"),
                "1ac\n\n3ac\n4ac\n",'\n',"  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n3ac\n",'4',"ac");
        checkCommand(forKeySeq("2dap"),
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n3ac\n",'4',"ac");
	}
	
	@Test public void test_cit_inTag() {
	    checkCommand(forKeySeq("cit"),
	            "<tag>with",' ',"text in it</tag>",
	            "<tag>",'<',"/tag>");
		assertYanked(ContentType.TEXT, "with text in it");
	}
	
	@Test public void test_cit_onStartTag() {
	    checkCommand(forKeySeq("cit"),
	            "<ta",'g',">with text in it</tag>",
	            "<tag>",'<',"/tag>");
		assertYanked(ContentType.TEXT, "with text in it");
	}
	
	@Test public void test_cit_onEndTag() {
	    checkCommand(forKeySeq("cit"),
	            "<tag>with text in it</t",'a',"g>",
	            "<tag>",'<',"/tag>");
		assertYanked(ContentType.TEXT, "with text in it");
	}
	
	@Test
	public void test_dit_indentationBeforeTag() {
	    checkCommand(forKeySeq("dit"),
	            "   ",' ',"<tag>with text in it</tag>",
	            "    <tag>",'<',"/tag>");
		assertYanked(ContentType.TEXT, "with text in it");
		
	   checkCommand(forKeySeq("dit"),
                "",' ',"<tag>with text in it</tag>",
                " <tag>",'<',"/tag>");

        checkCommand(forKeySeq("dit"),
                "<tag>\n",
                ' ',"    text\n" +
        		"</tag>",
        		
                "<tag>",'<',"/tag>");
            
        checkCommand(forKeySeq("dit"),
                "<tag1>\n",
                ' ',"   <tag2>text</tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "    <tag2>",'<',"/tag2>\n" +
                "</tag1>");
        
        checkCommand(forKeySeq("dit"),
                "<tag1>\n",
                ' ',"   <tag2><tag3>text</tag3></tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "    <tag2>",'<',"/tag2>\n" +
                "</tag1>");
        
        checkCommand(forKeySeq("dat"),
                "<tag1>\n",
                ' ',"   <tag2>text</tag2>\n" +
                "</tag1>",
                        
                "<tag1>\n" +
                "   ",' ',"\n" +
                "</tag1>");
		
	    checkCommand(forKeySeq("dat"),
                "<tag1>\n" +
                "    <tag2>\n",
                ' ',"       text\n" +
        		"    </tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "   ",' ',"\n" +
                "</tag1>");

	       checkCommand(forKeySeq("dat"),
                "<tag1>\n" +
                "    <tag2>\n",
                ' ',"       \n" +
                "        <tag3>ok</tag3>\n" +
                "    </tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "   ",' ',"\n" +
                "</tag1>");
	    
        checkCommand(forKeySeq("dat"),
                "<tag1>\n",
                ' ', "   <tag2>\n" +
                "        text\n" +
                "    </tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "   ",' ',"\n" +
                "</tag1>");
        
        checkCommand(forKeySeq("dat"),
                "<tag1>\n" +
                "    <tag2>\n" +
                "        text\n",
        		' ', "   </tag2>\n" +
                "</tag1>",
                
                "<tag1>\n" +
                "   ",' ',"\n" +
                "</tag1>");
	}
	
	@Test
	public void test_dat_endOfLine() {
        checkCommand(forKeySeq("dat"),
                " <tag1></tag1>", ' ', "\n",
                " <tag1></tag1>", ' ', "\n");
        checkCommand(forKeySeq("dat"),
                " <tag1></tag1", '>', "\r",
                "", ' ', "\r");
        checkCommand(forKeySeq("dat"),
                " <tag1></tag1>", '\r', "",
                "", ' ', "\r");
	}
	
	@Test
	public void test_vatd_endOfLine() {
	    // Make sure that tag gets matched when EvilCaret pushed the cursor towards the end of line.
        checkCommand(forKeySeq("vatd"),
                " <tag1></tag1", '>', "\r",
                "", ' ', "\r");
	    // EvilCaret would cause a StringOutOfBoundsException when at end of line.
        checkCommand(forKeySeq("vatd"),
                " <tag1></tag1>", ' ', "\r",
                " <tag1></tag1", '>', "\r");
	    // Another StringOutOfBoundsException was caused when on an empty line.
        checkCommand(forKeySeq("vatd"),
                " <tag1>\r", '\r', "</tag1>\r",
                "", ' ', "\r");
	    // EvilCaret may also cause a StringOutOfBoundsException when sitting on the newline char
        //  in windows.
        checkCommand(forKeySeq("vatd"),
                " <tag1>", '\r', "\n</tag1>\r",
                "", ' ', "\r");
        checkCommand(forKeySeq("vatd"),
                " <tag1>\r", '\n', "</tag1>\r",
                "", ' ', "\r");
	}

    @Test
    public void test_dit_betweenAttributes() {
        // Just make sure that indentation code isn't triggered when starting on whitespace
        checkCommand(forKeySeq("dat"),
                " <tag1 lang=\"en-US\"",' ',"state=\"Utah\"><tag2>with text in it</tag2></tag1>",
                "", ' ', "");
        
        checkCommand(forKeySeq("dit"),
                " <tag1 lang=\"en-US\"",' ',"state=\"Utah\"><tag2>with text in it</tag2></tag1>",
                " <tag1 lang=\"en-US\" state=\"Utah\">", '<', "/tag1>");
    }
	
	@Test
	public void test_dit() {
        checkCommand(forKeySeq("dit"),
                "<tag>co",'n',"tent</tag>",
                "<tag>",'<',"/tag>");
        
        checkCommand(forKeySeq("dit"),
                "he",'l',"lo <tag>content</tag>",
                "he",'l',"lo <tag>content</tag>");
        
        checkCommand(forKeySeq("dit"),
                "<tag>content</tag> out",'s',"ide",
                "<tag>content</tag> out",'s',"ide");
        
        checkCommand(forKeySeq("dit"),
                "<tag",'>',"content</tag>",
                "<tag>",'<',"/tag>");
        
        checkCommand(forKeySeq("dit"),
                "<tag><tag>co",'n',"tent</tag></tag>",
                "<tag><tag>",'<',"/tag></tag>");

        checkCommand(forKeySeq("dit"),
                "<tag><% jsp",' ',"%></tag>",
                "<tag>",'<',"/tag>");

        checkCommand(forKeySeq("dit"),
                "<tag var=\"<%=jsp%>\"> ",' ',"foo</tag>",
                "<tag var=\"<%=jsp%>\">",'<',"/tag>");
	}
	
	@Test
	public void test_dit_comment() {
        checkCommand(forKeySeq("dit"),
                "<tag><!-- comment -->",' ',"</tag>",
                "<tag>",'<',"/tag>");
        checkCommand(forKeySeq("dit"),
                "<tag>",'<',"!-- comment --></tag>",
                "<tag>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_multiline() {
        checkCommand(forKeySeq("dit"),
                "<tag\nother>co",'n',"tent</tag>",
                "<tag\nother>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_withinClosing() {
        checkCommand(forKeySeq("dit"),
                "<tag>content</t",'a',"g>",
                "<tag>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_withinOpening() {
        checkCommand(forKeySeq("dit"),
                "<t",'a',"g>content</tag>",
                "<tag>",'<',"/tag>");
	}
	
    @Test
    public void test_dit_singleTag() {
        checkCommand(forKeySeq("dit"),
                "<tag><tag/>co",'n',"tent</tag>",
                "<tag>",'<',"/tag>");
        
        checkCommand(forKeySeq("dit"),
                "<tag>co",'n',"tent<tag/></tag>",
                "<tag>",'<',"/tag>");
        
        checkCommand(forKeySeq("dit"),
                "<tag><tag/>co",'n',"tent<tag/></tag>",
                "<tag>",'<',"/tag>");
    }
	
	@Test
	public void test_2dit() {
        checkCommand(forKeySeq("2dit"),
                "<tag><tag>co",'n',"tent</tag></tag>",
                "<tag>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_otherOpener() {
        checkCommand(forKeySeq("dit"),
                "<\n<tag>co",'n',"tent</tag>",
                "<\n<tag>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_hangingOpeningTag() {
        checkCommand(forKeySeq("dit"),
                "<aaaa>\n<tag>co",'n',"tent</tag>",
                "<aaaa>\n<tag>",'<',"/tag>");
	}
	
	@Test
	public void test_dit_interweaved() {
	    checkCommand(forKeySeq("dit"),
	            "<a><b>con",'t',"ent</a></b>",
	            "<a><b>",'<',"/b>");
	}
	
	@Test
	public void test_more_dit() {
		checkCommand(forKeySeq("dit"),
                "<tag\nother>co",'n',"tent\n</tag>",
                "<tag\nother>",'<',"/tag>");
        
        checkCommand(forKeySeq("dit"),
                "<foo\n    blah=\"foo\"\nsomething=\"else\">co",'n',"tent\n</foo>",
                "<foo\n    blah=\"foo\"\nsomething=\"else\">",'<',"/foo>");
        
        checkCommand(forKeySeq("dit"),
                "<foo\n\tblah=\"foo\"\n\tsomething=\"else\">co",'n',"tent\n</foo>",
                "<foo\n\tblah=\"foo\"\n\tsomething=\"else\">",'<',"/foo>");
	}
	
	@Test
	@Ignore
	public void test_dit_containsOpening() {
	    // pretty tricky to implement this I think, but it's how Vim does it.
        checkCommand(forKeySeq("dit"),
                "<tag prop=\"<\">co",'n',"tent</tag>",
                "<tag prop=\"<\">",'<',"/tag>");
	}
	
	@Test
	public void test_delimiters() {
        checkCommand(forKeySeq("di("),
                "before (mi",'d',"dle) after",
                "before (",')'," after");
        checkCommand(forKeySeq("di)"),
                "before (mi",'d',"dle) after",
                "before (",')'," after");
        checkCommand(forKeySeq("di{"),
                "before {mi",'d',"dle} after",
                "before {",'}'," after");
        checkCommand(forKeySeq("di}"),
                "before {mi",'d',"dle} after",
                "before {",'}'," after");
        checkCommand(forKeySeq("di["),
                "before [mi",'d',"dle] after",
                "before [",']'," after");
        checkCommand(forKeySeq("di]"),
                "before [mi",'d',"dle] after",
                "before [",']'," after");
        checkCommand(forKeySeq("di>"),
                "before <mi",'d',"dle> after",
                "before <",'>'," after");
        checkCommand(forKeySeq("di<"),
              "before <mi",'d',"dle> after",
              "before <",'>'," after");
        checkCommand(forKeySeq("di>"),
                "before <middle",'>'," after",
                "before <",'>'," after");
        checkCommand(forKeySeq("di<"),
              "before ",'<',"middle> after",
              "before <",'>'," after");
        checkCommand(forKeySeq("dim_"),
              "before _",'m',"iddle_ after",
              "before _",'_'," after");
        // Don't know if this is meant to be supported.
//        checkCommand(forKeySeq("dim_"),
//              "before _",'m',"iddle line 1\nmiddle line 2_ after",
//              "before _",'_'," after");
        checkCommand(forKeySeq("dim_"),
              "before ",'_',"middle_ after",
              "before _",'_'," after");
        checkCommand(forKeySeq("di{"),
                "function {\n",'f',"oo\n}",
                "function {\n",'}',"");
        checkCommand(forKeySeq("di{"),
                "fu", 'n', "ction {\nfoo\n}",
                "fu", 'n', "ction {\nfoo\n}");
        checkCommand(forKeySeq("di{"),
                "function {\n",'f',"oo\n     }",
                "function {\n",'\n',"     }");
        //this doesn't actually match Vim's behavior
        //(Vim would leave the newline between '{' and '}')
        checkCommand(forKeySeq("di{"),
                "function { same line\n",'f',"oo\n}",
                "function {",'}',"");
        checkCommand(forKeySeq("di{"),
                "function { same line\n",'f',"oo\nsame line}",
                "function {",'}',"");

        checkCommand(forKeySeq("da("),
                "before (mi",'d',"dle) after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da)"),
                "before (mi",'d',"dle) after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da{"),
                "before {mi",'d',"dle} after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da}"),
                "before {mi",'d',"dle} after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da["),
                "before [mi",'d',"dle] after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da]"),
                "before [mi",'d',"dle] after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da>"),
                "before <mi",'d',"dle> after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da<"),
                "before <mi",'d',"dle> after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da{"),
                "function {\n",'f',"oo\n     }",
                "function",' ',"");

        //skip escaped delimiters
        checkCommand(forKeySeq("da{"),
                "before {m\\{i",'d',"d\\}le} after",
                "before ",' ',"after");
        checkCommand(forKeySeq("da'"),
                "before 'mi",'d',"d\\'le' after",
                "before ",'a',"fter");
        checkCommand(forKeySeq("di'"),
                "'before mi",'d',"dle after'",
                "'",'\'',"");
        checkCommand(forKeySeq("di'"),
                "\\'before mi",'d',"dle after\\'",
                "\\'before mi",'d',"dle after\\'");
    }

    @Test
    public void test_dip() {
        checkCommand(forKeySeq("dip"),
                "",'1',"ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "",'\n',"3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dip"),
                "1ac\n",'\n',"3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dip"),
                "1ac\n\n",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'\n',"  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dip"),
                "1ac\n\n3ac\n",'4',"ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'\n',"  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n",' '," \n\n11ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n",'1',"1ac\n12ac");
        
        // Special cases for file end sections
        checkCommand(forKeySeq("dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n",'1',"1ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n",EOF,"");
        checkCommand(forKeySeq("dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n",'1',"2ac",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n",EOF,"");
        
        checkCommand(forKeySeq("dip"),
                "\n\n\n",'f',"oo\nbar\n\n\n\n",
                "\n\n\n",'\n',"\n\n");
        checkCommand(forKeySeq("dip"),
                "\n",'\n',"",
                "",EOF,"");
        checkCommand(forKeySeq("dip"),
                "hello\n",'w',"orld",
                "",EOF,"");
        checkCommand(forKeySeq("dip"),
                "hello\n",'w',"orld\n",
                "",EOF,"");
        checkCommand(forKeySeq("dip"),
                "\n\n",'h',"ello\nworld",
                "\n",EOF,"");
        checkCommand(forKeySeq("dip"),
                "",'h',"ello",
                "",EOF,"");
        checkCommand(forKeySeq("dip"),
                "",EOF,"",
                "",EOF,"");
        
        // Cases for buffers with EOL at the end
        checkCommand(forKeySeq("dip"),
                "\n\n",'h',"ello\nworld\n",
                "\n\n",EOF,"");
        checkCommand(forKeySeq("dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac\n",'\n',"\n",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac\n",EOF,"");
	}
	
	@Test
    public void test_2dip() {
        checkCommand(forKeySeq("2dip"),
                "",'1',"ac\n\n3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("2dip"),
                "1ac\n",'\n',"3ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n",'\n',"  \n7ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("2dip"),
                "1ac\n\n",'3',"ac\n4ac\n\n  \n7ac\n\n  \n\n11ac\n12ac",
                "1ac\n\n",'7',"ac\n\n  \n\n11ac\n12ac");
        checkCommand(forKeySeq("2dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n",'\n',"  \n\n11ac\n12ac",
                "1ac\n\n3ac\n4ac\n\n  \n",'7',"ac");
        
        // Cases for buffers with EOL at the end
        checkCommand(forKeySeq("2dip"),
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n",'\n',"  \n\n11ac\n12ac\n",
                "1ac\n\n3ac\n4ac\n\n  \n7ac\n",EOF,"");
	}

	@Test
	public void test_gq_mergeLines() {
		//Some of these tests move the cursor when they shouldn't;
		//some of these tests append a newline when they shouldn't.
		//If we were to fix these behaviors, these tests might fail
		//but that doesn't mean the fixes aren't valid.
		//I'm mostly focusing on when lines are merged or not.
        checkCommand(forKeySeq("gqj"),
                "line",' ',"one\nline two",
                "",'l',"ine one line two\n");
        
        checkCommand(forKeySeq("gqj"),
                "* line",' ',"one\n  * line two",
                "",'*'," line one line two\n");
        
        checkCommand(forKeySeq("gqj"),
                "// line",' ',"one\n  // line two",
                "",'/',"/ line one line two\n");
        
        checkCommand(forKeySeq("gqj"),
                "# line",' ',"one\n  # line two",
                "",'#'," line one line two\n");
        
        checkCommand(forKeySeq("gqj"),
                "# line",' ',"one\n  #       line two",
                "",'#'," line one line two\n");
        
        checkCommand(forKeySeq("gqj"),
                "# line",' ',"one\n      #line two",
                "",'#'," line one line two\n");
        
        checkCommand(forKeySeq("gq2j"),
                "", '/', "*\n* line one\n*/",
                "", '/', "*\n* line one\n*/\n");
        
        checkCommand(forKeySeq("gq2j"),
                "", '/', "* foo\n* line one\n*/",
                "", '/', "* foo line one\n*/\n");
        
        checkCommand(forKeySeq("gqj"),
                "/* line",' ',"one */\n  /* line two */",
                "",'/',"* line one */\n  /* line two */\n");
        
        //this shouldn't actually move the cursor,
        //I just want to verify it doesn't merge un-common comment types
        checkCommand(forKeySeq("gqj"),
                "* line",' ',"one\n  // line two",
                "",'*'," line one\n  // line two\n");
        
        checkCommand(forKeySeq("gq2j"),
                "* line",' ',"one\n  line two\n* line three",
                "",'*'," line one\n  line two\n* line three\n");
        
        checkCommand(forKeySeq("gqj"),
                "// line",' ',"one\n  line two",
                "",'/',"/ line one\n  line two\n");
        
        //blank line
        checkCommand(forKeySeq("gqj"),
                "* line",' ',"one\n * \n  * line two",
                "",'*'," line one\n * \n  * line two");
	}
	
	@Test
	public void test_gq_splitLines() {
        
		configuration.set(Options.TEXT_WIDTH, 20);
		
        checkCommand(forKeySeq("gqq"),
                "// this",' ',"line is longer than text width and should be split",
                "",'/',"/ this line is\n// longer than text\n// width and should\n// be split\n");
        
        checkCommand(forKeySeq("gqq"),
                "* this",' ',"line is longer than text width and should be split",
                "",'*'," this line is\n* longer than text\n* width and should\n* be split\n");
        
        checkCommand(forKeySeq("gqq"),
                "# this",' ',"line is longer than text width and should be split",
                "",'#'," this line is\n# longer than text\n# width and should\n# be split\n");
        
        //ensure new lines use same indentation as first line
        checkCommand(forKeySeq("gqq"),
                "    # this",' ',"line is longer than text width and should be split",
                "",' ',"   # this line is\n    # longer than\n    # text width and\n    # should be\n    # split\n");
        checkCommand(forKeySeq("gqq"),
                "    #   this",' ',"line is longer than text width and should be split",
                "",' ',"   #   this line is\n    #   longer than\n    #   text width\n    #   and should\n    #   be split\n");
        
		configuration.set(Options.TEXT_WIDTH, 30);
        
        checkCommand(forKeySeq("gqj"),
                "// line",' ',"one\n  // line two is longer and will be split",
                "",'/',"/ line one line two is longer\n// and will be split\n");
        
        checkCommand(forKeySeq("gqj"),
                "// line",' ',"one is longer than line two,\n  // line two will be merged",
                "",'/',"/ line one is longer than\n// line two, line two will be\n// merged\n");
        
		configuration.set(Options.TEXT_WIDTH, 3);
        
        checkCommand(forKeySeq("gqq"),
                "# this",' ',"line will be split multiple times",
                "", '#', " this\n# line\n# will\n# be\n# split\n# multiple\n# times\n");
        
        checkCommand(forKeySeq("gqq"),
                "/* this",' ',"line will be split multiple times */",
                "", '/', "* this\n* line\n* will\n* be\n* split\n* multiple\n* times\n* */\n");

        configuration.set(Options.TEXT_WIDTH, 30);

        // Ensure tabs are counted as if they were 8 spaces (default tabstop)
        checkCommand(forKeySeq("gqq"),
                "\t# this",' ',"line will be split multiple times to fit textwidth\n",
                "", '\t', "# this line will be\n\t# split multiple times\n\t# to fit textwidth\n");

        // [TODO] Vim actually puts a space between the tab char and '*' to align with '/*'
        checkCommand(forKeySeq("gqq"),
                "\t/* this",' ',"line will be split multiple times to fit textwidth */",
                "", '\t', "/* this line will be\n\t* split multiple times\n\t* to fit textwidth */\n");
	}
	
	@Test
    public void testSurroundPlugin_ds() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("dsb"),
                "array[(in",'d',"ex)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[(in",'d',"ex)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[",'(',"index)];",
                "array[",'i',"ndex];");
        checkCommand(forKeySeq("ds("),
                "array[(index",')',"];",
                "array[",'i',"ndex];");
    }

    @Test
    public void testSurroundPlugin_cs() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("cs[b"),
                "fn[ar",'g',"ument];",
                "fn",'(',"argument);");
        checkCommand(forKeySeq("cs)("),
                "fn(ar",'g',"ument);",
                "fn",'('," argument );");
        checkCommand(forKeySeq("cs()"),
                "fn(  ar",'g',"ument  );",
                "fn",'(',"argument);");
    }

	@Test
    public void testSurroundPlugin_cs_input() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        when(platform.getPlatformSpecificModeProvider()).thenReturn(
                (PlatformSpecificModeProvider) new SurroundModesProvider());
        reloadEditorAdaptor();
        checkCommand(forKeySeq("cs[tok<RETURN>"),
                "fn[ar",'g',"ument];",
                "fn",'<',"ok>argument</ok>;");
        checkCommand(forKeySeq("cs)<LT>p><RETURN>"),
                "fn(ar",'g',"ument);",
                "fn",'<',"p>argument</p>;");
        checkCommand(forKeySeq("yswtok<RETURN>"),
                "fn[ar",'g',"ument];",
                "fn[ar",'<',"ok>gument</ok>];");
        checkCommand(forKeySeq("ysE<LT>p<RETURN>"),
                "fn(ar",'g',"ument);",
                "fn(ar",'<',"p>gument);</p>");
    }

	@Test
    public void testSurroundPlugin_cs_replaceTag() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        when(platform.getPlatformSpecificModeProvider()).thenReturn(
                (PlatformSpecificModeProvider) new SurroundModesProvider());
        reloadEditorAdaptor();
        // Simple replaces
        checkCommand(forKeySeq("cst<LT>ok<RETURN>"),
                "<root>\r",' ',"   <property>nill</property>\r</root>",
                "<root>\r    ",'<',"ok>nill</ok>\r</root>");
        checkCommand(forKeySeq("cst<LT>ok<GT><RETURN>"),
                "<root>\r",' ',"   <property>nill</property>\r</root>",
                "<root>\r    ",'<',"ok>nill</ok>\r</root>");
        
        // Replace tag, keep attributes (no <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok<RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok value=\"nill\"></ok>\r</root>");
        
        // Replace tag, remove attributes (see <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok<GT><RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok></ok>\r</root>");
        
        // Replace tag, add extra attributes (no <GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok type=\"String\"<RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok type=\"String\" value=\"nill\"></ok>\r</root>");
        
        // Replace tag, overwrite attribute (<GT> at end of replacement)
        checkCommand(forKeySeq("cst<LT>ok type=\"String\"<GT><RETURN>"),
                "<root>\r",' ',"   <property value=\"nill\"></property>\r</root>",
                "<root>\r    ",'<',"ok type=\"String\"></ok>\r</root>");
    }
	
	@Test
    public void testSurroundPlugin_ys() {
        SurroundStateProvider provider = new SurroundStateProvider();
        provider.initializeProvider(adaptor.getTextObjectProvider());
        when(platform.getPlatformSpecificStateProvider(Mockito.<TextObjectProvider>any()))
                .thenReturn(provider);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("ysiwb"),
                "so",'m',"ething",
                "",'(',"something)");

        checkCommand(forKeySeq("ys2iwb"),
                "so",'m',"ething funny",
                "",'(',"something funny)");

        checkCommand(forKeySeq("yssb"),
                "so",'m',"ething funny",
                "",'(',"something funny)");
    }

	@Test
    public void testYanking() {
        checkCommand(forKeySeq("yiw"),
                "so",'m',"ething",
                "",'s',"omething");
        assertYanked(ContentType.TEXT, "something");
    }
	
	@Test
	public void testCursorAfterYank() {
	    checkCommand(forKeySeq("yy"),
	            "first", ' ', "line\nsecond line\nthird line",
	            "first", ' ', "line\nsecond line\nthird line");
        assertYanked(ContentType.LINES, "first line\n");
        
	    checkCommand(forKeySeq("yy"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first line\nsecond", ' ', "line\nthird line");
        assertYanked(ContentType.LINES, "second line\n");
        
	    checkCommand(forKeySeq("yy"),
	            "first line\nsecond line\nthird", ' ', "line",
	            "first line\nsecond line\nthird", ' ', "line");
        assertYanked(ContentType.LINES, "third line\n");
        
	    checkCommand(forKeySeq("yk"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first ", 'l', "ine\nsecond line\nthird line");
        assertYanked(ContentType.LINES, "first line\nsecond line\n");
        
	    checkCommand(forKeySeq("yj"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first line\nsecond", ' ', "line\nthird line");
        assertYanked(ContentType.LINES, "second line\nthird line\n");
        
	    checkCommand(forKeySeq("y2k"),
	            "first line\nsecond line\nthird", ' ', "line",
	            "first", ' ', "line\nsecond line\nthird line");
        assertYanked(ContentType.LINES, "first line\nsecond line\nthird line\n");
        
	    checkCommand(forKeySeq("y2j"),
	            "first", ' ', "line\nsecond line\nthird line",
	            "first", ' ', "line\nsecond line\nthird line");
        assertYanked(ContentType.LINES, "first line\nsecond line\nthird line\n");
        
	    checkCommand(forKeySeq("ygg"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first ", 'l', "ine\nsecond line\nthird line");
        assertYanked(ContentType.LINES, "first line\nsecond line\n");
        
	    checkCommand(forKeySeq("yG"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first line\nsecond", ' ', "line\nthird line");
        assertYanked(ContentType.LINES, "second line\nthird line\n");
        
	    checkCommand(forKeySeq("y$"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first line\nsecond", ' ', "line\nthird line");
        assertYanked(ContentType.TEXT, " line");
        
	    checkCommand(forKeySeq("y0"),
	            "first line\nsecond", ' ', "line\nthird line",
	            "first line\n", 's', "econd line\nthird line");
        assertYanked(ContentType.TEXT, "second");
        
	    checkCommand(forKeySeq("yw"),
	            "first line\nse", 'c', "ond line\nthird line",
	            "first line\nse", 'c', "ond line\nthird line");
        assertYanked(ContentType.TEXT, "cond ");
        
	    checkCommand(forKeySeq("yb"),
	            "first line\nse", 'c', "ond line\nthird line",
	            "first line\n", 's', "econd line\nthird line");
        assertYanked(ContentType.TEXT, "se");
        
	    checkCommand(forKeySeq("yiw"),
	            "first line\nse", 'c', "ond line\nthird line",
	            "first line\n", 's', "econd line\nthird line");
        assertYanked(ContentType.TEXT, "second");
	}

	@Test
    public void test_deleteLastLines() {
        checkCommand(forKeySeq("dd"),
                "sth\n",EOF,"",
                "",'s',"th");
        checkCommand(forKeySeq("dj"),
                "   abc\nst",'h',"\nsth",
                "   ",'a',"bc");
    }

	@Test
    public void test_J() {
        checkCommand(forKeySeq("J"),
                "s",'t',"h\nsth",
                "sth",' ',"sth");
        // don't append space if current
        // line ends with whitespace. D'oh!
        checkCommand(forKeySeq("J"),
                "s",'t',"h   \nsth",
                "sth   ",'s',"th");
        checkCommand(forKeySeq("2J"),
                "s",'t',"h\nsth\nsth",
                "sth", ' ', "sth\nsth");
        checkCommand(forKeySeq("J"),
                "s",'t',"h\n   sth",
                "sth",' ',"sth");
        checkCommand(forKeySeq("JJ"),
                "s",'t',"h\n\nsth",
                "sth ",'s',"th");
        checkCommand(forKeySeq("J"),
                "",'\n',"hello",
                "",'h',"ello");
        checkCommand(forKeySeq("3J"),
                "th",'i',"s\njoins\nthree lines",
                "this joins",' ',"three lines");
    }

	@Test
    public void test_gJ() {
        checkCommand(forKeySeq("gJ"),
                "s",'t',"h\nsth",
                "sth",'s',"th");
        checkCommand(forKeySeq("2gJ"),
                "s",'t',"h\nsth\nsth",
                "sth",'s',"th\nsth");
        checkCommand(forKeySeq("gJ"),
                "s",'t',"h\n   sth",
                "sth",' ',"  sth");
    }

	@Test
    public void testJoinLastLine() {
        checkCommand(forKeySeq("J"),
                "no",'o',"p",
                "no",'o',"p");
        verify(userInterfaceService).setErrorMessage("there is nothing to join below last line");
	}

	@Test
    public void testJoinLastLineDumbWay() {
        checkCommand(forKeySeq("gJ"),
                "no",'o',"p",
                "no",'o',"p");
        verify(userInterfaceService).setErrorMessage("there is nothing to join below last line");
	}

    @Test
    public void test_Percent() {
        // Shouldn't do anything
        checkCommand(forKeySeq("500%"),
                "1\n",'2',"\n3\n4\n5\n6\n7\n8\n9\n10\n",
                "1\n",'2',"\n3\n4\n5\n6\n7\n8\n9\n10\n");
        checkCommand(forKeySeq("100%"),
                "1\n",'2',"\n3\n4\n5\n6\n7\n8\n9\n10",
                "1\n2\n3\n4\n5\n6\n7\n8\n9\n",'1',"0");
        // Should go to first non-whitespace character
        checkCommand(forKeySeq("100%"),
                "1\n",'2',"\n3\n4\n5\n6\n7\n8\n9\n    10",
                "1\n2\n3\n4\n5\n6\n7\n8\n9\n    ",'1',"0");
        checkCommand(forKeySeq("%"),
                "fun",'(',"call);",
                "fun(call",')',";");
        checkCommand(forKeySeq("%"),
                "fun(call",')',";",
                "fun",'(',"call);");
    }

    @Test
    public void test_dPercent() {
        // Shouldn't do anything
        checkCommand(forKeySeq("d500%"),
                "fun",'(',"call);",
                "fun",'(',"call);");
        // Deletes current line up to and including last line
        checkCommand(forKeySeq("d100%"),
                "oh my\nfun",'(',"call);",
                "oh my\n", EOF, "");
        checkCommand(forKeySeq("d100%"),
                "oh my\nfun",'(',"call);\nis this\nhere",
                "oh my\n", EOF, "");
        checkCommand(forKeySeq("d%"),
                "fun",'(',"call);",
                "fun",';',"");
        checkCommand(forKeySeq("d%"),
                "fun(call",')',";",
                "fun",';',"");
    }

	@Test
	public void test_incrementDecimal() throws Exception {
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'3',"4",
	            "xx3",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'3',"4x",
	            "xx3",'5',"x");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"xx34",
	            "xxxxx3",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"xx34x",
	            "xxxxx3",'5',"x");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"xxx",
	            "xx",'x',"xxx");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "11",'1',"111",
	            "11111",'2',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "",'9',"",
	            "1",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "10",'.',"1",
	            "10.",'2',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "10",'.',"9",
	            "10.1",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "",'0',"",
	            "",'1',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "-",'1',"",
	            "",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "",'-',"1",
	            "",'0',"");
	    
	    checkCommand(forKeySeq("5<C-a>"),
	            "",'0',"",
	            "",'5',"");
	    
	    checkCommand(forKeySeq("5<C-a>"),
	            "-",'2',"",
	            "",'3',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"xx34xx0x234",
	            "xxxxx3",'5',"xx0x234");
	    
	    // Check that rollover doesn't happen at 2^31 (Java int is signed)
	    checkCommand(forKeySeq("<C-a>"),
	            "0", 'x', "7FFFFFFF",
	            "0x8000000", '0', "");
	    
	    checkCommand(forKeySeq("256<C-a>"),
	            "0", 'x', "7FFFFFFF",
	            "0x800000F", 'F', "");
	}

	@Test
	public void test_decrementDecimal() throws Exception {
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'3',"4",
	            "xx3",'3',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'3',"4x",
	            "xx3",'3',"x");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'x',"xx34",
	            "xxxxx3",'3',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'x',"xx34x",
	            "xxxxx3",'3',"x");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'x',"xxx",
	            "xx",'x',"xxx");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "11",'1',"111",
	            "11111",'0',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "1",'0',"",
	            "",'9',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "10",'.',"1",
	            "10.",'0',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "10.1",'0',"",
	            "10.",'9',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "",'0',"",
	            "-",'1',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "-",'1',"",
	            "-",'2',"");
	    
	    checkCommand(forKeySeq("5<C-x>"),
	            "",'0',"",
	            "-",'5',"");
	    
	    checkCommand(forKeySeq("5<C-x>"),
	            "",'5',"",
	            "",'0',"");
	}
	
	@Test
	public void test_incrDecrOctal() {
		//increment
	    checkCommand(forKeySeq("<C-a>"),
	            "xx0",'3',"4",
	            "xx03",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx0",'3',"7",
	            "xx04",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx0",'7',"7",
	            "xx010",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx000",'7',"7",
	            "xx0010",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "000",'7',"7",
	            "0010",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "",'0',"077",
	            "010",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"x077",
	            "xxxx010",'0',"");
	    
	    checkCommand(forKeySeq("5<C-a>"),
	            "",'0',"77",
	            "010",'4',"");
	    
	    //decrement
	    checkCommand(forKeySeq("<C-x>"),
	            "xx0",'3',"4",
	            "xx03",'3',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx0",'4',"0",
	            "xx03",'7',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx0",'1',"00",
	            "xx007",'7',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'x',"x0100",
	            "xxxx007",'7',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx001",'0',"0",
	            "xx0007",'7',"");
	    
	    //not actually octal
	    checkCommand(forKeySeq("<C-a>"),
	            "xx0",'1',"90",
	            "xx19",'1',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx0",'1',"90",
	            "xx18",'9',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx0",'9',"8",
	            "xx9",'7',"");
	}
	
	@Test
	public void test_incrDecrHex() {
		//increment
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0x",'3',"4",
	            "foo0x3",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0X",'3',"4",
	            "foo0X3",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0x0",'3',"4",
	            "foo0x03",'5',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0x0",'3',"a",
	            "foo0x03",'b',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0xa0",'3',"a",
	            "foo0xa03",'b',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0xa0",'0',"f",
	            "foo0xa01",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "0xa0",'0',"f",
	            "0xa01",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "0xaa",'a',"a",
	            "0xaaa",'b',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "0xaa",'a',"ag",
	            "0xaaa",'b',"g");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "",'0',"xabc",
	            "0xab",'d',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "0",'x',"abc",
	            "0xab",'d',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"x0xa00f",
	            "xxxx0xa01",'0',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "aa",'a',"a0xabc",
	            "aaaa0xab",'d',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"x0xABCF",
	            "xxxx0xABD",'0',"");
	    
	    checkCommand(forKeySeq("5<C-a>"),
	            "",'0',"xabc",
	            "0xac",'1',"");
	    
	    checkCommand(forKeySeq("<C-a>"),
	            "xx",'x',"x0x234xx34",
	            "xxxx0x23",'5',"xx34");
	    
		//decrement
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0x",'3',"4",
	            "foo0x3",'3',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0x0",'3',"4",
	            "foo0x03",'3',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0x0",'3',"a",
	            "foo0x03",'9',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0xa0",'3',"a",
	            "foo0xa03",'9',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0xa0",'1',"0",
	            "foo0xa00",'f',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "0xa0",'1',"0",
	            "0xa00",'f',"");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "xx",'x',"x0xa00f",
	            "xxxx0xa00",'e',"");
	    
	    //not actually hex
	    checkCommand(forKeySeq("<C-a>"),
	            "foo0xxa",'0',"f",
	            "foo0xxa",'1',"f");
	    
	    checkCommand(forKeySeq("<C-x>"),
	            "foo0xxa",'0',"f",
	            "foo0xxa-",'1',"f");
	}

	@Test
	public void test_indent_default() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true); // VimTestCase forced to false
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    checkCommand(forKeySeq(">>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n\tThis\nShould be indented");
	    // Expand tab
	    checkCommand(forKeySeq("3>>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n                This\n        Should be indented");
	    checkCommand(forKeySeq(">>"),
	            "        Ok", 'a', "y\n\tThis\nShould be indented",
	            "                ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "Okay\n   Th", 'i', "s\nShould be indented",
	            "Okay\n           ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_indent_shiftround() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true); // VimTestCase forced to false
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(true);
	    checkCommand(forKeySeq(">>"),
	            "  Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "       Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "         Ok", 'a', "y\n\tThis\nShould be indented",
	            "                ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\tTh", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t Th", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t       Th", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t         Th", 'i', "s\nShould be indented",
	            "Okay\n                        ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_indent_expandtab() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    checkCommand(forKeySeq("3>>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n                This\n        Should be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t        Th", 'i', "s\nShould be indented",
	            "Okay\n                        ", 'T', "his\nShould be indented");

	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(false);
	    checkCommand(forKeySeq("3>>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "\t", 'O', "kay\n\t\tThis\n\tShould be indented");
	    // Fix mixed tabs and spaces, other way around
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t        Th", 'i', "s\nShould be indented",
	            "Okay\n\t\t\t", 'T', "his\nShould be indented");
	}

	@Test
	public void test_indent_shiftwidth() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(2);
	    checkCommand(forKeySeq("3>>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "  ", 'O', "kay\n          This\n  Should be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t        Th", 'i', "s\nShould be indented",
	            "Okay\n                  ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_indent_tabstop() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    when(configuration.get(Options.TAB_STOP)).thenReturn(4);
	    checkCommand(forKeySeq("3>>"),
	            "Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n            This\n        Should be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq(">>"),
	            "Okay\n\t        Th", 'i', "s\nShould be indented",
	            "Okay\n                    ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_unindent_default() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true); // VimTestCase forced to false
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    checkCommand(forKeySeq("<<"),
	            "        Ok", 'a', "y\n\tThis\nShould be indented",
	            "", 'O', "kay\n\tThis\nShould be indented");
	    // Expand tab
	    checkCommand(forKeySeq("3<<"),
	            "        Ok", 'a', "y\n\t        This\n        Should be indented",
	            "", 'O', "kay\n        This\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "                Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "Okay\n           Th", 'i', "s\nShould be indented",
	            "Okay\n   ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_unindent_shiftround() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true); // VimTestCase forced to false
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(true);
	    checkCommand(forKeySeq("<<"),
	            "  Ok", 'a', "y\n\tThis\nShould be indented",
	            "", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "        Ok", 'a', "y\n\tThis\nShould be indented",
	            "", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "         Ok", 'a', "y\n\tThis\nShould be indented",
	            "        ", 'O', "kay\n\tThis\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t       Th", 'i', "s\nShould be indented",
	            "Okay\n        ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t         Th", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t       Th", 'i', "s\nShould be indented",
	            "Okay\n        ", 'T', "his\nShould be indented");
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t         Th", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_unindent_expandtab() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    checkCommand(forKeySeq("3<<"),
	            "        Ok", 'a', "y\n\t        This\n        Should be indented",
	            "", 'O', "kay\n        This\nShould be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t                Th", 'i', "s\nShould be indented",
	            "Okay\n                ", 'T', "his\nShould be indented");

	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(false);
	    checkCommand(forKeySeq("3<<"),
	            "\t        Ok", 'a', "y\n\t\tThis\n\tShould be indented",
	            "\t", 'O', "kay\n\tThis\nShould be indented");
	    // Fix mixed tabs and spaces, other way around
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t        \tTh", 'i', "s\nShould be indented",
	            "Okay\n\t\t", 'T', "his\nShould be indented");
	}

	@Test
	public void test_unindent_shiftwidth() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    when(configuration.get(Options.SHIFT_WIDTH)).thenReturn(2);
	    checkCommand(forKeySeq("3<<"),
	            "          Ok", 'a', "y\n\t  This\n          Should be indented",
	            "        ", 'O', "kay\n        This\n        Should be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t            Th", 'i', "s\nShould be indented",
	            "Okay\n                  ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_unindent_tabstop() throws Exception {
	    when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
	    when(configuration.get(Options.SHIFT_ROUND)).thenReturn(false);
	    when(configuration.get(Options.TAB_STOP)).thenReturn(4);
	    checkCommand(forKeySeq("3<<"),
	            "        Ok", 'a', "y\n\t\t\tThis\n        Should be indented",
	            "", 'O', "kay\n    This\nShould be indented");
	    // Fix mixed tabs and spaces
	    checkCommand(forKeySeq("<<"),
	            "Okay\n\t                Th", 'i', "s\nShould be indented",
	            "Okay\n            ", 'T', "his\nShould be indented");
	}

	@Test
	public void test_sentenceMotion() throws Exception {
	    checkCommand(forKeySeq(")"),
	            "This ",'i',"s a sentence",
	            "This is a sentenc", 'e', "");
	    
	    checkCommand(forKeySeq(")"),
	            "This ",'i',"s a sentence.  This is another one.",
	            "This is a sentence.  ",'T',"his is another one.");
	    
	    checkCommand(forKeySeq(")"),
	            "This ",'i',"s a sentence?????  This is another one.",
	            "This is a sentence?????  ",'T',"his is another one.");
	    
	    checkCommand(forKeySeq(")"),
	            "This ",'i',"s a sentence?????')  This is another one.",
	            "This is a sentence?????')  ",'T',"his is another one.");
	    
	    checkCommand(forKeySeq(")"),
	            "This ",'i',"s a sentence\n\n\nThis is another one.",
	            "This is a sentence\n",'\n',"\nThis is another one.");
	    
	    checkCommand(forKeySeq(")"),
	            "This is a sentence\n",'\n',"\nThis is another one.",
	            "This is a sentence\n\n\n",'T',"his is another one.");
	    
	    checkCommand(forKeySeq("("),
	            "This ",'i',"s a sentence",
	            "", 'T', "his is a sentence");
	    
	    checkCommand(forKeySeq("("),
	            "This is a sentence.  This i", 's', " another one.",
	            "This is a sentence.  ", 'T', "his is another one.");
	    
	    checkCommand(forKeySeq("("),
	            "This is a sentence.  ", 'T', "his is another one.",
	            "", 'T', "his is a sentence.  This is another one.");
	    
	    checkCommand(forKeySeq("("),
	            "This is a sentence\n\n\n", 'T', "his is another one.",
	            "This is a sentence\n\n",'\n',"This is another one.");
	    
	    checkCommand(forKeySeq("("),
	            "This is a sentence\n\n",'\n',"This is another one.",
	            "", 'T', "his is a sentence\n\n\nThis is another one.");
	    
	    checkCommand(forKeySeq("("),
	            "This is a sentence?????')  ", 'T', "his is another one.",
	            "", 'T', "his is a sentence?????')  This is another one.");
	    
	    checkCommand(forKeySeq(")"),
	            "This is a sentenc", 'e', "",
	            "This is a sentenc", 'e', "");
	    
	    checkCommand(forKeySeq("("),
	            "", 'T', "his is a sentence",
	            "", 'T', "his is a sentence");
	}

	@Test
	public void test_cfx() throws Exception {
        checkCommand(forKeySeq("ctx"),
                "so",'m',"ething",
                "so",'m',"ething");
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
        verify(userInterfaceService).setErrorMessage("'x' not found");
	}

	@Test
	public void test_dot_on_i() {
        installSaneRegisterManager();
	    checkCommand(forKeySeq("ideja-vu <Esc>l."),
	            "This is ",'.',"",
	            "This is deja-vu deja-vu",' ',".");
	    checkCommand(forKeySeq("adeja-vu <Esc>."),
	            "This is",' ',".",
	            "This is deja-vu deja-vu",' ',".");
	    // Sanity checks
	    checkCommand(forKeySeq("2ideja-vu <Esc>"),
	            "This is ",'.',"",
	            "This is deja-vu deja-vu",' ',".");
	    checkCommand(forKeySeq("2adeja-vu <Esc>"),
	            "This is",' ',".",
	            "This is deja-vu deja-vu",' ',".");
	    // dot with count
	    checkCommand(forKeySeq("2ideja-vu <Esc>."),
	            "This is ",'.',"",
	            "This is deja-vu deja-vu deja-vu deja-vu",' ',".");
	    checkCommand(forKeySeq("2adeja-vu <Esc>."),
	            "This is",' ',".",
	            "This is deja-vu deja-vu deja-vu deja-vu",' ',".");
	}

	@Test
	public void test_dot_on_c() {
        installSaneRegisterManager();
	    checkCommand(forKeySeq("ceXXX<Esc>w."),
	            "Ala ",'m',"a kota i psa",
	            "Ala XXX XX",'X'," i psa");
	}
	
	@Test
	public void test_CtrlC_cancels() {
		checkCommand(forKeySeq("rx<space><space>r<C-c>"),
				"replace ", 'a', "bcd",
				"replace xb", 'c', "d");
		checkCommand(forKeySeq("/xxx<C-c>"),
				"", 'a', " test is this ... xxx abc",
				"", 'a', " test is this ... xxx abc");
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}
	
	@Test
	public void test_map() {
		type(parseKeyStrokes(":no d h<enter>"));
		type(parseKeyStrokes(":no e d<enter>"));
		checkCommand(forKeySeq("d"),
				"first line\nsecond ", 'l', "ine\nthird line",
				"first line\nsecond", ' ', "line\nthird line");
		checkCommand(forKeySeq("5d"),
				"first line\nsecond ", 'l', "ine\nthird line",
				"first line\nse", 'c', "ond line\nthird line");
		checkCommand(forKeySeq("2ee"),
				"first line\nsecond ", 'l', "ine\nthird line",
				"", 'f', "irst line");
		checkCommand(forKeySeq("5ed"),
				"first line\nsecond ", 'l', "ine\nthird line",
				"first line\nse", 'l', "ine\nthird line");
	}

	@Test
	public void testInsertModeCounts() {
		installSaneRegisterManager();
        checkCommand(forKeySeq("5ib <esc>"),
                "x",'x',"x",
                "xb b b b b",' ',"xx");
        checkCommand(forKeySeq("."),
                "xb b b b b",' ',"xx",
                "xb b b b b b b b b b",' ',"xx");
        checkCommand(forKeySeq("5ab <esc>"),
                "a",'a',"a",
                "aab b b b b",' ',"a");
	}

	@Test
	public void testCountingDeleteToRegister() {
	    installSaneRegisterManager();
	    Register defaultReg = registerManager.getDefaultRegister();

	    checkCommand(forKeySeq("\"_3dl"),
	            "This is",' ',"a deja-vu.",
	            "This is",'d',"eja-vu.");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());

	    checkCommand(forKeySeq("3\"_dl"),
	            "This is",' ',"a deja-vu.",
	            "This is",'d',"eja-vu.");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());

	    checkCommand(forKeySeq("\"_3dd"),
	            "a", 'b', "c\ndef\nghi\njkl",
	            "", 'j', "kl");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());

	    checkCommand(forKeySeq("3\"_dd"),
	            "a", 'b', "c\ndef\nghi\njkl",
	            "", 'j', "kl");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());

	    // Crazy - this does multiplication: equivalent to "_4dd
	    checkCommand(forKeySeq("2\"_2dd"),
	            "a", 'b', "c\ndef\nghi\njkl\n\n\n",
	            "", '\n', "\n");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());

	    // Craziest it gets - 3 different counts get multiplied: equivalent to "_d8l
	    checkCommand(forKeySeq("\"_d8l"),
	            "a", 'b', "cdefghijklm",
	            "a", 'j', "klm");
	    checkCommand(forKeySeq("2\"_2d2l"),
	            "a", 'b', "cdefghijklm",
	            "a", 'j', "klm");
	    assertEquals("Black hole register was not used!", "", defaultReg.getContent().getText());
	}
	
	@Test
	public void testSwitchRegister() {
	    installSaneRegisterManager();
		checkCommand(forKeySeq("Vj\"fddd"),
				"",'1',"\n\n2\n\n3",
				"",'\n',"3");
	    assertEquals("F register had incorrect contents", "1\n\n", registerManager.getRegister("f").getContent().getText());
	    assertEquals("Last edit register had incorrect contents", "2\n", registerManager.getRegister("\"").getContent().getText());
	}
	
    @Test
    public void testPasteBeforeCommandPosition() {
        //yank a word into the "a" register and Paste it 2 times
        checkCommand(forKeySeq("\"ayw2\"aP"),
                "Ala ",'m', "a kota",
                "Ala ma ma",' ', "ma kota");

        checkCommand(forKeySeq("yw2P"),
                "Ala ",'m', "a kota",
                "Ala ma ma",' ', "ma kota");

        checkCommand(forKeySeq("yw2p"),
                "Ala ",'m', "a kota",
                "Ala mma ma",' ', "a kota");

        checkCommand(forKeySeq("yy2p"),
                "Ala ",'m', "a kota",
                "Ala ma kota\n",'A', "la ma kota\nAla ma kota");

        checkCommand(forKeySeq("yy2P"),
                "Ala ",'m', "a kota",
                "",'A', "la ma kota\nAla ma kota\nAla ma kota");
    }
}
