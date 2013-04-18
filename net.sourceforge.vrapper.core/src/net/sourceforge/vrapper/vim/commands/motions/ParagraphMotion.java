package net.sourceforge.vrapper.vim.commands.motions;

import static net.sourceforge.vrapper.utils.VimUtils.isLineBlank;
import net.sourceforge.vrapper.platform.Configuration;
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

    private ParagraphMotion(boolean moveForward) {
        step = moveForward ? 1 : -1;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN)
            count = 1;
        
        TextContent modelContent = editorAdaptor.getModelContent();
        LineInformation currentLine = modelContent.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset());
        
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
        return editorAdaptor.getPosition().setModelOffset(offset);
    }

    protected int moveMore(TextContent modelContent, int lineNo) {
        return lineNo;
    }

    protected boolean isLineEmpty(TextContent content, int lineNo) {
        return doesLineEmptinessEqual(true, content, lineNo);
    }
    
    protected boolean isLineNonEmpty(TextContent content, int lineNo) {
        return doesLineEmptinessEqual(false, content, lineNo);
    }
    
    protected boolean isInRange(TextContent content, int lineNo) {
        return (lineNo + step >= 0) && (lineNo + step < content.getNumberOfLines());
    }
    
    private boolean doesLineEmptinessEqual(boolean equalWhat, TextContent content, int lineNo) {
        boolean isEmpty = content.getLineInformation(lineNo).getLength() == 0;
        return isEmpty == equalWhat;
    }
    
    public BorderPolicy borderPolicy() {
        return BorderPolicy.EXCLUSIVE;
    }

    public boolean updateStickyColumn() {
        return true;
    }
    
    @Override
    public boolean isJump() {
        return true;
    }

    public static class ParagraphTextObject extends AbstractTextObject {

        private boolean outer;
        
        public ParagraphTextObject(boolean outer) {
            super();
            this.outer = outer;
        }

        public TextRange getRegion(EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            if (count == NO_COUNT_GIVEN) {
                count = 1;
            }
           
            // EOL "lines" at the end of the text buffer will be of the same
            // type (blank, non-blank) as the previous line
            boolean endsWithEOL = VimUtils.endsWithEOL(editorAdaptor);
            
            TextContent content = editorAdaptor.getModelContent();
            int startLineNo = content.getLineInformationOfOffset(editorAdaptor.getPosition().getModelOffset()).getNumber();
            if (endsWithEOL && startLineNo > 0 &&
                    (startLineNo + 1) == content.getNumberOfLines()) {
                startLineNo--;
            }
            
            boolean cursorOnBlank = isLineBlank(content, startLineNo);
            while (startLineNo > 0) {
                boolean upperLineIsBlank = isLineBlank(content, startLineNo - 1);
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
                                boolean lowerLineIsBlank = isLineBlank(content, endLineNo + 1);
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
            
            Position startPos = editorAdaptor.getPosition().setModelOffset(content.getLineInformation(startLineNo).getBeginOffset());
            Position endPos = editorAdaptor.getPosition().setModelOffset(content.getLineInformation(endLineNo).getEndOffset()); ;
            return new LineWiseSelection(editorAdaptor, startPos, endPos);
        }

        public ContentType getContentType(Configuration configuration) {
            return ContentType.LINES;
        }
    }
}