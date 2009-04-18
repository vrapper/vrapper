package net.sourceforge.vrapper.core.tests.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import net.sourceforge.vrapper.keymap.vim.GoThereState;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.vim.DefaultEditorAdaptor;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.modes.EditorMode;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.junit.Before;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.MockitoAnnotations;

public class CommandTestCase {
	@Mock protected Platform platform;
	@Mock protected RegisterManager registerManager;
	protected TestTextContent content;
	protected TestCursorAndSelection cursorAndSelection;
	protected EditorAdaptor adaptor;
	protected EditorMode mode;
	protected static final char EOF = Character.MIN_VALUE;

	public void initMocks() {
		MockitoAnnotations.initMocks(this);
		content = new TestTextContent();
		cursorAndSelection = new TestCursorAndSelection();
		when(platform.getCursorService()).thenReturn(cursorAndSelection);
		when(platform.getSelectionService()).thenReturn(cursorAndSelection);
		when(platform.getModelContent()).thenReturn(content);
		when(platform.getViewContent()).thenReturn(content);
		adaptor = new DefaultEditorAdaptor(platform, registerManager);
	}

	@Before
	public void setUp() {
		initMocks();
	}

	private void checkCommand(Command command, boolean changesContent,
			String beforeCursor1, char atCursor1, String afterCursor1,
			String beforeCursor2, char atCursor2, String afterCursor2) {

		int initalOffset = beforeCursor1.length();
		int expectedFinalOffset = beforeCursor2.length();
		String cursorStr1 = atCursor1 != EOF ? Character.toString(atCursor1) : "";
		String cursorStr2 = atCursor2 != EOF ? Character.toString(atCursor2) : "";
		String initialContent = beforeCursor1 + cursorStr1 + afterCursor1;
		String expectedFinalContent = beforeCursor2 + cursorStr2 + afterCursor2;
		if (!changesContent)
			assertEquals("Test shouldn't expect motion to change content", initialContent, expectedFinalContent);

		content.setText(initialContent);
		cursorAndSelection.setPosition(new DumbPosition(initalOffset), true);
		command.execute(adaptor);
		int actualFinalOffset = cursorAndSelection.getPosition().getModelOffset();
		String actualFinalContent = content.getText();

		String msg;
		String initialLine = formatLine(beforeCursor1, cursorStr1, afterCursor1);
		String expectedLine = formatLine(beforeCursor2, cursorStr2, afterCursor2);
		String actualLine = formatLine(actualFinalContent, actualFinalOffset, actualFinalOffset + 1);

		msg = String.format("STARTING FROM:\n%s\nEXPECTED:\n%s\nGOT:\n%s\n", initialLine, expectedLine, actualLine);
		if (!actualFinalContent.equals(expectedFinalContent) || actualFinalOffset != expectedFinalOffset)
			fail(msg);
	}

	protected static String formatLine(String line, int from, int to) {
		if (from < line.length() && to <= line.length())
			return formatLine(line.substring(0, from), line.substring(from, to), line.substring(to));
		return line + "[EOF]";
	}

	protected static String formatLine(String beforeCursor, String atCursor, String afterCursor) {
		String wholeLine = beforeCursor + atCursor + afterCursor;
		if (beforeCursor.length() < wholeLine.length())
			return String.format("'%s[%s]%s'", beforeCursor, atCursor, afterCursor);
		else
			return wholeLine + "[EOF]";
	}

	public void checkCommand(Command command,
			String beforeCursor1, char atCursor1, String afterCursor1,
			String beforeCursor2, char atCursor2, String afterCursor2) {
		checkCommand(command, true, beforeCursor1, atCursor1, afterCursor1, beforeCursor2, atCursor2, afterCursor2);
	}

	public void checkMotion(Motion motion,
			String beforeCursor1, char atCursor1, String afterCursor1,
			String beforeCursor2, char atCursor2, String afterCursor2) {
		Command command = GoThereState.motion2command(motion);
		checkCommand(command, false, beforeCursor1, atCursor1, afterCursor1, beforeCursor2, atCursor2, afterCursor2);
	}

	public Command forKeySeq(String keyNames) {
		throw new UnsupportedOperationException();
	}

}
