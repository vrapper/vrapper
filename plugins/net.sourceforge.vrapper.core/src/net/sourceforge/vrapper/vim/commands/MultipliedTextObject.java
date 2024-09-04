package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class MultipliedTextObject implements TextObject {

    private final int count;
    private final TextObject textObject;

    public MultipliedTextObject(int count, TextObject textObject) {
        this.count = count;
        this.textObject = textObject;
    }

    public ContentType getContentType(Configuration configuration) {
        return textObject.getContentType(configuration);
    }

    public TextRange getRegion(EditorAdaptor editorMode, int count) throws CommandExecutionException {
        int newCount = NO_COUNT_GIVEN;

        // NO_COUNT_GIVEN is a special value, if neither counts are specified we stick to it.If one
        // of the two values is specified (or both), we multiply the counts. The case where
        // this.count == NO_COUNT_GIVEN is rare, seeing how this is the MultipliedTextObject class.
        if (count != NO_COUNT_GIVEN || this.count != NO_COUNT_GIVEN) {
            newCount = (count == NO_COUNT_GIVEN ? 1 : count)
                    * (this.count == NO_COUNT_GIVEN ? 1 : this.count);
        }
        return textObject.getRegion(editorMode, newCount);
    }

    public TextObject withCount(int count) {
        return new MultipliedTextObject(count, textObject);
    }

    public int getCount() {
        return count;
    }

    @Override
    public TextObject repetition() {
        TextObject repeatedTextObj = textObject.repetition();

        if (repeatedTextObj == null) {
            return null;
        } else {
            return new MultipliedTextObject(count, repeatedTextObj);
        }
    }
}
