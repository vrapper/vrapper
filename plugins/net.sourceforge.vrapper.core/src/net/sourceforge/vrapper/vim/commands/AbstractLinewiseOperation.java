package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Base class for line-range-based operations which <i>also</i> work in line mode if invoked as a
 * TextOperation. If the operation should not modify full lines in all cases, implement the
 * {@link LineWiseOperation} interface directly.
 */
public abstract class AbstractLinewiseOperation implements TextOperation, LineWiseOperation {

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count, TextObject textObject)
            throws CommandExecutionException {
        LineRange range;
        if (textObject instanceof LineRange) {
            //It would be better if the caller invoked the right method, but just pass it on.
            range = (LineRange) textObject;
        } else if (textObject instanceof Selection) {
            range = SimpleLineRange.fromSelection(editorAdaptor, (Selection) textObject);
        } else {
            TextRange originalRegion = textObject.getRegion(editorAdaptor, count);
            if (originalRegion == null) {
                range = getDefaultRange(editorAdaptor, count, editorAdaptor.getPosition());
            } else {
                range = SimpleLineRange.fromTextRange(editorAdaptor, originalRegion);
            }
        }
        execute(editorAdaptor, range);
    }
}
