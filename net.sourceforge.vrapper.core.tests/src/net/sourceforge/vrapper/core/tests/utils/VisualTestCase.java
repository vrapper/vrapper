package net.sourceforge.vrapper.core.tests.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.Assert.fail;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
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
            Position to = cursorService.newPositionForModelOffset(selectTo - 1);
            selectionService.setSelection(new LineWiseSelection(adaptor, from, to));
        } else {
            Position to = cursorService.newPositionForModelOffset(selectTo);
            selectionService.setSelection(new SimpleSelection(new StartEndTextRange(from, to)));
        }
    }


    private void assertCommandResult(String initialLine,
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

            String msg = "";
            boolean selectionMishmash = false;
            if (expSelFrom != actSelFrom || expSelTo != actSelTo) {
                msg = "selection mishmash\n" + expSelFrom + " " + expSelTo + " but got " + actSelFrom + " " + actSelTo + "\n";
                selectionMishmash = true;
            }

//            int offset = mockEditorAdaptor.getCaretOffset();
            String expectedLine = formatLine(beforeSelection, selected, afterSelection) + "\n";// + cursorLine(expSelTo);
            String   actualLine = formatLine(actualFinalContent,
                    min(actSelFrom, actSelTo),
                    max(actSelFrom, actSelTo)) + "\n";// + cursorLine(offset);

            msg += String.format("STARTING FROM:\n%s\nEXPECTED:\n%s\nGOT:\n%s\n", initialLine, expectedLine, actualLine);
            if (!actualFinalContent.equals(expectedFinalContent) || selectionMishmash) {
                fail(msg);
            }
        }


    protected void checkCommand(Command command,
            boolean inverted1, String beforeSelection1, String selected1, String afterSelection1,
            boolean inverted2, String beforeSelection2, String selected2, String afterSelection2) {

        String  initialLine = formatLine(beforeSelection1, selected1, afterSelection1) + "\n"; // + cursorLine(selectTo);

        prepareEditor(inverted1, beforeSelection1, selected1, afterSelection1);
        executeCommand(command);
        assertCommandResult(initialLine, inverted2, beforeSelection2, selected2, afterSelection2);
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