package net.sourceforge.vrapper.core.tests.cases;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.KeyStroke.CTRL;
import static net.sourceforge.vrapper.keymap.KeyStroke.SHIFT;
import static net.sourceforge.vrapper.keymap.SpecialKey.ARROW_LEFT;
import static net.sourceforge.vrapper.keymap.SpecialKey.ARROW_RIGHT;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;

import org.junit.Test;

public class SimpleKeyStrokeTests {

	static void assertEqualsAndHashAsWell(Object obj1, Object obj2) {
		assertEquals(obj1, obj2);
		assertTrue(obj1.hashCode() == obj2.hashCode());
	}

	static void assertNotEqualAndHashAsWell(Object obj1, Object obj2) {
		assertFalse(obj1.equals(obj2));
		assertFalse(obj1.hashCode() == obj2.hashCode());
	}

	@Test
	public void testEqualsAndHashCode() {
		assertEquals(key('a'), key('a'));
		assertFalse(key('a').equals(key('b')));
		assertEquals(key('a').hashCode(), key('a').hashCode());
		assertFalse(key('a').hashCode() == key('z').hashCode());

		assertEqualsAndHashAsWell(key('a'), key('a'));
		assertEqualsAndHashAsWell(key('A'), key('A'));
		assertEqualsAndHashAsWell(ctrlKey('a'), ctrlKey('a'));
		assertEqualsAndHashAsWell(ctrlKey('A'), ctrlKey('A'));
		assertEqualsAndHashAsWell(key(ARROW_LEFT), key(ARROW_LEFT));
		assertEqualsAndHashAsWell(key(CTRL, ARROW_LEFT), key(CTRL, ARROW_LEFT));

		assertNotEqualAndHashAsWell(key('a'), key('A'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('a'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('A'));
		assertNotEqualAndHashAsWell(ctrlKey('A'), ctrlKey('a'));
		assertNotEqualAndHashAsWell(key(CTRL, ARROW_LEFT), key(ARROW_LEFT));
		assertNotEqualAndHashAsWell(key(ARROW_LEFT), key(CTRL, ARROW_LEFT));
		assertNotEqualAndHashAsWell(key(ARROW_LEFT), key(ARROW_RIGHT));
		assertNotEqualAndHashAsWell(key(ARROW_LEFT), key(CTRL, ARROW_RIGHT));
	}

	static void assertToStringReturns(String expected, Object obj) {
		assertEquals(expected, obj.toString());
	}

	@Test
	public void testAutomaticShiftModifier() {
		String notShifted = "`1234567890-=[];'\\,./" + "abc";
		String autoShifted = "~!@#$%^&*()_+{}:\"|<>?" + "ABC";
		for (int i = 0; i < notShifted.length(); i++) {
			assertEquals(0, key(notShifted.charAt(i)).getModifiers());
			assertEquals(CTRL, ctrlKey(notShifted.charAt(i)).getModifiers());
		}
		for (int i = 0; i < autoShifted.length(); i++) {
			assertEquals(0, key(0, autoShifted.charAt(i)).getModifiers());
			assertEquals(SHIFT, key(autoShifted.charAt(i)).getModifiers());
			assertEquals(SHIFT|CTRL, ctrlKey(autoShifted.charAt(i)).getModifiers());
		}
	}

	@Test
	public void testGetChar() {
		assertGetCharReturns('a', key('a'));
		assertGetCharReturns('%', key('%'));
		assertGetCharReturns(']', ctrlKey(']'));
		assertGetCharReturns('a', ctrlKey('a'));
		assertGetCharReturns('A', key('A'));
		assertGetCharReturns('A', ctrlKey('A'));
	}

	private void assertGetCharReturns(char expected, KeyStroke key) {
		assertEquals(expected, key.getCharacter());
	}

	@Test
    public void testPayseKeySeq() {
	    assertEquals(asList(new SimpleKeyStroke(0, SpecialKey.ESC)), parseKeyStrokes("<Esc>"));
    }

}
