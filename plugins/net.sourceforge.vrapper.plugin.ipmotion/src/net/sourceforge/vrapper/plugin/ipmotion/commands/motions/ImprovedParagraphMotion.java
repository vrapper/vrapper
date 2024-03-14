package net.sourceforge.vrapper.plugin.ipmotion.commands.motions;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;

import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.ParagraphMotion;

public class ImprovedParagraphMotion extends ParagraphMotion {
    
    public static final ImprovedParagraphMotion FORWARD = new ImprovedParagraphMotion(true);
    public static final ImprovedParagraphMotion BACKWARD = new ImprovedParagraphMotion(false);
    public static final State<Motion> PARAGRAPH_MOTIONS;
    
    static {
        final Motion paragraphBackward = ImprovedParagraphMotion.BACKWARD;
        final Motion paragraphForward = ImprovedParagraphMotion.FORWARD;
        
        final State<Motion> ipMotions = state(
                leafBind('{', paragraphBackward),
                leafBind('}', paragraphForward));
        PARAGRAPH_MOTIONS = ipMotions;
    }
    
    /** From original ipmotion.vim:
     * 
     *  The global definition of paragraph boundary.
     *  Default value is "\s*$".
     *  It can be changed in .vimrc or anytime. Defining
     *  b:ip_boundary will override this setting.
     *
     *  Example:
     *  :let g:ip_boundary = '"\?\s*$'
     *  Setting that will make empty lines, and lines only
     *  contains '"' as boundaries.
     *
     *  Note that there is no need adding a "^" sign at the
     *  beginning. It is enforced by the script.
     * 
     */
    private final String gIpBoundary = "\\s*$";
    
    /**
     * Built from gIpBoundary; used to determine line emptiness.
     *  If vrapper supports script variables, the behavior could
     *  be customized....
     */
    private final Pattern mIpBoundary;

    protected ImprovedParagraphMotion(final boolean moveForward) {
        super(moveForward);
        
        mIpBoundary = Pattern.compile("^(" + gIpBoundary +")");
    }

    @Override
    protected boolean doesLineEmptinessEqual(final boolean equalWhat, final TextContent content, final int lineNo) {
        final LineInformation info = content.getLineInformation(lineNo);
        final String line = content.getText(info.getBeginOffset(), info.getLength());
        final boolean isEmpty = mIpBoundary.matcher(line).matches();
//        final boolean isEmpty = Pattern.matches("^(" + gIpBoundary + ")", line); // for testing, with hotswap
        return isEmpty == equalWhat;
    }
    
}
