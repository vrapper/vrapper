package net.sourceforge.vrapper.core.tests.cases;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class InsertModeTests extends VimTestCase {

	@Test
	public void test_CtrlC_exits() {
		adaptor.changeModeSafely(InsertMode.NAME);
		assertEquals(InsertMode.NAME, adaptor.getCurrentModeName());
		type(parseKeyStrokes("<C-c>"));
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}
}
