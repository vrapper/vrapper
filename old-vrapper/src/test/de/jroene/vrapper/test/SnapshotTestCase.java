package de.jroene.vrapper.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

public class SnapshotTestCase extends VimTestCase {

    private static final String SNAPSHOT_DIRECTORY = "src/test-resources/snapshots/";
    private final String textName;
    private final String testSetName;
    private final String specialChar;

    public SnapshotTestCase(String textName, String testSetName, String specialchar) {
        super();
        this.testSetName = testSetName;
        this.textName = textName;
        this.specialChar = specialchar;
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
        if (specialChar != null) {
            command = command.replace("_", specialChar);
        }
        type(command);
        TestCase.assertEquals(lastNumber + "->" + number, state,
                platform.getBuffer());
    }

    private String readFile(File start) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(start));
        String line;
        StringBuilder sb = new StringBuilder();
        String newline = vim.getVariables().getNewLine();
        while((line = reader.readLine()) != null) {
            sb.append(line);
            sb.append(newline);
        }
        reader.close();
        if(sb.length() > 0) {
            sb.delete(sb.length()-newline.length(), sb.length());
        }
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
