package net.sourceforge.vrapper.vim.commands;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * The cursor is inside a pair of XML tags.  Find the open tag *before* the cursor
 * that matches the closing tag *after* the cursor.  Note that We don't know which
 * open tag name we're looking for until we find the unbalanced closing tag after
 * the cursor.  This is to handle malformed XML documents with lingering open tags.
 * This aligns with how Vim handles things.
 */
public class XmlTagDelimitedText implements DelimitedText {
    /**
     * Look for close tag first. Easier regex pattern to find and get the tag name.
     * If valid tag name found, then search backwards for matching opening tag.
     */
    private static final String XML_CLOSE_TAG_REGEX = "<\\/([a-zA-Z]\\w*(?:\\:?[a-zA-Z]\\w*)(?:[^>])*)>";
    private static final Pattern closeTagPattern = Pattern.compile(XML_CLOSE_TAG_REGEX);

    private static final String XML_TAG_ATTR_REGEX = "(?:(?:\\s*<%.*?%>\\s*)|(?:\\s*\\w=\"[^\"]+\"\\s*))*>";
    private static final Pattern tagAttrPattern = Pattern.compile(XML_TAG_ATTR_REGEX);

    private TextRange openTag = null;
    private TextRange closeTag = null;

    @Override
    public TextRange leftDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (openTag == null) {
            calculateOpenAndCloseTag(offset, editorAdaptor, count);
        }
        return openTag;
    }

    @Override
    public TextRange rightDelimiter(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (closeTag == null) {
            calculateOpenAndCloseTag(offset, editorAdaptor, count);
        }
        return closeTag;
    }

    private void calculateOpenAndCloseTag(int offset, EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        openTag = closeTag = null;

        Position beginningPosition = editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    	Position startOpenSearch = beginningPosition.addModelOffset(1);

        TextContent content = editorAdaptor.getModelContent();
        int startPos = startOpenSearch.getModelOffset();
        String textAfterStartPos = getText(editorAdaptor, startPos, content.getTextLength());

        // find close tag with specified 'count' iteration
        // if no close tag found with iteration, throw CommandExecutionException
        Matcher closeTagMatch = closeTagPattern.matcher(textAfterStartPos);
        for (int i = 0; i < count; ++i) {
            if (!closeTagMatch.find()) {
                throw new CommandExecutionException("No close tag found. Count: " + count + ", i: " + i);
            }
        }

        closeTag = getRange(editorAdaptor, closeTagMatch.start(), closeTagMatch.end());
        String tagName = closeTagMatch.group(1);

        // search backwards for a valid matching open tag
        String textBeforeCloseTag = getText(editorAdaptor, 0, closeTagMatch.start());
        String openTagStr = '<' + tagName;
        int openTagLen = openTagStr.length();
        int openTagStart = closeTagMatch.start() - 1;

        while (openTagStart >= 0) {
            openTagStart = textBeforeCloseTag.lastIndexOf(openTagStr, openTagStart);
            int nextcIdx = openTagStart + openTagLen;
            // ensure it's the proper open tag by inspecting next char
            char nextc = textBeforeCloseTag.charAt(nextcIdx);
            if (nextc == '>') {
                // matching open tag found
                openTag = getRange(editorAdaptor, openTagStart, openTagStart + openTagLen + 1);
                break;
            } else {
                // need to scan rest of tag to ensure tag validity
                Matcher openTagAttrMatch = tagAttrPattern.matcher(textBeforeCloseTag.substring(nextcIdx, closeTagMatch.end()));
                if (openTagAttrMatch.find()) {
                    // valid matching open tag found
                    openTag = getRange(editorAdaptor, openTagStart, openTagAttrMatch.end());
                    break;
                }
            }

            --openTagStart;
        }

        if (openTag == null) {
            // if no matching open tag found with iteration, throw CommandExecutionException
            throw new CommandExecutionException("No open tag found. Count: " + count);
        }
    }

    private String getText(EditorAdaptor editorAdaptor, int from, int to) {
        return editorAdaptor.getModelContent().getText(from, to - from);
    }

    private TextRange getRange(EditorAdaptor editorAdaptor, int start, int end) {
        Position matchBegin = editorAdaptor.getPosition().setModelOffset(start);
        Position matchEnd   = editorAdaptor.getPosition().setModelOffset(end);
        return new StartEndTextRange(matchBegin, matchEnd);
    }

}
