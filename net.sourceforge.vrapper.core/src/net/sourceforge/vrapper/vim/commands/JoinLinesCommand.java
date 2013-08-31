package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/* FIXME: In Vim, if line ends with dot, two spaces are inserted instead of one
 * when joining lines. It's not implemented here.
 * I (Krzysiek) don't like that feature anyway, so if you are going to implement it,
 * please provide an option to turn if off ;-)
 */ 
 /* There's an interesting article on why two spaces were used after a period here:
 * http://www.writersdigest.com/online-editor/how-many-spaces-after-a-period
 * 
 *     The “two spaces after period” rule was instituted during the days of 
 *     typewriters. Typewriters had only one font, so all the letters were 
 *     monospaced, or took up the same amount of space.
 * 
 * An on/off switch for .vrapperrc would be best -- BRD
 */

public class JoinLinesCommand extends CountAwareCommand {

    public static final Command INSTANCE = new JoinLinesCommand(true);
    public static final Command DUMB_INSTANCE = new JoinLinesCommand(false);
    private final boolean isSmart;
    
    private JoinLinesCommand(boolean isSmart) {
        this.isSmart = isSmart;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN)
            count = 2;
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            doIt(editorAdaptor, count, isSmart);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    /**
     * @param editorAdaptor
     * @param count
     *            number of lines to be joined from current position. If count <
     *            2, then two lines will be joined
     * @param isSmart
     * @throws CommandExecutionException
     */
    public static void doIt(EditorAdaptor editorAdaptor, int count, boolean isSmart)
            throws CommandExecutionException {
        if (count < 2) {
            count = 2;
        }
        
        TextContent modelContent = editorAdaptor.getModelContent();
        for (int i = 1; i < count; i++) {
            int modelOffset = editorAdaptor.getPosition().getModelOffset();
            LineInformation firstLnInfo = modelContent.getLineInformationOfOffset(modelOffset);
            if (firstLnInfo.getNumber() + 1 == modelContent.getNumberOfLines())
                throw new CommandExecutionException("there is nothing to join below last line");
            LineInformation secondLnInfo = modelContent.getLineInformation(firstLnInfo.getNumber() + 1);
            int eolOffset = firstLnInfo.getEndOffset();
            int bolOffset = secondLnInfo.getBeginOffset();
            String secondLineText = modelContent.getText(bolOffset, secondLnInfo.getLength());
            LineInformation lastLineInfo = modelContent.getLineInformation(modelContent.getNumberOfLines() - 1);
            String glue;
            if (isSmart) {
                glue = " ";
                
                // If there is only newline on the first line, then don't add
                // any space between joined lines (this behavior is not
                // documented in Vim manual, but experiments show that it works
                // this way)
                if (firstLnInfo.getLength() == 0)
                    glue = "";
                else if (Character.isWhitespace(modelContent.getText(eolOffset - 1, 1).charAt(0)))
                    glue = "";
                for (int j = 0; j < secondLineText.length() && Character.isWhitespace(secondLineText.charAt(j)); j++)
                    bolOffset++;
                // On last line of file, if it's a blank line, we don't want to append a space
                if(secondLnInfo.getNumber() == lastLineInfo.getNumber() && secondLineText.length() == 0)
                     glue = "";
                else if (modelContent.getText(bolOffset, 1).charAt(0) == ')')
                    glue = "";
            } else
                glue = "";
                
            modelContent.replace(eolOffset, bolOffset - eolOffset, glue);
            editorAdaptor.setPosition(editorAdaptor.getPosition().setModelOffset(eolOffset),
                    StickyColumnPolicy.ON_CHANGE);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
