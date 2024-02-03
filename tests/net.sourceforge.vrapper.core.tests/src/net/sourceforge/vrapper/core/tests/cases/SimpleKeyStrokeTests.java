package net.sourceforge.vrapper.core.tests.cases;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.SpecialKey.ARROW_LEFT;
import static net.sourceforge.vrapper.keymap.SpecialKey.ARROW_RIGHT;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.EnumSet;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.KeyStroke.Modifier;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.keymap.vim.PlugKeyStroke;
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

		assertNotEqualAndHashAsWell(key('a'), key('A'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('a'));
		assertNotEqualAndHashAsWell(ctrlKey('a'), key('A'));
		assertNotEqualAndHashAsWell(key(ARROW_LEFT), key(ARROW_RIGHT));
	}

	static void assertToStringReturns(String expected, Object obj) {
		assertEquals(expected, obj.toString());
	}
	
	@Test
	public void testKeyStrokeToString() {
	    assertEquals("<C-[><C-r>", ConstructorWrappers.keyStrokesToString(
	            asList(ctrlKey('['), ctrlKey('r'))));
	    assertEquals("<DOWN>", ConstructorWrappers.keyStrokesToString(
	            asList(key(SpecialKey.ARROW_DOWN))));
	    assertEquals("<Plug>(vrapper.window.moveUp)", ConstructorWrappers.keyStrokesToString(
	            asList((KeyStroke) new PlugKeyStroke("(vrapper.window.moveUp)"))));
        // Do these one by one, modifier order is not guaranteed
        assertEquals("<S-F1>", ConstructorWrappers.keyStrokesToString(
                asList((KeyStroke)new SimpleKeyStroke(SpecialKey.F1, EnumSet.of(Modifier.SHIFT)))));
        assertEquals("<D-F1>", ConstructorWrappers.keyStrokesToString(
                asList((KeyStroke)new SimpleKeyStroke(SpecialKey.F1, EnumSet.of(Modifier.COMMAND)))));
        assertEquals("<C-F1>", ConstructorWrappers.keyStrokesToString(
                asList((KeyStroke)new SimpleKeyStroke(SpecialKey.F1, EnumSet.of(Modifier.CONTROL)))));
        assertEquals("<A-F1>", ConstructorWrappers.keyStrokesToString(
                asList((KeyStroke)new SimpleKeyStroke(SpecialKey.F1, EnumSet.of(Modifier.ALT)))));
	}

	@Test
	public void testGetChar() {
		assertGetCharReturns('a', key('a'));
		assertGetCharReturns('%', key('%'));
		assertGetCharReturns(']', ctrlKey(']'));
		assertGetCharReturns('a', ctrlKey('a'));
		assertGetCharReturns('A', key('A'));
		assertGetCharReturns('a', ctrlKey('A'));
	}

	private void assertGetCharReturns(char expected, KeyStroke key) {
		assertEquals(expected, key.getCharacter());
	}

    @Test
    public void testParseKeySeq() {
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.ESC)), parseKeyStrokes("<Esc>"));
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.ESC)), parseKeyStrokes("<ESC>"));
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.ARROW_DOWN)), parseKeyStrokes("<ARROW_DOWN>"));
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.F1, EnumSet.of(Modifier.ALT, Modifier.CONTROL))),
                parseKeyStrokes("<C-A-F1>"));
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.F1,
                    EnumSet.of(Modifier.ALT, Modifier.CONTROL, Modifier.SHIFT, Modifier.COMMAND))),
                parseKeyStrokes("<C-A-D-S-F1>"));
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.F1,
                    EnumSet.of(Modifier.ALT, Modifier.CONTROL, Modifier.SHIFT, Modifier.COMMAND))),
                parseKeyStrokes("<D-A-C-S-F1>"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('\'')),
                parseKeyStrokes("''"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<')),
                parseKeyStrokes("'<"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('d'), new SimpleKeyStroke('d')), parseKeyStrokes("'<dd"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('i'), new SimpleKeyStroke('a'),
                new SimpleKeyStroke(SpecialKey.ESC)), parseKeyStrokes("'<ia<Esc>"));
        assertEquals(asList(new SimpleKeyStroke('<'), new SimpleKeyStroke('<')),
                parseKeyStrokes("<<"));
        assertEquals(asList(new SimpleKeyStroke('<'), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('<'), new SimpleKeyStroke('<')),
                parseKeyStrokes("<<<<"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('<'), new SimpleKeyStroke('<')),
                 parseKeyStrokes("'<<<"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('$')),
                 parseKeyStrokes("'<$"));
        assertEquals(asList(new SimpleKeyStroke('<'), new SimpleKeyStroke('>')),
                parseKeyStrokes("<>"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('>'), new SimpleKeyStroke('>')),
                 parseKeyStrokes("'<>>"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('<'), new SimpleKeyStroke('j')),
                 parseKeyStrokes("'<<j"));
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('>'), new SimpleKeyStroke('j')),
                 parseKeyStrokes("'<>j"));
        // Not that you'd really want this as a mapping, but we'll test it anyway.
        assertEquals(asList(new SimpleKeyStroke('\''), new SimpleKeyStroke('<'),
                new SimpleKeyStroke('<')),
                 parseKeyStrokes("'<<"));
        assertEquals(asList(new SimpleKeyStroke('d'), new SimpleKeyStroke('i'),
                new SimpleKeyStroke('<')),
                 parseKeyStrokes("di<"));
        // This caught a user by surprise.
        assertEquals(asList(new SimpleKeyStroke(SpecialKey.ESC), new SimpleKeyStroke('\''),
                new SimpleKeyStroke('<'), new SimpleKeyStroke('m'), new SimpleKeyStroke('`'),
                new SimpleKeyStroke('O'), new SimpleKeyStroke(SpecialKey.ESC),
                new SimpleKeyStroke('`'), new SimpleKeyStroke('`'), new SimpleKeyStroke('g'),
                new SimpleKeyStroke('v')),
                parseKeyStrokes("<ESC>'<m`O<Esc>``gv"));
        // Make sure all special characters are handled.
        assertEquals(asList(new SimpleKeyStroke(']', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-]>"));
        assertEquals(asList(new SimpleKeyStroke('[', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-[>"));
        assertEquals(asList(new SimpleKeyStroke('@', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-@>"));
        assertEquals(asList(new SimpleKeyStroke('\\', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-\\>"));
        assertEquals(asList(new SimpleKeyStroke('^', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-^>"));
        assertEquals(asList(new SimpleKeyStroke('_', EnumSet.of(Modifier.CONTROL))),
                parseKeyStrokes("<C-_>"));
        // Test what happens in case of missing / mismatching parentheses.
        assertEquals(Collections.emptyList(),
                parseKeyStrokes("<Plug>vrapper.window.moveUp"));
        assertEquals(Collections.emptyList(),
                parseKeyStrokes("<Plug>(vrapper.window.moveUp"));
        assertEquals(Collections.emptyList(),
                parseKeyStrokes("<Plug>vrapper.window.moveUp)"));
        // Test that plug can be followed by other keys
        assertEquals(asList(new PlugKeyStroke("(vrapper.window.moveUp)"), new SimpleKeyStroke('G')),
                parseKeyStrokes("<Plug>(vrapper.window.moveUp)G"));
        assertEquals(asList(new SimpleKeyStroke('g'), new SimpleKeyStroke('g'),
                new PlugKeyStroke("(vrapper.window.moveUp)"), new SimpleKeyStroke('G')),
                parseKeyStrokes("gg<Plug>(vrapper.window.moveUp)G"));
        // Test lower- and upper-case.
        assertEquals(asList(new SimpleKeyStroke('g'), new SimpleKeyStroke('g'),
                new PlugKeyStroke("(vrapper.window.moveUp)"), new SimpleKeyStroke('G')),
                parseKeyStrokes("gg<plug>(vrapper.window.moveUp)G"));
        assertEquals(asList(new SimpleKeyStroke('g'), new SimpleKeyStroke('g'),
                new PlugKeyStroke("(vrapper.window.moveUp)"), new SimpleKeyStroke('G')),
                parseKeyStrokes("gg<PLUG>(vrapper.window.moveUp)G"));
    }
}
