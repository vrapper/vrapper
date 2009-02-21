package de.jroene.vrapper.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;
import de.jroene.vrapper.vim.VimConstants;

public class SnapshotTestCase extends VimTestCase {

    private static final String SNAPSHOT_DIRECTORY = "src/test-resources/snapshots/";
    private final String textName;
    private final String testSetName;

    public SnapshotTestCase(String textName, String testSetName) {
        super();
        this.testSetName = testSetName;
        this.textName = textName;
    }

    public void testSnapshot() throws IOException {
        File start = getStart();
        String startState = readFile(start);
        platform.setBuffer(startState);
        String lastNumber = "0";
        File[] states = getStates();
        assertTrue(states.length > 0);
        for (File snapshot : states) {
            String s = snapshot.getName();
            String number = s.substring(0, 1);
            String command = s.substring(1);
            String state = readFile(snapshot);
            assertTransition(number, lastNumber, command, state);
            lastNumber = number;
        }
    }

    private void assertTransition(String number, String lastNumber,
            String command, String state) {
        type(command);
        TestCase.assertEquals(lastNumber + "->" + number, state,
                platform.getBuffer());
    }

    private String readFile(File start) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(start));
        String line;
        StringBuilder sb = new StringBuilder();
        while((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append(VimConstants.NEWLINE);
        }
        reader.close();
        return sb.toString();
    }

    private File getStart() {
        return new File(SNAPSHOT_DIRECTORY+"common/"+textName);
    }

    private File[] getStates() {
        File baseDir = new File(SNAPSHOT_DIRECTORY, testSetName);
        File[] listFiles = baseDir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return !pathname.isDirectory();
            }
        });
        Arrays.sort(listFiles);
        return listFiles;
    }
}
