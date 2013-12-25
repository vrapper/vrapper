package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;

/**
 * Move to the next (or previous) '{' or '}' **in the first column only**.
 * I don't understand how this feature is useful, but people requested it.
 */
public class SectionMotion extends MethodDeclarationMotion {

    public static final SectionMotion NEXT_START = new SectionMotion(false, true);
    public static final SectionMotion PREV_START = new SectionMotion(true, true);
    public static final SectionMotion NEXT_END   = new SectionMotion(false, false);
    public static final SectionMotion PREV_END   = new SectionMotion(true, false);
    
    protected SectionMotion(boolean backwards, boolean methodBegin) {
        super(backwards, methodBegin);
    }

    @Override
    protected int doIt(int offset, TextContent content) {
        int delta = this.backwards ? -1 : 1;
        char toFind = this.methodBegin ? '{' : '}';
        LineInformation line = content.getLineInformationOfOffset(offset);

        //don't start on current line
        int lineNo = line.getNumber() + delta;
        char testChar;
        while(lineNo >= 0 && lineNo < content.getNumberOfLines()) {
            line = content.getLineInformation(lineNo);
            if(line.getLength() > 0) {
                testChar = content.getText(line.getBeginOffset(), 1).charAt(0);
                if(testChar == toFind) {
                    return line.getBeginOffset();
                }
            }
            lineNo += delta;
        }
        return offset;
    }

}
