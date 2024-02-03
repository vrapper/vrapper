package net.sourceforge.vrapper.core.tests.cases;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.utils.ExplodedPattern;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.StringUtils.PatternHolder;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.Assert;
import org.junit.Test;

public class UtilityTests {

    @Test
    public void testCalculateVisualOffsets() {
        String test;
        int[] result;
        final int TABSTOP = 8;

        // Rendered as "         aha                      patterna baz4"
        test = "\t aha\t\t    \t  patterna baz4";
        result = StringUtils.calculateVisualOffsets(test, test.length(), TABSTOP);
        Assert.assertArrayEquals(
                new int[] {
                        0,
                        /* space and chars */ 8, 9, 10, 11,
                        /* tabs and spaces */ 12, 16, 24, 25, 26, 27, 28, 32, 33,
                        /* text */ 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47}, result);

        // First tab shrinks to just 1 character width
        // Rendered as "1234567  aha                      patterna baz4"
        test = "1234567\t aha\t\t    \t  patterna baz4";
        result = StringUtils.calculateVisualOffsets(test, test.length(), TABSTOP);
        Assert.assertArrayEquals(
                new int[] {
                        0, 1, 2, 3, 4, 5, 6, 7,
                        /* space and chars */ 8, 9, 10, 11,
                        /* tabs and spaces */ 12, 16, 24, 25, 26, 27, 28, 32, 33,
                        /* text */ 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47}, result);

        // First tab suddenly shifts everything with 8 character widths
        // Rendered as "12345678         aha                      patterna baz4"
        test = "12345678\t aha\t\t    \t  patterna baz4";
        result = StringUtils.calculateVisualOffsets(test, test.length(), TABSTOP);
        Assert.assertArrayEquals(
                new int[] {
                        0, 1, 2, 3, 4, 5, 6, 7, 8,
                        /* space and chars */ 16, 17, 18, 19,
                        /* tabs and spaces */ 20, 24, 32, 33, 34, 35, 36, 40, 41,
                        /* text */ 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55}, result);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testExplodePattern() throws Exception {
        // Get access to internal parts list
        Field contentsRef = ExplodedPattern.class.getDeclaredField("contents");
        contentsRef.setAccessible(true);

        String test = "I want to \\cSEE this split";
        ExplodedPattern result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.contains("\\c"));
        List<String> contents = (List<String>) contentsRef.get(result);
        List<String> contentsExpected = Arrays.asList("I"," ","w","a","n","t"," ","t","o"," ","\\c",
                "S","E","E"," ","t","h","i","s"," ","s","p","l","i","t");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));

        // Verify that surrogate characters are kept together.
        test = "testing \uCE45 surrogates";
        result = StringUtils.explodePattern(test);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("t","e","s","t","i","n","g"," ","\uCE45"," ",
                "s","u","r","r","o","g","a","t","e","s");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));

        test = "test extended \\%V flag";
        result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.contains("\\%V"));
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("t","e","s","t"," ","e","x","t","e","n","d","e","d"," ",
                "\\%V"," ","f","l","a","g");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));

        test = "test backslash \\\\";
        result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.contains("\\\\"));
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("t","e","s","t"," ","b","a","c","k","s","l","a","s","h",
                " ","\\\\");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));

        test = "test backslash \\\\can't\\\\\\c";
        result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.contains("\\\\"));
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("t","e","s","t"," ","b","a","c","k","s","l","a","s","h",
                " ","\\\\", "c", "a", "n", "'", "t", "\\\\", "\\c");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));

        test = "test \\\\replace and reassembly\\c\\%v";
        result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.replace("\\%v", "\\%V"));
        Assert.assertEquals("test \\\\replace and reassembly\\c\\%V", result.toString());

        test = "test \\\\remove\\c\\%v\\c";
        result = StringUtils.explodePattern(test);
        Assert.assertTrue(result.removeAll("\\c"));
        Assert.assertEquals("test \\\\remove\\%v", result.toString());

        test = "";
        result = StringUtils.explodePattern(test);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Collections.emptyList();
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSplitIntoPatterns() throws Exception {
        // Get access to internal parts list
        Field contentsRef = ExplodedPattern.class.getDeclaredField("contents");
        contentsRef.setAccessible(true);

        String test = "#I want to \\cSEE this split#replaced#c";
        PatternHolder holder = StringUtils.splitIntoPatterns(test, 2);
        ExplodedPattern result = holder.patterns.get(0);
        Assert.assertTrue(result.contains("\\c"));
        List<String> contents = (List<String>) contentsRef.get(result);
        List<String> contentsExpected = Arrays.asList("I"," ","w","a","n","t"," ","t","o"," ","\\c",
                "S","E","E"," ","t","h","i","s"," ","s","p","l","i","t");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        result = holder.patterns.get(1);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("r","e","p","l","a","c","e","d");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        Assert.assertEquals("c", holder.remainder);

        test = "#I want to \\cSEE this split\\#now\\\\#replaced#c";
        holder = StringUtils.splitIntoPatterns(test, 2);
        result = holder.patterns.get(0);
        Assert.assertTrue(result.contains("\\c"));
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("I"," ","w","a","n","t"," ","t","o"," ","\\c",
                "S","E","E"," ","t","h","i","s"," ","s","p","l","i","t","\\#","n","o","w","\\\\");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        result = holder.patterns.get(1);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("r","e","p","l","a","c","e","d");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        Assert.assertEquals("c", holder.remainder);

        test = "##replaced#c";
        holder = StringUtils.splitIntoPatterns(test, 2);
        result = holder.patterns.get(0);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Collections.emptyList();
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        result = holder.patterns.get(1);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("r","e","p","l","a","c","e","d");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        Assert.assertEquals("c", holder.remainder);

        test = "#replaced";
        holder = StringUtils.splitIntoPatterns(test, 2);
        Assert.assertEquals(1, holder.patterns.size());
        result = holder.patterns.get(0);
        contents = (List<String>) contentsRef.get(result);
        contentsExpected = Arrays.asList("r","e","p","l","a","c","e","d");
        MatcherAssert.assertThat(contents, CoreMatchers.is(contentsExpected));
        Assert.assertEquals("", holder.remainder);

    }
}
