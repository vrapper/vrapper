package net.sourceforge.vrapper.core.tests.cases;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.plugin.surround.provider.SurroundStateProvider;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.modes.CommandBasedMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;

public class NormalModeTests extends CommandTestCase {
	@Override
	public void setUp() {
		super.setUp();
		mode = new NormalMode(adaptor);
	};

	@Test public void testEnteringNormalModeChangesCaret() {
		mode.enterMode();
		assertEquals(CaretType.RECTANGULAR, cursorAndSelection.getCaret());
	}

	@Test public void test_x() {
		checkCommand(forKeySeq("x"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
	}

	@Test public void test_s() {
		checkCommand(forKeySeq("s"),
				"Al",'a'," ma kota",
				"Al",' ',"ma kota");
		assertYanked(ContentType.TEXT, "a");
		// TODO: obtain correct arguments used by by ChangeOperation when changing mode
//		verify(adaptor).changeMode(InsertMode.NAME);
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

	@Test public void test_cw_sane() {
	    when(configuration.get(Options.SANE_CW)).thenReturn(true);
	    checkCommand(forKeySeq("cw"),
			"Ala",' ',"ma kota",
			"Ala",'m',"a kota");
		assertYanked(ContentType.TEXT, " ");
	}

	@Test public void test_cw_compilant() {
	    when(configuration.get(Options.SANE_CW)).thenReturn(false);
		checkCommand(forKeySeq("cw"),
			"Ala",' ',"ma kota",
			"Ala",' ',"kota");
		assertYanked(ContentType.TEXT, " ma");
	}

	@Test public void test_dw() {
		checkCommand(forKeySeq("dw"),
			"Ala ",'m',"a kota",
			"Ala ",'k',"ota");
		assertYanked(ContentType.TEXT, "ma ");
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

	@Test public void test_p() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, " czasami"));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Ala czasam",'i'," ma kota");
	}

	@Test public void test_p_empty_clipboard() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.TEXT, ""));
		checkCommand(forKeySeq("p"),
				"Al",'a'," ma kota",
				"Al",'a'," ma kota");
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
				"Ala ma kota\n",'A',"s to pies Ali\n");
	}

	// TODO: I don't like Vim's p behaviour for P with text -- make compatibility optional and THEN test it

	@Test public void test_P_lines() {
		defaultRegister.setContent(new StringRegisterContent(ContentType.LINES, "As to pies Ali\n"));
		checkCommand(forKeySeq("P"),
				"Al",'a'," ma kota",
				"",'A',"s to pies Ali\nAla ma kota");
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
	            "ALA",'\n',"ma\nkota");
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
		CommandBasedMode normalMode = (CommandBasedMode) mode;
		normalMode.executeCommand(cmd);
		verify(registerManager).setLastEdit(repetition);
	}

	@Test public void testThereIsNoCommandRegistrationWhenThereIsNoRepetition() throws CommandExecutionException {
		Command cmd = mock(Command.class);
		when(cmd.repetition()).thenReturn(null);
		CommandBasedMode normalMode = (CommandBasedMode) mode;
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
    }
	
	@Test
    public void test_dib() {
        checkCommand(forKeySeq("dib"),
                "call(so",'m',"ething, funny);",
                "call(",')',";");
        checkCommand(forKeySeq("dib"),
                "call(so",'m',"ething(), funny);",
                "call(",')',";");
        checkCommand(forKeySeq("dib"),
                "call(something(),",' ',"funny);",
                "call(",')',";");
        checkCommand(forKeySeq("dib"),
                "call(\nsomething(),",'\n',"funny()\n);",
                "call(",')',";");
        checkCommand(forKeySeq("d2ib"),
                "call(something very(fu",'n',"ny));",
                "call(",')',";");
        checkCommand(forKeySeq("2dib"),
                "call(something, very(fu",'n',"ny));",
                "call(",')',";");
        // as strange as it may look, this is the actual Vim behaviour
        checkCommand(forKeySeq("dib"),
                "call",'(',"something);",
                "call(",')',";");
        checkCommand(forKeySeq("dib"),
                "call(something",')',";",
                "call(",')',";");
        checkCommand(forKeySeq("2dib"),
                "call(something, very",'(',"funny));",
                "call(",')',";");
    }
	
	@Test
    public void test_di_() {
        checkCommand(forKeySeq("di'"),
                "'abc",'\'',"def'",
                "'",'\'',"def'");
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
    public void testSurroundPlugin_ds() {
        when(platform.getPlatformSpecificStateProvider()).thenReturn(SurroundStateProvider.INSTANCE);
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
        when(platform.getPlatformSpecificStateProvider()).thenReturn(SurroundStateProvider.INSTANCE);
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
    public void testSurroundPlugin_ys() {
        when(platform.getPlatformSpecificStateProvider()).thenReturn(SurroundStateProvider.INSTANCE);
        reloadEditorAdaptor();
        checkCommand(forKeySeq("ysiwb"),
                "so",'m',"ething",
                "",'(',"something)");
    }
	
	@Test
    public void testYanking() {
	    when(configuration.get(Options.MOVE_ON_YANK).booleanValue()).thenReturn(true);
        checkCommand(forKeySeq("yiw"),
                "so",'m',"ething",
                "",'s',"omething");
        assertYanked(ContentType.TEXT, "something");        
        
	    when(configuration.get(Options.MOVE_ON_YANK).booleanValue()).thenReturn(false);
        checkCommand(forKeySeq("yiw"),
                "so",'m',"ething",
                "so",'m',"ething");
        assertYanked(ContentType.TEXT, "something");        
    }
	
	@Test
    public void test_dd_lastLine() {
	    // FIXME: currently fails
        checkCommand(forKeySeq("dd"),
                "sth\n",EOF,"",
                "",'s',"th");
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
                "sth sth",' ',"sth");
        checkCommand(forKeySeq("J"),
                "s",'t',"h\n   sth",
                "sth",' ',"sth");
        checkCommand(forKeySeq("JJ"),
                "s",'t',"h\n\nsth",
                "sth ",'s',"th");
    }
	
	@Test
    public void test_gJ() {
        checkCommand(forKeySeq("gJ"),
                "s",'t',"h\nsth",
                "sth",'s',"th");
        checkCommand(forKeySeq("2gJ"),
                "s",'t',"h\nsth\nsth",
                "sthsth",'s',"th");
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
	
}
