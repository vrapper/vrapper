package net.sourceforge.vrapper.core.tests.cases;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import net.sourceforge.vrapper.core.tests.utils.SnapshotTestsExecutor;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Run a number of commands on a test buffer and compare the contents of the buffer with a
 * "snapshot file" at given moments.
 * 
 * @see SnapshotTestsExecutor
 */
public class SnapshotTests extends VimTestCase {

    @Override
    public void setUp() {
        super.setUp();
        Mockito.when(configuration.getNewLine()).thenReturn(VimConstants.REGISTER_NEWLINE);
        // we need no mock magic for register manager
        registerManager = new DefaultRegisterManager();
        reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
    }

    @Test public void testDelete() throws IOException {
        SnapshotTestsExecutor executor = new SnapshotTestsExecutor(this);
        executor.execute("text.txt", "Delete", null);
    }

    @Test public void testFind() throws IOException {
        SnapshotTestsExecutor executor = new SnapshotTestsExecutor(this);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("-", "<Esc>");
        executor.execute("chars.txt", "Find", map);
    }
    
    @Test public void testPut() throws IOException {
        SnapshotTestsExecutor executor = new SnapshotTestsExecutor(this);
        executor.execute("text.txt", "Put", null);
    }

    private void executeRegistersTest() throws IOException {
        SnapshotTestsExecutor executor = new SnapshotTestsExecutor(this);
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("-", "<Esc>");
        map.put("_", "\"");
        executor.execute("text.txt", "Registers", map);
    }

    @Test public void testRegistersWithLocals() throws IOException {
        adaptor.useLocalRegisters();
        executeRegistersTest();
    }

    @Test public void testRegistersWithGlobals() throws IOException {
        adaptor.useGlobalRegisters();
        executeRegistersTest();
    }

//    @Ignore
    @Test public void testRegistersNewlineConversion() throws IOException {
        registerManager.getRegister("a").setContent(
                new StringRegisterContent(ContentType.LINES, "Francis Bacon said\r\njust too many things\r\n"));
        registerManager.getRegister("b").setContent(
                new StringRegisterContent(ContentType.TEXT, "No circumferential\r\ndata available."));
        registerManager.getRegister("c").setContent(
                new StringRegisterContent(ContentType.TEXT, "The word of 1952:\r\nPotrzebie"));

        SnapshotTestsExecutor executor = new SnapshotTestsExecutor(this);
        // Must keep ordering! Otherwise the Esc mapping messes up <C-R>.
        HashMap<String, String> map = new LinkedHashMap<String, String>();
        map.put("-", "<Esc>");
        map.put("_", "\"");
        map.put("@", "<C-R>");
        executor.execute("text.txt", "EOLConversion", map);
    }
}
