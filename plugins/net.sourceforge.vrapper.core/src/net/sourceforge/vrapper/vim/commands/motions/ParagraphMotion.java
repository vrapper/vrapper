package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.utils.VimUtils.isLineBlank;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractTextObject;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;

public class ParagraphMotion extends CountAwareMotion {
    public static final ParagraphMotion FORWARD = new ParagraphMotion(true);
    public static final ParagraphMotion BACKWARD = new ParagraphMotion(false);
    
    protected final int step;

    protected ParagraphMotion(final boolean moveForward) {
        step = moveForward ? 1 : -1;
    }

    @Override
    public Position destination(final EditorAdaptor editorAdaptor, int count, Position fromPosition)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN)
            count = 1;
        
        final TextContent modelContent = editorAdaptor.getModelContent();
        final LineInformation currentLine = modelContent.getLineInformationOfOffset(fromPosition.getModelOffset());
        
        int lineNo = currentLine.getNumber();
        for (int i = 0; i < count; i++) {
            while (isInRange(modelContent, lineNo) && isLineEmpty(modelContent, lineNo))
                lineNo += step;
            while (isInRange(modelContent, lineNo) && isLineNonEmpty(modelContent, lineNo))
                lineNo += step;
        }
        
        lineNo = moveMore(modelContent, lineNo);
        int offset = modelContent.getLineInformation(lineNo).getBeginOffset();
        
        // If we are moving forward and we are on the last line, then put cursor
        // at the end of the line
        if (step > 0 && ((lineNo + 1) == modelContent.getNumberOfLines())) {
            offset = modelContent.getLineInformation(lineNo).getEndOffset();
        }
        return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
    }

    protected int moveMore(final TextContent modelContent, final int lineNo) {
        return lineNo;
    }

    protected boolean isLineEmpty(final TextContent content, final int lineNo) {
        return doesLineEmptinessEqual(true, content, lineNo);
    }
    
    protected boolean isLineNonEmpty(final TextContent content, final int lineNo) {
        return doesLineEmptinessEqual(false, content, lineNo);
    }
    
    protected boolean isInRange(final TextContent content, final int lineNo) {
        return (lineNo + step >= 0) && (lineNo + step < content.getNumberOfLines());
    }
    
    protected boolean doesLineEmptinessEqual(final boolean equalWhat, final TextContent content, final int lineNo) {
        final boolean isEmpty = content.getLineInformation(lineNo).getLength() == 0;
        return isEmpty == equalWhat;
    }
    
    @Override
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    @Override
    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }
    
    @Override
    public boolean isJump() {
        return true;
    }

    public static class ParagraphTextObject extends AbstractTextObject {

        private final boolean outer;
        
        public ParagraphTextObject(final boolean outer) {
            super();
            this.outer = outer;
        }

        @Override
        public TextRange getRegion(final EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            if (count == NO_COUNT_GIVEN) {
                count = 1;
            }
           
            // EOL "lines" at the end of the text buffer will be of the same
            // type (blank, non-blank) as the previous line
            final boolean endsWithEOL = VimUtils.endsWithEOL(editorAdaptor);
            
            final TextContent content = editorAdaptor.getModelContent();
            int startLineNo = content.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset()).getNumber();
            if (endsWithEOL && startLineNo > 0 &&
                    (startLineNo + 1) == content.getNumberOfLines()) {
                startLineNo--;
            }
            
            final boolean cursorOnBlank = isLineBlank(content, startLineNo);
            while (startLineNo > 0) {
                final boolean upperLineIsBlank = isLineBlank(content, startLineNo - 1);
                if (cursorOnBlank ^ upperLineIsBlank) {
                    break;
                }
                
                startLineNo--;
            }
            
            // Either a special case for blank lines in the last section of
            // file, or fail case (in both cases, the cursor remain in the
            // same position)
            boolean doNothing = false;
            int endLineNo = startLineNo;
            if (outer) {
                boolean noMoreRepeat = false;
                for (int i = count; i > 0; i--) {
                    if (noMoreRepeat) {
                        // Fail
                        doNothing = true;
                        break;
                    }
                    else {
                        // if cursorOnBlank==true, then eat blanks
                        // if cursorOnBlank==false, then eat non-blanks
                        while ((endLineNo + 1) < content.getNumberOfLines() &&
                                (!cursorOnBlank || isLineBlank(content, endLineNo + 1)) &&
                                (cursorOnBlank || !isLineBlank(content, endLineNo + 1)))
                            endLineNo++;
                        
                        if (endsWithEOL && (endLineNo + 2) == content.getNumberOfLines()) {
                            endLineNo++;
                        }
                        
                        if ((endLineNo + 1) >= content.getNumberOfLines()) {
                            if (cursorOnBlank) {
                                doNothing = true;
                                noMoreRepeat = true;
                            }
                            else {
                                // This is a special case, where we eat blank
                                // lines above the current start line
                                while (startLineNo > 0) {
                                    if (!isLineBlank(content, startLineNo - 1)) {
                                        break;
                                    }

                                    startLineNo--;
                                }

                                noMoreRepeat = true;
                            }
                        }
                        
                        if ((endLineNo + 1) < content.getNumberOfLines()) {
                            endLineNo++;

                            // if cursorOnBlank==true, then eat non-blanks
                            // if cursorOnBlank==false, then eat blanks
                            while ((endLineNo + 1) < content.getNumberOfLines() &&
                                    (!cursorOnBlank || !isLineBlank(content, endLineNo + 1)) &&
                                    (cursorOnBlank || isLineBlank(content, endLineNo + 1)))
                                endLineNo++;

                            if ((endLineNo + 1) >= content.getNumberOfLines()) {
                                noMoreRepeat = true;
                            } else {
                                endLineNo++;
                            }
                        }
                    }
                }
            }
            else {
                boolean isCurrentSectionBlank = cursorOnBlank;
                boolean noMoreRepeat = false;
                for (int i = count; i > 0; i--) {
                    if (noMoreRepeat) {
                        // Fail
                        doNothing = true;
                        break;
                    }
                    else {
                        while (true) {
                            if ((endLineNo + 1) >= content.getNumberOfLines()) {
                                noMoreRepeat = true;
                                break;
                            }
                            
                            if (endsWithEOL && (endLineNo + 2) == content.getNumberOfLines()) {
                                endLineNo++;
                            }
                            else {
                                final boolean lowerLineIsBlank = isLineBlank(content, endLineNo + 1);
                                if (isCurrentSectionBlank ^ lowerLineIsBlank) {
                                    break;
                                }
                                else {
                                    endLineNo++;
                                }
                            }
                        }
                        
                        isCurrentSectionBlank = !isCurrentSectionBlank;
                    }
                }
                
                if ((endLineNo + 1) < content.getNumberOfLines()) {
                    endLineNo++;
                }
            }
            
            if (doNothing) {
                return null;
            }
            
            // if endLineNo is not pointing at the last line of the buffer, then
            // it is always pointing one line after the selected paragraph, so
            // we decrement it
            if ((endLineNo + 1) < content.getNumberOfLines() && endLineNo > 0) {
                endLineNo--;
            }
            
            final CursorService cursorService = editorAdaptor.getCursorService();
            final Position startPos = cursorService.newPositionForModelOffset(content.getLineInformation(startLineNo).getBeginOffset());
            final Position endPos = cursorService.newPositionForModelOffset(content.getLineInformation(endLineNo).getEndOffset()); ;
            return new LineWiseSelection(editorAdaptor, startPos, endPos);
        }

        @Override
        public ContentType getContentType(final Configuration configuration) {
            return ContentType.LINES;
        }
    }
}