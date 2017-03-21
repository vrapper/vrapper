package net.sourceforge.vrapper.core.tests.cases;

import org.junit.Test;

import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

public class MacroTests extends CommandTestCase {
	
	@Override
	public void setUp() {
		super.setUp();
		registerManager = new DefaultRegisterManager();
		reloadEditorAdaptor();
		adaptor.changeModeSafely(NormalMode.NAME);
	};

	@Test public void testMacro() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//macro not found
		checkCommand(forKeySeq("@x"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//use defined macro
		checkCommand(forKeySeq("@a"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
	}
	
	@Test public void testMultipleMacros() {
		//define macro 'a'
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//define macro 'b'
		checkCommand(forKeySeq("qbcwsomething<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala somethin", 'g', " kota");
		
		//execute macro 'a'
		checkCommand(forKeySeq("@a"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//execute macro 'b'
		checkCommand(forKeySeq("@b"),
				"Ala ",'m', "a kota",
				"Ala somethin", 'g', " kota");
	}
	
	@Test public void testLastMacro() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//use defined macro
		checkCommand(forKeySeq("@a"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//re-use defined macro
		checkCommand(forKeySeq("@@"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//macro not found
		checkCommand(forKeySeq("@x"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//re-use macro not found
		checkCommand(forKeySeq("@@"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//use defined macro
		checkCommand(forKeySeq("@a"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");
		
		//change defined macro
		checkCommand(forKeySeq("qacwblah<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala bla", 'h', " kota");
		
		//re-use modified macro
		checkCommand(forKeySeq("@@"),
				"Ala ",'m', "a kota",
				"Ala bla", 'h', " kota");
	}

	@Test public void testRepeatedMacro() {
		checkCommand(forKeySeq("qcxq10@c"),
				"Ala ",'m', "a kota",
				"", EOF, "");
	}

	@Test public void testRecursiveMacro() {
		// This should simply abort with a CommandExecutionException.
		// If it runs with infinite recursion that it would raise a StackOverFlowError.
		checkCommand(forKeySeq("qch@aqqal@cq@c"),
				"Ala ",'m', "a kota",
				"Ala",' ', "ma kota");
	}

	@Test public void testYankMacro() {
		//yank text into a register to be used later as a macro
		checkCommand(forKeySeq("\"byW"),
				"Ala ",'c', "wfoo<ESC> ma kota",
				"Ala ",'c', "wfoo<ESC> ma kota");

		//execute the register as a macro, changing the word to foo
		checkCommand(forKeySeq("@b"),
				"Ala ",'m', "a kota",
				"Ala foo", ' ' , "kota");
	}
	
	@Test public void testPasteMacroBefore() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");

		//paste before from a register filled by recording a macro
		checkCommand(forKeySeq("\"aP"),
				"Ala ",'m', "a kota",
				"Ala cwfoo<ESC", '>' , "ma kota");
	}
	
	@Test public void testPasteMacroAfter() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");

		//paste after from a register filled by recording a macro
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mcwfoo<ESC", '>' , "a kota");
	}
	
	@Test public void testYankMacro() {
		//yank text into a register to be used later as a macro
		checkCommand(forKeySeq("\"byW"),
				"Ala ",'c', "wfoo<ESC> ma kota",
				"Ala ",'c', "wfoo<ESC> ma kota");

		//execute the register as a macro, changing the word to foo
		checkCommand(forKeySeq("@b"),
				"Ala ",'m', "a kota",
				"Ala foo", ' ' , "kota");
	}
	
	@Test public void testPasteMacroBefore() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");

		//paste before from a register filled by recording a macro
		checkCommand(forKeySeq("\"aP"),
				"Ala ",'m', "a kota",
				"Ala cwfoo<ESC", '>' , "ma kota");
	}
	
	@Test public void testPasteMacroAfter() {
		//define macro
		checkCommand(forKeySeq("qacwfoo<ESC>q"),
				"Ala ",'m', "a kota",
				"Ala fo", 'o', " kota");

		//paste after from a register filled by recording a macro
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mcwfoo<ESC", '>' , "a kota");
	}
	
	@Test public void testRegisters() {
		//yank a word into the "a" register
		checkCommand(forKeySeq("\"ayw"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//paste from the "a" register
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma",' ', "a kota");
		
		//overwrite some text with the contents of the "a" register
		checkCommand(forKeySeq("vw\"ap"),
				"",'A', "la ma kota",
				"ma",' ', "a kota");
		
		//verify that the overwritten text was stored in the default register
		checkCommand(forKeySeq("p"),
				"Ala ",'m', "a kota",
				"Ala mAla ",'m', "a kota");
		
		//verify that the "a" register is unchanged
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma",' ', "a kota");
	}
	
	@Test public void testMultipleRegisters() {
		//yank a word into the "a" register
		checkCommand(forKeySeq("\"ayw"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//yank a word into the "b" register
		checkCommand(forKeySeq("\"byw"),
				"",'A', "la ma kota",
				"",'A', "la ma kota");
		
		//paste from the "a" register
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma",' ', "a kota");
		
		//paste from the "b" register
		checkCommand(forKeySeq("\"bp"),
				"Ala ",'m', "a kota",
				"Ala mAla",' ', "a kota");
	}
	
	@Test public void testRegisterOperations() {
		//store into 'a' with yw
		checkCommand(forKeySeq("\"ayw"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma",' ', "a kota");
		
		//store into 'a' with dw
		checkCommand(forKeySeq("\"adw"),
				"Ala ",'m', "a kota",
				"Ala ",'k', "ota");
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma",' ', "a kota");
		
		//store into 'a' with 2dw
		checkCommand(forKeySeq("\"a2dw"),
				"Ala ",'m', "a kota",
				"Ala",' ', "");
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala mma kot",'a', "a kota");
		
		//store into 'a' with x
		checkCommand(forKeySeq("\"ax"),
				"Ala ",'m', "a kota",
				"Ala ",'a', " kota");
		checkCommand(forKeySeq("\"ap"),
				"Ala ",'m', "a kota",
				"Ala m",'m', "a kota");
		
	}
	
	@Test public void testBlackHoleRegister() {
		//yank a word into the default register ("ma ")
		checkCommand(forKeySeq("yw"),
				"Ala ",'m', "a kota",
				"Ala ",'m', "a kota");
		
		//delete word with black hole register active
		checkCommand(forKeySeq("\"_dw"),
				"Ala ma ",'k', "ota",
				"Ala ma",' ', "");
		
		//verify that the default register is unharmed and immediately active
		checkCommand(forKeySeq("p"),
				"Ala ma",' ', "",
				"Ala ma ma",' ', "");
		
		//verify that the blackhole register is empty
		checkCommand(forKeySeq("\"_p"),
				"Ala ma ",'k', "ota",
				"Ala ma ",'k', "ota");
	}
	
}
