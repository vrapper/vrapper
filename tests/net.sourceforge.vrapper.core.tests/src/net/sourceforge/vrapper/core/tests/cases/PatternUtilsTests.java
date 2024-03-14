package net.sourceforge.vrapper.core.tests.cases;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import net.sourceforge.vrapper.utils.PatternUtils;

/**
 * Test cases for {@link PatternUtils}.
 *
 */
public class PatternUtilsTests {
    
    /** Test that the dot is escaped */
    @Test
    public void shellPatternToRegexDot() {
        test("file.txt", "file\\.txt");
    }

    /** Test regex pattern conversion from shell pattern's asterisks */
    @Test
    public void shellPatternToRegexAsterisk() {
        test("***f*il**e****", ".*f.*il.*e.*");
    }

    /** Test regex pattern conversion from shell pattern's question marks */
    @Test
    public void shellPatternToRegexQuestionMark() {
        test("file???", "file...");
    }
    
    /** Test that the squared brackets are NOT escaped */
    @Test
    public void shellPatternToRegexSquaredBracket() {
        test("file[]", "file[]");
    }
    
    /** Test that the braces {} alone are escaped */
    @Test
    public void shellPatternToRegexBraces() {
        test("fi{le}", "fi\\{le\\}");
    }
    
    /** Test that the commas are NOT escaped */
    @Test
    public void shellPatternToRegexComma() {
        test("fi,le", "fi,le");
    }
    
    /** Test that the slashes are NOT escaped */
    @Test
    public void shellPatternToRegexSlashes() {
        test("folder/file", "folder/file");
    }
    
    /** Test that the backslashes are escaped */
    @Test
    public void shellPatternToRegexBacklashes() {
        test("folder\\file", "folder\\\\file");
    }
    
    /** Test that characters of start/end of line are NOT escaped */
    @Test
    public void shellPatternToRegexStartEnd() {
        test("^file$", "^file$");
    }
    
    /** Test that the pipes characters are escaped */
    @Test
    public void shellPatternToRegexPipe() {
        test("file|", "file\\|");
    }
    
    /**
     * Assert that a converted shell pattern is equals to an expected output 
     * 
     * @param inputShellPattern The shell pattern as input of the test method
     * @param expectedRegexPattern The expected regex pattern as output
     */
    private void test(String inputShellPattern, String expectedRegexPattern) {
        final String actualRegexPattern = PatternUtils.shellPatternToRegexString(inputShellPattern);
        
        assertEquals(expectedRegexPattern, actualRegexPattern);
        
    }
}