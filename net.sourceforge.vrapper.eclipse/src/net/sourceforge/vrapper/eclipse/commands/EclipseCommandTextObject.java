package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class EclipseCommandTextObject extends AbstractTextObject {

    private final String commandName;

    public EclipseCommandTextObject(String commandId) {
        commandName = commandId;
    }

    @Override
    public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        for (int i = 0; i < count; i++) {
            EclipseCommand.doIt(commandName, editorAdaptor, false);
        }
        TextRange result = editorAdaptor.getNativeSelection();
        // For when the Eclipse command did not reset Vrapper's selection.
        if (SelectionService.VRAPPER_SELECTION_ACTIVE.equals(result)) {
            result = editorAdaptor.getSelection().getRegion(editorAdaptor, count);
        }
        // Update sticky column by repositioning cursor. SelectTextObjectCommand will set selection
        Position newCarretOffset = editorAdaptor.getPosition();
        editorAdaptor.setPosition(newCarretOffset, StickyColumnPolicy.ON_CHANGE);
        return result;
    }

    @Override
    public ContentType getContentType(Configuration configuration) {
        return ContentType.TEXT;
    }

}
