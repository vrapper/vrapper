package de.jroene.vrapper.test;

import junit.framework.TestCase;
import de.jroene.vrapper.vim.LineInformation;

/**
 * Test for {@link TestPlatform}.
 *
 * @author Matthias Radig
 */
public class TestPlatformTest extends TestCase {

    private TestPlatform platform;

    @Override
    public void setUp() {
        platform = new TestPlatform();
    }
    public void testGetLineInformation() {
        String s = "\nAAAA\n\n";
        platform.setBuffer(s);
        LineInformation l1 = platform.getLineInformation(0);
        LineInformation l2 = platform.getLineInformation(1);
        LineInformation l3 = platform.getLineInformation(2);
        LineInformation l4 = platform.getLineInformation(3);
        assertLineInfo(l1, 0, 0, 0, 0);
        assertLineInfo(l2, 1, 1, 5, 4);
        assertLineInfo(l3, 2, 6, 6, 0);
        assertLineInfo(l4, 3, 7, 7, 0);

        l1 = platform.getLineInformation();
        platform.setPosition(1);
        l2 = platform.getLineInformation();
        platform.setPosition(6);
        l3 = platform.getLineInformation();
        platform.setPosition(7);
        l4 = platform.getLineInformation();
        assertLineInfo(l1, 0, 0, 0, 0);
        assertLineInfo(l2, 1, 1, 5, 4);
        assertLineInfo(l3, 2, 6, 6, 0);
        assertLineInfo(l4, 3, 7, 7, 0);


        l1 = platform.getLineInformationOfOffset(0);
        assertLineInfo(l1, 0, 0, 0, 0);

        l2 = platform.getLineInformationOfOffset(1);
        assertLineInfo(l2, 1, 1, 5, 4);
        l2 = platform.getLineInformationOfOffset(2);
        assertLineInfo(l2, 1, 1, 5, 4);
        l2 = platform.getLineInformationOfOffset(3);
        assertLineInfo(l2, 1, 1, 5, 4);
        l2 = platform.getLineInformationOfOffset(4);
        assertLineInfo(l2, 1, 1, 5, 4);
        l2 = platform.getLineInformationOfOffset(5);
        assertLineInfo(l2, 1, 1, 5, 4);

        l3 = platform.getLineInformationOfOffset(6);
        assertLineInfo(l3, 2, 6, 6, 0);

        l4 = platform.getLineInformationOfOffset(7);
        assertLineInfo(l4, 3, 7, 7, 0);
    }

    public void testGetText() {
        String s = "aaa\naa\n";
        platform.setBuffer(s);
        assertGetText("aaa", 0, 3);
        assertGetText("aaa\n", 0, 4);
        assertGetText(s, 0, 7);
        assertGetText("aa", 4, 2);
        assertGetText("aa", 1, 2);
        assertGetText("\naa\n", 3, 4);
    }

    public void testNumberOfLines() {
        assertEquals(1, platform.getNumberOfLines());
        String s = "\noetu\ntoeuh\notnuh\nouuu\n";
        platform.setBuffer(s);
        assertEquals(6, platform.getNumberOfLines());

    }

    public void testReplace() {
        String s = "test string\n";
        String s2 = "work string\n";
        String s3 = "work";
        String s4 = "nice work";
        String s5 = "nice work!";
        assertEquals("", platform.getBuffer());
        platform.replace(0, 0, s);
        assertEquals(s, platform.getBuffer());
        platform.replace(0, 4, "work");
        assertEquals(s2, platform.getBuffer());
        platform.replace(4, 8, "");
        assertEquals(s3, platform.getBuffer());
        platform.replace(0, 0, "nice ");
        assertEquals(s4, platform.getBuffer());
        platform.replace(9, 0, "!");
        assertEquals(s5, platform.getBuffer());
    }

    private void assertGetText(String string, int i, int length) {
        assertEquals(string, platform.getText(i, length));
    }

    private void assertLineInfo(LineInformation info, int line, int start, int end, int length) {
        assertEquals(start,  info.getBeginOffset());
        assertEquals(end,    info.getEndOffset());
        assertEquals(length, info.getLength());
        assertEquals(line,   info.getNumber());
    }
}
