package net.sourceforge.vrapper.core.tests.cases;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.testutil.VimTestCase;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SwitchBufferCommand;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Test case for {@link SwitchBufferCommand}.
 *
 */
public class SwitchBufferCommandTests extends VimTestCase {

    @Before
    public void activateNormalMode() {
        adaptor.changeModeSafely(NormalMode.NAME);
    }

    /**
     * Test that ":buffer 2" switches to buffer with ID 2
     * 
     * @throws CommandExecutionException
     */
    @Test
    public void testSwitchNumber() throws CommandExecutionException {
        final int expectedBufferId = 2;
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getId()).thenReturn(1);
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getId()).thenReturn(2);
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(String.valueOf(expectedBufferId));
        cmd.execute(adaptor, expectedBufferId);
        
        verify(bufferAndTabsService).switchBuffer(buffer2);
    }

    /**
     * Test that ":buffer fer2" switches to the buffer whose name is "buffer2"
     * 
     * @throws CommandExecutionException
     */
    @Test
    public void testSwitchPattern() throws CommandExecutionException {        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand("fer2");
        cmd.execute(adaptor);
        
        verify(bufferAndTabsService).switchBuffer(buffer2);
    }

    /**
     * Test that ":buffer folder/buffer2" switches to the buffer whose name is "folder/buffer2"
     * 
     * @throws CommandExecutionException
     */
    @Test
    public void testSwitchPatternSlash() throws CommandExecutionException {        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("folder/buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand("older/buf");
        cmd.execute(adaptor);
        
        verify(bufferAndTabsService).switchBuffer(buffer2);
    }

    /**
     * Test that ":buffer ^buffer2$" switches to the buffer whose name is "buffer2"
     * 
     * @throws CommandExecutionException
     */
    @Test
    public void testSwitchPatternCircumflexDollar() throws CommandExecutionException {        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand("^buffer2$");
        cmd.execute(adaptor);
        
        verify(bufferAndTabsService).switchBuffer(buffer2);
    }

    /** Test that ":buffer ^ffer2" does not switch to the buffer whose name is "buffer2" */
    @Test
    public void testSwitchPatternKoCircumflex() {
        final String input = "^ffer2";
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(input);

        try {
            cmd.execute(adaptor);
            fail("Exception expected");
        } catch (final CommandExecutionException ex) {
            assertEquals("No matching buffer for " + input, ex.getMessage());
        }
    }

    /** Test that ":buffer buff$" does not switch to the buffer whose name is "buffer2" */
    @Test
    public void testSwitchPatternKoDollar() {
        final String input = "buff$";
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(input);

        try {
            cmd.execute(adaptor);
            fail("Exception expected");
        } catch (final CommandExecutionException ex) {
            assertEquals("No matching buffer for " + input, ex.getMessage());
        }
    }

    /** Test that buffer is not found if using bad case */
    @Test
    public void testSwitchPatternKoCaseSensitive() {
        final String input = "FeR2";
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(input);
        
        try {
            cmd.execute(adaptor);
            fail("Exception expected");
        } catch (final CommandExecutionException ex) {
            assertEquals("No matching buffer for " + input, ex.getMessage());
        }
    }

    /** Test that buffer is found if bad case and case insensitivity is set 
     * @throws CommandExecutionException */
    @Test
    public void testSwitchPatternOkCaseInsensitive() throws CommandExecutionException {
        final String input = "FeR2";

        when(configuration.get(Options.FILE_IGNORE_CASE)).thenReturn(true);
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(input);
        cmd.execute(adaptor);
        
        verify(bufferAndTabsService).switchBuffer(buffer2);
    }

    /** Test that an exception is raised if there are multiple matches */
    @Test
    public void testSwitchPatternKoMultipleMatch() {
        final String input = "ffer";
        
        final Buffer buffer1 = mock(Buffer.class);
        when(buffer1.getDisplayName()).thenReturn("buffer1");
        
        final Buffer buffer2 = mock(Buffer.class);
        when(buffer2.getDisplayName()).thenReturn("buffer2");
        
        when(bufferAndTabsService.getBuffers()).thenReturn(Arrays.asList(buffer1, buffer2));
        
        final SwitchBufferCommand cmd = new SwitchBufferCommand(input);
        
        try {
            cmd.execute(adaptor);
            fail("Exception expected");
        } catch (final CommandExecutionException exception) {
            assertEquals("More than one match for " + input, exception.getMessage());
        }
    }
}