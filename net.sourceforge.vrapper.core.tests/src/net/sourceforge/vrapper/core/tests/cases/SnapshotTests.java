package net.sourceforge.vrapper.core.tests.cases;

import java.io.IOException;
import java.util.HashMap;

import net.sourceforge.vrapper.core.tests.utils.SnapshotTestsExecutor;
import net.sourceforge.vrapper.core.tests.utils.VimTestCase;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

import org.junit.Test;

import static org.mockito.Mockito.when;

public class SnapshotTests extends VimTestCase {


    @Override
    public void setUp() {
        super.setUp();
        // we need no mock magic for register manager
        registerManager = new DefaultRegisterManager();
        when(platform.getUserInterfaceService()).thenReturn(new UserInterfaceService() {
            public void setRecording(boolean recording) { }
            public void setEditorMode(String modeName) { }
            public void setCommandLine(String content, int position) { }
            public void setInfoMessage(String content) { }

            public void setErrorMessage(String content) {
                System.err.println(content);
            }
            public void setAsciiValues(String asciiValue, int decValue, String hexValue, String octalValue) { }
            public String getLastAsciiValue() { return null; }
            public String getCurrentEditorMode() { return null; }
            public String getLastInfoValue() { return null; }
            public String getLastErrorValue() { return null; }
            public void setAsciiSet(boolean asciiSet) { }
            public boolean isAsciiSet() { return false; }
        });
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
