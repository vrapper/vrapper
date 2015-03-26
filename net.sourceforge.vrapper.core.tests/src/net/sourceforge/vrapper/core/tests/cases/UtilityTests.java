package net.sourceforge.vrapper.core.tests.cases;

import net.sourceforge.vrapper.utils.StringUtils;

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
}
