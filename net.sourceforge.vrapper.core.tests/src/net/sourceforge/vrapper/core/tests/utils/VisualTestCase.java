package net.sourceforge.vrapper.core.tests.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.*;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public abstract class VisualTestCase extends CommandTestCase {

    protected static String cursorLine(int offset) {
        StringBuilder cursorLine = new StringBuilder(">");
        int start = offset == 0 ? 1 : 0;
        for (int i=start; i<=offset; i++) {
            cursorLine.append(' ');
        }
        cursorLine.append("^\n");
        return cursorLine.toString();
    }

    private void prepareEditor(boolean inverted,
            String beforeSelection, String selected, String afterSelection) {
        String initialContent = beforeSelection + selected + afterSelection;

        content.setText(initialContent);
        int selectFrom, selectTo;
        selectFrom = selectTo = beforeSelection.length();
        if (!inverted) {
            selectTo += selected.length();
        } else {
            selectFrom += selected.length();
        }

        // Don't switch when already in temp Visual mode or linewise mode.
        if ( ! (adaptor.getMode(adaptor.getCurrentModeName()) instanceof AbstractVisualMode)) {
            adaptor.changeModeSafely(VisualMode.NAME);
        }

        CursorService cursorService = platform.getCursorService();
        SelectionService selectionService = platform.getSelectionService();
        Position from = cursorService.newPositionForModelOffset(selectFrom);
        if (selected.endsWith("\n")) {
            // Test in linewise
            Position to = cursorService.newPositionForModelOffset(selectTo - 1);
            selectionService.setSelection(new LineWiseSelection(adaptor, from, to));
        } else {
            // Plain visual
            Position to = cursorService.newPositionForModelOffset(selectTo);
            StartEndTextRange selectionRange = new StartEndTextRange(from, to);
            boolean isSelectionInclusive = Selection.INCLUSIVE.equals(
                    adaptor.getConfiguration().get(Options.SELECTION));

            if (isSelectionInclusive && from.compareTo(to) == 0) {
                throw new IllegalArgumentException("Error in test: Visual mode with inclusive "
                        + "selection should always have at least one character selected!");
            }
            SimpleSelection selection;
            selection = new SimpleSelection(cursorService, isSelectionInclusive, selectionRange);
            selectionService.setSelection(selection);
        }
    }


    public void assertVisualResult(String initialLine,
        boolean inverted, String beforeSelection, String selected, String afterSelection) {
        String expectedFinalContent = beforeSelection + selected + afterSelection;
        String actualFinalContent = content.getText();
        int actSelFrom;
        int actSelTo;
        TextRange selection = adaptor.getSelection();
        if (selection != null) {
            actSelFrom = selection.getStart().getModelOffset();
            actSelTo = selection.getEnd().getModelOffset();
        } else {
            actSelFrom = actSelTo = adaptor.getCursorService().getPosition().getModelOffset();
        }
        int expSelTo, expSelFrom;
        expSelFrom = expSelTo = beforeSelection.length();
        if (!inverted) {
            expSelTo += selected.length();
        } else {
            expSelFrom += selected.length();
        }

        String expectedLine = formatLine(beforeSelection, selected, afterSelection) + "\n";// + cursorLine(expSelTo);
        String   actualLine = formatLine(actualFinalContent,
                min(actSelFrom, actSelTo),
                max(actSelFrom, actSelTo)) + "\n";// + cursorLine(offset);
        String msg = String.format("STARTING FROM:\n'%s'\nEXPECTED:\n'%s'\nGOT:\n'%s'\n", initialLine, expectedLine, actualLine);

        assertEquals(msg, expectedFinalContent, actualFinalContent);
        
        if (expSelFrom != actSelFrom || expSelTo != actSelTo) {
            msg = "selection mishmash\n" + expSelFrom + " " + expSelTo + " but got " + actSelFrom + " " + actSelTo + "\n" + msg;
        }
        // First compare strings with selection markers. Eclipse JUnit runner has a nice
        // visual compare for this.
        assertEquals(msg, expectedLine, actualLine);
        // Now check that start and end positions aren't off (selection might be
        // reversed or content might contain '[' characters throwing off compare)
        assertEquals(msg, expSelFrom, actSelFrom);
        assertEquals(msg, expSelTo, actSelTo);
    }

    protected void checkCommand(Command command,
            boolean inverted1, String beforeSelection1, String selected1, String afterSelection1,
            boolean inverted2, String beforeSelection2, String selected2, String afterSelection2) {

        String  initialLine = formatLine(beforeSelection1, selected1, afterSelection1) + "\n"; // + cursorLine(selectTo);

        prepareEditor(inverted1, beforeSelection1, selected1, afterSelection1);
        executeCommand(command);
        assertVisualResult(initialLine, inverted2, beforeSelection2, selected2, afterSelection2);
    }

    protected void checkLeavingCommand(Command command,
            boolean inverted, String beforeSelection, String selected, String afterSelection,
            String beforeCursor, char atCursor, String afterCursor) {
        String  initialLine = formatLine(beforeSelection, selected, afterSelection) + "\n"; // + cursorLine(selectTo);

        prepareEditor(inverted, beforeSelection, selected, afterSelection);
        executeCommand(command);
        assertCommandResult(initialLine, beforeCursor, atCursor, afterCursor);
    }

}