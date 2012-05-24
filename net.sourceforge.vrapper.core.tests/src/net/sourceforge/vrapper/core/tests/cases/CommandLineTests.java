package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.LinkedList;
import java.util.Queue;

import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.ComplexOptionEvaluator;

import org.junit.Test;

public class CommandLineTests extends VimTestCase {

    ComplexOptionEvaluator ev = new ComplexOptionEvaluator();

    @Test
    public void testComplexOptionEvaluator() {
        assertSetOption(Options.SELECTION, "blbllb", Options.SELECTION.getLegalValues().toArray(new String[0]));
        assertSetOption(Options.SCROLL_JUMP, "bbshh", 1, 0, -20);
        assertSetOption(Options.SCROLL_OFFSET, "bbshh", 1, 0, -20);
    }
	
	@Test
	public void test_CtrlC_exits() {
		adaptor.changeModeSafely(CommandLineMode.NAME);
		adaptor.getConfiguration().set(Options.IGNORE_CASE, false);
		type(parseKeyStrokes("set ic<C-c>"));
		assertFalse(adaptor.getConfiguration().get(Options.IGNORE_CASE));
		assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
	}

    private <T> void assertSetOption(Option<T> o, String invalid, T... values) {

        for (String name : o.getAllNames()) {
            for (T value : values) {
                testSetOption(o, name, value, value);
            }
            T value = adaptor.getConfiguration().get(o);
            if (invalid != null) {
                testSetOption(o, name, invalid, value);
            }
        }
    }

    private <T> void testSetOption(Option<T> o, String name, T setValue, T resultValue) {
        testSetOption(o, name, setValue.toString(), resultValue);
    }

    private <T> void testSetOption(Option<T> o, String name, String setValue, T resultValue) {
        Queue<String> cmd = new LinkedList<String>();
        cmd.add(name + "=" + setValue.toString());
        ev.evaluate(adaptor, cmd);
        assertEquals(resultValue, adaptor.getConfiguration().get(o));
    }
}
