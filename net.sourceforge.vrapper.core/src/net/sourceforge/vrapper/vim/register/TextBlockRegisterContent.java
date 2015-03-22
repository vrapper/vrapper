package net.sourceforge.vrapper.vim.register;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.StringUtils;

/**
 * Register content for text blocks.
 */
public class TextBlockRegisterContent implements RegisterContent {

    private final List<String> payload = new ArrayList<String>();
    private final int visualWidth;
    private String newLine;

    public TextBlockRegisterContent(int visualWidth, String newLine) {
        this.visualWidth = visualWidth;
        this.newLine = newLine;
    }

    public ContentType getPayloadType() {
        return ContentType.TEXT_RECTANGLE;
    }

    public void appendLine(String line) {
        payload.add(line);
    }

    public String getText() {
        // Vim includes newlines for each line in the block
        return StringUtils.join(newLine, payload).concat(newLine);
    }

    public int getVisualWidth() {
        return visualWidth;
    }

    public int getNumLines() {
        return payload.size();
    }

    public String getLine(int lineNo) {
        return payload.get(lineNo);
    }

}
