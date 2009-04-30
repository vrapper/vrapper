package net.sourceforge.vrapper.core.tests.utils;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import junit.framework.Assert;

public class SnapshotTestsExecutor {

    private static final String SNAPSHOT_DIRECTORY = "test-resources/snapshots/";
    private final VimTestCase vimTestCase;

    public SnapshotTestsExecutor(VimTestCase vimTestCase) {
        this.vimTestCase = vimTestCase;
    }

    public void execute(String textName, String testSetName, String specialchar) throws IOException {
        File start = getStart(textName);
        String startState = readFile(start);
        vimTestCase.setBuffer(startState);
        String lastNumber = "0";
        File[] states = getStates(testSetName);
        assertTrue(states.length > 0);
        for (File snapshot : states) {
            String s = snapshot.getName();
            String number = s.substring(0, 1);
            String command = s.substring(1);
            String expectedState = readFile(snapshot);
            assertTransition(number, lastNumber, command, expectedState, specialchar);
            lastNumber = number;
        }
    }

    private void assertTransition(String number, String lastNumber, String command, String expectedState, String specialChar) {
        if (specialChar != null) {
            command = command.replace("_", specialChar);
        }
        vimTestCase.type(parseKeyStrokes(command));
        Assert.assertEquals(lastNumber + "->" + number, expectedState, vimTestCase.getBuffer());
    }

    private String readFile(File start) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(start));
        String line;
        StringBuilder sb = new StringBuilder();
        String newline = "\n"; // FIXME: vim.getVariables().getNewLine();
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

    private File getStart(String textName) {
        return new File(SNAPSHOT_DIRECTORY+"common/"+textName);
    }

    private File[] getStates(String testSetName) {
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
