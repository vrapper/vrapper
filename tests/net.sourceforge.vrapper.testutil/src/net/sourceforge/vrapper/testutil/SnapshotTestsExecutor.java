package net.sourceforge.vrapper.testutil;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Assert;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.VimConstants;

/**
 * Takes an input file and several snapshot files with commands in their filenames.
 * 
 * <p>
 * The names of the snapshot files are parsed to extract a number of commands.
 * These commands are run in order, once they are finished the contents of the snapshot file
 * will be compared to the contents of the test buffer.
 * 
 * <p>
 * Because the filesystem cannot handle "special" characters like &lt; or <Esc> in filenames,
 * a String replacement system is provided to replace e.g. "-" with "<ESC>".
 */
public class SnapshotTestsExecutor {

    private static final File SNAPSHOT_DIRECTORY = new File("test-resources/snapshots/");
    private final VimTestCase vimTestCase;

    public SnapshotTestsExecutor(VimTestCase vimTestCase) {
        this.vimTestCase = vimTestCase;
    }

    public void execute(String textName, String testSetName, Map<String, String> specialChars) throws IOException {
        File start = getStart(textName);
        String startState = readFile(start);
        vimTestCase.setBuffer(startState);
        String lastNumber = "0";
        File[] states = getStates(testSetName);
        assertTrue(states.length > 0);
        for (File snapshot : states) {
            String s = snapshot.getName();
            String number = s.substring(0, 1);
            if (! s.endsWith(".txt")) {
                throw new IllegalStateException("Snapshot " + number + " in testset " + testSetName
                        + " is missing .txt extension!");
            }
            //strip off .txt extension
            String command = s.substring(1, s.length() - 4);
            String expectedState = readFile(snapshot);
            assertTransition(number, lastNumber, command, expectedState, specialChars);
            lastNumber = number;
        }
    }

    private void assertTransition(String number, String lastNumber, String command, String expectedState, Map<String, String> specialChars) {
        if (specialChars != null) {
            for (Entry<String, String> entry: specialChars.entrySet())
                command = command.replace(entry.getKey(), entry.getValue());
        }
        int[] rowcol = getPosition();
        vimTestCase.type(parseKeyStrokes(command));
        String assertMsg = String.format("[%d, %d] '%s': %s -> %s", rowcol[0], rowcol[1], command, lastNumber, number);
        Assert.assertEquals(assertMsg, expectedState, vimTestCase.getBuffer());
    }

    private int[] getPosition() {
        Position position = vimTestCase.adaptor.getCursorService().getPosition();
        TextContent modelContent = vimTestCase.adaptor.getModelContent();
        String text = modelContent.getText(0, modelContent.getTextLength());
        int offset = position.getModelOffset();
        int[] rowcol = new int[]{1, 1};
        for (int i = 0; i < offset; i++) {
            if  (text.charAt(i) == '\n') {
                ++rowcol[0];
                rowcol[1] = 1;
            } else
                ++rowcol[1];
        }
        return rowcol;
    }

    /**
     * Reads an external file into a String, converting the newlines to
     *  {@link VimConstants#REGISTER_NEWLINE}
     */
    private String readFile(File start) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(start));
        String line;
        StringBuilder sb = new StringBuilder();
        String newline = VimConstants.REGISTER_NEWLINE;
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
        return new File(SNAPSHOT_DIRECTORY, "common/"+textName);
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
