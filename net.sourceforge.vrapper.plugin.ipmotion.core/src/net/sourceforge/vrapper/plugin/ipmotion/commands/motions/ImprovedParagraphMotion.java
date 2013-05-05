package net.sourceforge.vrapper.plugin.ipmotion.commands.motions;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.motions.ParagraphMotion;

public class ImprovedParagraphMotion extends ParagraphMotion {
    
    public static final ImprovedParagraphMotion FORWARD = new ImprovedParagraphMotion(true);
    public static final ImprovedParagraphMotion BACKWARD = new ImprovedParagraphMotion(false);

    protected ImprovedParagraphMotion(final boolean moveForward) {
        super(moveForward);
    }

    @Override
    protected boolean doesLineEmptinessEqual(final boolean equalWhat, final TextContent content, final int lineNo) {
        final LineInformation info = content.getLineInformation(lineNo);
        final String line = content.getText(info.getBeginOffset(), info.getLength());
        final boolean isEmpty = line.trim().length() == 0;
        return isEmpty == equalWhat;
    }
    
}
