package net.sourceforge.vrapper.core.tests.utils;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;


public class CommandTestCase extends VimTestCase {

	protected static final char EOF = Character.MIN_VALUE;

	private void checkCommand(final Command command, final boolean changesContent,
			final String beforeCursor1, final char atCursor1, final String afterCursor1,
			final String beforeCursor2, final char atCursor2, final String afterCursor2) {

		final int initalOffset = beforeCursor1.length();
		
		final String cursorStr1 = cursorStr(atCursor1);
		final String cursorStr2 = cursorStr(atCursor2);
		
		final String initialContent = beforeCursor1 + cursorStr1 + afterCursor1;
		final String expectedFinalContent = beforeCursor2 + cursorStr2 + afterCursor2;
		if (!changesContent)
			assertEquals("Test shouldn't expect motion to change content", initialContent, expectedFinalContent);

		content.setText(initialContent);
		cursorAndSelection.setPosition(new DumbPosition(initalOffset), StickyColumnPolicy.RESET_EOL);
		
		executeCommand(command);
		
		final String initialLine = formatLine(beforeCursor1, cursorStr1, afterCursor1);
        assertCommandResult(initialLine, beforeCursor2, atCursor2, afterCursor2);
	}

    private static String cursorStr(final char chr) {
        return chr != EOF ? Character.toString(chr) : "";
    }

	public void assertCommandResult(final String initialLine,
            final String beforeCursor, final char atCursor, final String afterCursor) {
		final String cursor = cursorStr(atCursor);
		final int expectedFinalOffset = beforeCursor.length();
		final String expectedFinalContent = beforeCursor + (atCursor == 0x04 ? "" : cursor)
		        + afterCursor;
		
		final int actualFinalOffset = cursorAndSelection.getPosition().getModelOffset();
		final String actualFinalContent = content.getText();

		final String expectedLine = formatLine(beforeCursor, cursor, afterCursor);
		final String actualLine = formatLine(actualFinalContent, actualFinalOffset, actualFinalOffset + 1);

		final String msg = String.format("STARTING FROM:\n%s\nEXPECTED:\n%s\nGOT:\n%s\n", initialLine, expectedLine, actualLine);
		assertEquals(msg,  expectedFinalContent, actualFinalContent);
		assertEquals(msg, expectedFinalOffset, actualFinalOffset);
    }

    protected static String formatLine(final String line, final int from, final int to) {
		if (from < line.length() && to <= line.length())
			return formatLine(line.substring(0, from), line.substring(from, to), line.substring(to));
		return line + "[EOF]";
	}

	protected static String formatLine(final String... block) {
	    final StringBuilder buf = new StringBuilder();
	    for (int i=0; i < block.length; i += 3) {
	        final String beforeCursor= block[i];
	        final String atCursor    = block[i+1];
	        final String afterCursor = block[i+2];
    		final String wholeLine = join(beforeCursor, atCursor, afterCursor);
    		if (beforeCursor.length() < wholeLine.length())
    			buf.append(String.format("%s[%s]%s", beforeCursor, atCursor, afterCursor));
    		else
    			buf.append(wholeLine + "[EOF]"); // FIXME ?
    		
    		if (i + 3 < block.length)
    		    buf.append('\n');
	    }
	    
	    return "'" + buf.toString() + "'";
	}

	protected static String join(final String...strings) {
	    return join(0, strings.length, strings);
	}
	protected static String join(final int start, final int end, final String...strings) {
	    final StringBuilder buf = new StringBuilder();
	    for (int i=start; i < end; i++) {
	        buf.append(strings[i]);
	    }
        return buf.toString();
    }

    public void checkCommand(final Command command,
			final String beforeCursor1, final char atCursor1, final String afterCursor1,
			final String beforeCursor2, final char atCursor2, final String afterCursor2) {
		checkCommand(command, true, beforeCursor1, atCursor1, afterCursor1, beforeCursor2, atCursor2, afterCursor2);
	}

	public void checkMotion(final Motion motion,
	        final String beforeCursor1, final char atCursor1, final String afterCursor1,
	        final String beforeCursor2, final char atCursor2, final String afterCursor2) {
	    final Command command = GoThereState.motion2command(motion);
	    checkCommand(command, false, beforeCursor1, atCursor1, afterCursor1, beforeCursor2, atCursor2, afterCursor2);
	}

	public void checkMotion(final Motion motion, final int count,
			final String beforeCursor1, final char atCursor1, final String afterCursor1,
			final String beforeCursor2, final char atCursor2, final String afterCursor2) {
		final Command command = GoThereState.motion2command(motion).withCount(count);
		checkCommand(command, false, beforeCursor1, atCursor1, afterCursor1, beforeCursor2, atCursor2, afterCursor2);
	}

	public Command forKeySeq(final String keyNames) {
	    return forKeySeq(parseKeyStrokes(keyNames));
	}

	public Command forKeySeq(final Iterable<KeyStroke> keySeq) {
		return new CountIgnoringNonRepeatableCommand() {
			@Override
            public void execute(final EditorAdaptor editorAdaptor) {
				assertSame(adaptor, editorAdaptor);
				type(keySeq);
			}
		};
	}

    protected void assertYanked(final ContentType type, final String text) {
        assertEquals(type, defaultRegister.getContent().getPayloadType());
        assertEquals(text, defaultRegister.getContent().getText());
    }

    protected void executeCommand(final Command command) {
        try {
            command.execute(adaptor);
        } catch (final CommandExecutionException e) {
            fail("exception during command execution: " + e.getMessage());
        }
    }

}
