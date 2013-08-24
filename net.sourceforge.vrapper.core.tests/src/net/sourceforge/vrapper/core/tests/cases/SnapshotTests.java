package net.sourceforge.vrapper.core.tests.cases;

import java.io.IOException;
import java.util.HashMap;

import net.sourceforge.vrapper.core.tests.utils.SnapshotTestsExecutor;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

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
        // we need no mock magic for register manager
        registerManager = new DefaultRegisterManager();
        //let UIInterface mock print out error messages
        Mockito.doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                for (Object argument : invocation.getArguments()) {
                    System.err.println(argument);
                }
                return null;
            }
        }).when(userInterfaceService).setErrorMessage(Mockito.anyString());
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

}
