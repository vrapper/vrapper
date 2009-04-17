package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.sourceforge.vrapper.keymap.KeyStroke;

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
		assertEqualsAndHashAsWell(key('a'), key('a'));
		assertEqualsAndHashAsWell(key('A'), key('A'));
		assertEqualsAndHashAsWell(ctrlKey('a'), ctrlKey('a'));
		assertEqualsAndHashAsWell(ctrlKey('A'), ctrlKey('A'));

		assertNotEqualAndHashAsWell(key('a'), key('A'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('a'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('A'));
		assertNotEqualAndHashAsWell(ctrlKey('A'), ctrlKey('a'));
	}

	static void assertToStringReturns(String expected, Object obj) {
		assertEquals(expected, obj.toString());
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

}
