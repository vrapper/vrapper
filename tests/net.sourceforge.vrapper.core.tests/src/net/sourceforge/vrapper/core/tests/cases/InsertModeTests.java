package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.junit.Test;

public class InsertModeTests extends VimTestCase {

	@Test
	public void test_CtrlC_exits() {
		adaptor.changeModeSafely(InsertMode.NAME);
		assertEquals(InsertMode.NAME, adaptor.getCurrentModeName());
		type(parseKeyStrokes("<C-c>"));
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}
}
