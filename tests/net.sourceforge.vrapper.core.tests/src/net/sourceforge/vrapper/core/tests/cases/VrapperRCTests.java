package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Iterator;

import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.Remapping;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;

import org.junit.Test;

public class VrapperRCTests extends VimTestCase {

    @Test
    public void testVrapperRCParsing() throws Exception {
        File config = new File("test-resources/vrapperrc/sample.vrapperrc");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(config));
            String line;
            CommandLineMode commandLineMode = new CommandLineMode(adaptor);
            CommandLineParser parser = commandLineMode.createParser();
            while((line = reader.readLine()) != null) {
                Command c = parser.parseAndExecute(null, line.trim());
                if (c != null) {
                    c.execute(adaptor);
                }
            }
        } finally {
            if(reader != null) {
                    reader.close();
            }
        }
        KeyMap map = keyMapProvider.getKeyMap(NormalMode.KEYMAP_NAME);
        assertMappingEquals(map.press(key('"')).getValue(), key('1'));
        assertMappingEquals(map.press(key('\u00e9')).getValue(), key('d'), key('d'));
        assertMappingEquals(map.press(key('\u00e4')).getValue(), key('z'), key('z'));
    }

    private void assertMappingEquals(Remapping re, KeyStroke... strokes) {
        Iterator<KeyStroke> it = re.getKeyStrokes().iterator();
        for (KeyStroke s : strokes) {
            assertTrue(it.hasNext());
            assertEquals(s, it.next());
        }
        assertFalse(it.hasNext());
    }

}
