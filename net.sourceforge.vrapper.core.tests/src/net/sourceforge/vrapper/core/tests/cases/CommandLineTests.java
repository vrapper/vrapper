package net.sourceforge.vrapper.core.tests.cases;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import net.sourceforge.vrapper.core.tests.utils.DumbPosition;
import net.sourceforge.vrapper.core.tests.utils.TestSearchService;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DummyTextObject;
import net.sourceforge.vrapper.vim.commands.LineRangeOperationCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.RetabOperation;
import net.sourceforge.vrapper.vim.commands.SimpleTextOperation;
import net.sourceforge.vrapper.vim.commands.SortOperation;
import net.sourceforge.vrapper.vim.commands.SubstitutionOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineMode;
import net.sourceforge.vrapper.vim.modes.commandline.CommandLineParser;
import net.sourceforge.vrapper.vim.modes.commandline.ComplexOptionEvaluator;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.junit.Test;

public class CommandLineTests extends VimTestCase {

    ComplexOptionEvaluator ev = new ComplexOptionEvaluator();

    @Test
    public void testComplexOptionEvaluator() {
        assertSetOption(Options.SELECTION, "blbllb", Options.SELECTION.getLegalValues().toArray(new String[0]));
        assertSetOption(Options.SCROLL_JUMP, "bbshh", 1, 0, -20);
        assertSetOption(Options.SCROLL_OFFSET, "bbshh", 1, 0, -20);

        // Test string-set option
        Set<String> actual;
        actual = doSetOption(Options.CLIPBOARD, "clipboard",
                RegisterManager.CLIPBOARD_VALUE_AUTOSELECT);
        assertThat(actual, hasItems(RegisterManager.CLIPBOARD_VALUE_AUTOSELECT));

        actual = doSetOption(Options.CLIPBOARD, "clipboard", "");
        assertTrue("Setting string-set option to empty string didn't clear it!", actual.isEmpty());

        actual = doSetOption(Options.CLIPBOARD, "clipboard",
                RegisterManager.CLIPBOARD_VALUE_AUTOSELECT + ','
                    + RegisterManager.CLIPBOARD_VALUE_UNNAMED);
        assertThat(actual, hasItems(RegisterManager.CLIPBOARD_VALUE_AUTOSELECT,
                RegisterManager.CLIPBOARD_VALUE_UNNAMED));
    }

    @Test
    public void testSubstitution() throws CommandExecutionException {
        registerManager = new DefaultRegisterManager();
        when(platform.getSearchAndReplaceService()).thenReturn(new TestSearchService(content, configuration));
        reloadEditorAdaptor();
        DummyTextObject defaultRange = new DummyTextObject(null);

        registerManager.setSearch(new Search("two", false, false, false));

        content.setText("one Two three two but two and Two");
        makeSubstitution("s/one/four/Ig").execute(adaptor, 0, defaultRange);
        assertEquals("four Two three two but two and Two", content.getText());

        content.setText("one Two three two but two and Two");
        makeSubstitution("s!one!four!Ig").execute(adaptor, 0, defaultRange);
        assertEquals("four Two three two but two and Two", content.getText());

        content.setText("one Two three two but two and Two");
        // Replace with contents of (search) register
        makeSubstitution("s!one!\\=@/!Ig").execute(adaptor, 0, defaultRange);
        assertEquals("two Two three two but two and Two", content.getText());

        registerManager.setSearch(new Search("two", false, false, false));

        content.setText("one Two three two");
        // Do last search again, replace with empty string, minding case.
        makeSubstitution("s///I").execute(adaptor, 0, defaultRange);
        assertEquals("one Two three ", content.getText());

        content.setText("one Two three two");
        // Do last search again, replace with empty string, without minding case.
        makeSubstitution("s///ig").execute(adaptor, 0, defaultRange);
        assertEquals("one  three ", content.getText());

        content.setText("one Two three two but two and Two");
        // Do last search again, replace with empty string, without minding case.
        makeSubstitution("s///Ig").execute(adaptor, 0, defaultRange);
        assertEquals("one Two three  but  and Two", content.getText());

        registerManager.setSearch(new Search("Two", false, false, false));

        content.setText("one Two three two");
        // Special case: do last search again, replace with empty string
        makeSubstitution("s//").execute(adaptor, 0, defaultRange);
        assertEquals("one  three two", content.getText());
    }
    
    private SubstitutionOperation makeSubstitution(String command) {
        SubstitutionDefinition definition = new SubstitutionDefinition(command, registerManager);
        return new SubstitutionOperation(definition);
    }

    @Test
    public void testCommandLineParser() {
    	CommandLineMode commandLineMode = new CommandLineMode(adaptor);
    	CommandLineParser parser = commandLineMode.createParser();
    	Command command;
    	
    	command = parser.parseAndExecute(":", "set nohlsearch");
    	assertNotNull(command);
    	assertTrue(command instanceof CommandLineParser.RunEvaluatorCommand);
    	
    	command = parser.parseAndExecute(":", "w");
    	assertNotNull(command);
    	assertTrue(command instanceof CommandLineParser.RunEvaluatorCommand);
    	
    	command = parser.parseAndExecute(":", "2");
    	assertNotNull(command);
    	assertTrue(command instanceof MotionCommand);
    	
    	command = parser.parseAndExecute(":", "99999");
    	assertNotNull(command);
    	assertTrue(command instanceof MotionCommand);
    	
    	command = parser.parseAndExecute(":", "s/foo/bar/");
    	assertNotNull(command);
    	assertTrue(command instanceof TextOperationTextObjectCommand);
    	
    	command = parser.parseAndExecute(":", "%s/foo/bar/");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "2,3d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "'a,'bd");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "-1,-2d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "+1,+2d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", ",5d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", ".,$d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "/foo/,/bar/d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    	
    	command = parser.parseAndExecute(":", "?foo?,/bar/d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "m +1");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "m+2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "mov +2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "mo+2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "move+1");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "copy +1");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "copy+2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "co +1");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "co+2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "t +1");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "t+2");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "d");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "delete");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "d A");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "y");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "y A");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);

    	command = parser.parseAndExecute(":", "yank A");
    	assertNotNull(command);
    	assertTrue(command instanceof LineRangeOperationCommand);
    }
    
    @Test
    public void testRetab() throws CommandExecutionException {
    	SimpleTextOperation retabCommand = (SimpleTextOperation) new RetabOperation(null);
    
    	when(configuration.get(Options.EXPAND_TAB)).thenReturn(true);
    	when(configuration.get(Options.TAB_STOP)).thenReturn(4);
    	
    	content.setText("\t");
    	retabCommand.execute(adaptor, null, ContentType.LINES);
    	assertEquals("    ", content.getText());
    	
    	content.setText("line\n\t\t\tnew\tline\n\t\tABC");
    	retabCommand.execute(adaptor, null, ContentType.LINES);
    	assertEquals("line\n            new    line\n        ABC", content.getText());
    	
    	content.setText("\t\t\t\n\t\n\n\t\t\t\n\t");
    	retabCommand.execute(adaptor, null, ContentType.LINES);
    	assertEquals("            \n    \n\n            \n    ", content.getText());
    }
    
    @Test
    public void testSort() throws CommandExecutionException {
    	content.setText("single line");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("single line", content.getText());
    	
    	content.setText("3\n2\n1\n10");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("1\n10\n2\n3", content.getText());
    	
    	new SortOperation("!").execute(adaptor, null, ContentType.LINES);
    	assertEquals("3\n2\n10\n1", content.getText());
    	
    	new SortOperation("n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("1\n2\n3\n10", content.getText());
    	
    	new SortOperation("!n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("10\n3\n2\n1", content.getText());
    	
    	content.setText("3\n-2\n1\n10");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("-2\n1\n10\n3", content.getText());
    	
    	content.setText("3\n-2\n1\n10");
    	new SortOperation("n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("-2\n1\n3\n10", content.getText());
    	
    	content.setText("c\n3\n2\nb\n10\n1\na");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("1\n10\n2\n3\na\nb\nc", content.getText());
    	
    	content.setText("c\n3\n2\nb\n10\n1\na");
    	new SortOperation("n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("c\nb\na\n1\n2\n3\n10", content.getText());
    	
    	content.setText("c\n3\n2\nb\n10\n1\na");
    	new SortOperation("n!").execute(adaptor, null, ContentType.LINES);
    	assertEquals("10\n3\n2\n1\na\nb\nc", content.getText());
    	
    	content.setText("c\n3\n2\nb\n10\n1\na");
    	new SortOperation("x").execute(adaptor, null, ContentType.LINES);
    	assertEquals("1\n2\n3\na\nb\nc\n10", content.getText());
    	
    	content.setText("c\n3\n2\nb\nxx\n10\n1\na");
    	new SortOperation("x").execute(adaptor, null, ContentType.LINES);
    	assertEquals("xx\n1\n2\n3\na\nb\nc\n10", content.getText());
    	
    	content.setText("bb3\naa4\ncc2\ndd1");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("aa4\nbb3\ncc2\ndd1", content.getText());
    	
    	new SortOperation("n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("dd1\ncc2\nbb3\naa4", content.getText());
    	
    	new SortOperation("x").execute(adaptor, null, ContentType.LINES);
    	assertEquals("aa4\nbb3\ncc2\ndd1", content.getText());
    	
    	content.setText("a\na\na\na\nb\nb\nc");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("a\na\na\na\nb\nb\nc", content.getText());
    	
    	new SortOperation("u").execute(adaptor, null, ContentType.LINES);
    	assertEquals("a\nb\nc", content.getText());
    	
    	content.setText("a\na\nA\nA\nb\nb\nc");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("A\nA\na\na\nb\nb\nc", content.getText());
    	
    	content.setText("d\nA\nC\nb\nE");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("A\nC\nE\nb\nd", content.getText());
    	
    	content.setText("d\nA\nC\nb\nE");
    	new SortOperation("i").execute(adaptor, null, ContentType.LINES);
    	assertEquals("A\nb\nC\nd\nE", content.getText());
    	
    	content.setText("a\na\nA\nA\nb\nb\nc");
    	new SortOperation("i").execute(adaptor, null, ContentType.LINES);
    	assertEquals("a\na\nA\nA\nb\nb\nc", content.getText());
    	
    	new SortOperation("u").execute(adaptor, null, ContentType.LINES);
    	assertEquals("A\na\nb\nc", content.getText());
    	
    	content.setText("a\na\nA\nA\nb\nb\nc");
    	new SortOperation("ui").execute(adaptor, null, ContentType.LINES);
    	assertEquals("a\nb\nc", content.getText());
    	
    	content.setText("A\nA\na\na\nb\nb\nc");
    	new SortOperation("ui").execute(adaptor, null, ContentType.LINES);
    	assertEquals("A\nb\nc", content.getText());
    	
    	content.setText("1xx5\n3xx3\n2xx4\n4xx2");
    	new SortOperation("n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("1xx5\n2xx4\n3xx3\n4xx2", content.getText());
    	
    	content.setText("1xx5\n3xx3\n2xx4\n4xx2");
    	new SortOperation("/xx/ n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("4xx2\n3xx3\n2xx4\n1xx5", content.getText());
    	
    	content.setText("1xx5\n3xx3\n2xx4\n4xx2\naxxb");
    	new SortOperation("/xx/").execute(adaptor, null, ContentType.LINES);
    	assertEquals("4xx2\n3xx3\n2xx4\n1xx5\naxxb", content.getText());
    	
    	content.setText("1xx5\n3xx3\n2xx4\n4xx2\naxxa");
    	new SortOperation("/xx/ n").execute(adaptor, null, ContentType.LINES);
    	assertEquals("axxa\n4xx2\n3xx3\n2xx4\n1xx5", content.getText());
    	
    	content.setText("1xx5\n3xx3\n2xx4\n4xx2\naxxb\nfoo");
    	new SortOperation("/xx/").execute(adaptor, null, ContentType.LINES);
    	assertEquals("foo\n4xx2\n3xx3\n2xx4\n1xx5\naxxb", content.getText());
    	
    	content.setText("3\n2\n-1\n1\n0");
    	new SortOperation("").execute(adaptor, null, ContentType.LINES);
    	assertEquals("-1\n0\n1\n2\n3", content.getText());
    }
   
    @Test
    public void testAscii() throws CommandExecutionException {
        /*
         TODO: Trying to get the last set values of the UserInterfaceService for testing
               but values are always null. Could use some help with this. -- BRD
    	content.setText("");
    	AsciiCommand.INSTANCE.execute(adaptor);
    	// Should be NUL
    	String expected = null;
    	String actual = userInterfaceService.getLastInfoValue();
    	assertEquals(expected, actual);
    	
    	content.setText("r");
    	AsciiCommand.INSTANCE.execute(adaptor);
    	expected = "<r>  114,  Hex 72,  Octal 162";
    	actual = userInterfaceService.getLastInfoValue();
    	assertEquals(expected, actual);
    	*/
    }
    
    
    @Test
    public void testSortRange() throws CommandExecutionException {
    	content.setText("one\ntwo\nthree");
    	Position startPos = new DumbPosition(4);
    	Position stopPos = new DumbPosition(6);
    	TextRange range = new LineWiseSelection(adaptor, startPos, stopPos);
    	
    	new SortOperation("").execute(adaptor, range, ContentType.LINES);
    	assertEquals("one\ntwo\nthree", content.getText());
    	
    	content.setText("3\n2\n1\n10");
    	startPos = new DumbPosition(0);
    	stopPos = new DumbPosition(6);
    	range = new LineWiseSelection(adaptor, startPos, stopPos);
    	
    	new SortOperation("").execute(adaptor, range, ContentType.LINES);
    	assertEquals("1\n10\n2\n3", content.getText());
    	
    	content.setText("3\n2\n1\n10");
    	startPos = new DumbPosition(2);
    	stopPos = new DumbPosition(5);
    	range = new LineWiseSelection(adaptor, startPos, stopPos);
    	
    	new SortOperation("").execute(adaptor, range, ContentType.LINES);
    	assertEquals("3\n1\n2\n10", content.getText());
    	
    	content.setText("3\n2\n10\n1");
    	startPos = new DumbPosition(2);
    	stopPos = new DumbPosition(8);
    	range = new LineWiseSelection(adaptor, startPos, stopPos);
    	
    	new SortOperation("").execute(adaptor, range, ContentType.LINES);
    	assertEquals("3\n1\n10\n2", content.getText());
    	
    	content.setText("a\nb\nc\n3\n2\n10\n1");
    	startPos = new DumbPosition(9);
    	stopPos = new DumbPosition(14);
    	range = new LineWiseSelection(adaptor, startPos, stopPos);
    	
    	new SortOperation("n").execute(adaptor, range, ContentType.LINES);
    	assertEquals("a\nb\nc\n3\n1\n2\n10", content.getText());
    }

    @Test
    public void test_CtrlC_exits() {
    	adaptor.changeModeSafely(CommandLineMode.NAME);
    	adaptor.getConfiguration().set(Options.IGNORE_CASE, false);
    	type(parseKeyStrokes("set ic<C-c>"));
    	assertFalse(adaptor.getConfiguration().get(Options.IGNORE_CASE));
    	assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    /**
     * Checks that return on an empty command line exits.
     */
    @Test
    public void test_Return_exits() {
    	adaptor.changeModeSafely(CommandLineMode.NAME);
    	adaptor.getConfiguration().set(Options.IGNORE_CASE, false);
    	type(parseKeyStrokes("<RETURN>"));
    	assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    @Test
    public void test_Backspace_exits() {
        adaptor.changeModeSafely(CommandLineMode.NAME);
        adaptor.getConfiguration().set(Options.IGNORE_CASE, false);
        type(parseKeyStrokes("set<BS><BS><BS>"));
        assertEquals(CommandLineMode.NAME, adaptor.getCurrentModeName());
        type(parseKeyStrokes("<BS>"));
        assertEquals(NormalMode.NAME, adaptor.getCurrentModeName());
    }

    private <T> void assertSetOption(Option<T> o, String invalid, T... values) {

        for (String name : o.getAllNames()) {
            for (T value : values) {
                T actual = doSetOption(o, name, value.toString());
                assertEquals(value, actual);
            }
            T value = adaptor.getConfiguration().get(o);
            if (invalid != null) {
                T actual = doSetOption(o, name, invalid);
                assertEquals(value, actual);
            }
        }
    }

    private <T> T doSetOption(Option<T> o, String name, String setValue) {
        Queue<String> cmd = new LinkedList<String>();
        cmd.add(name + "=" + setValue.toString());
        ev.evaluate(adaptor, cmd);
        return adaptor.getConfiguration().get(o);
    }
}
