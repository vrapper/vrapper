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
}
