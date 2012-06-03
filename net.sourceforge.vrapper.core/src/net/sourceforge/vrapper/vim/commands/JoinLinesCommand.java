package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;

// FIXME: In Vim, if line ends with dot, two spaces are inserted isnead of one
// when joining lines. It's not implemented here.
// I (Krzysiek) don't like that feature anyway, so if you are going to implement it,
// please provide an option to turn if off ;-)

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
            count = 1;
        try {
            editorAdaptor.getHistory().beginCompoundChange();
            doIt(editorAdaptor, count, isSmart);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public static void doIt(EditorAdaptor editorAdaptor, int count, boolean isSmart)
            throws CommandExecutionException {
        TextContent modelContent = editorAdaptor.getModelContent();
        for (int i = 0; i < count; i++) {
            int modelOffset = editorAdaptor.getPosition().getModelOffset();
            LineInformation firstLnInfo = modelContent.getLineInformationOfOffset(modelOffset);
            if (firstLnInfo.getNumber() + 1 == modelContent.getNumberOfLines())
                throw new CommandExecutionException("there is nothing to join below last line");
            LineInformation secondLnInfo = modelContent.getLineInformation(firstLnInfo.getNumber() + 1);
            int eolOffset = firstLnInfo.getEndOffset();
            int bolOffset = secondLnInfo.getBeginOffset();
            String secondLine = modelContent.getText(bolOffset, secondLnInfo.getLength());
            String glue;
            if (isSmart) {
                glue = " ";
                if (firstLnInfo.getLength() > 0 && Character.isWhitespace(modelContent.getText(eolOffset - 1, 1).charAt(0)))
                    glue = "";
                for (int j = 0; j < secondLine.length() && Character.isWhitespace(secondLine.charAt(j)); j++)
                    bolOffset++;
                if (modelContent.getText(bolOffset, 1).charAt(0) == ')')
                    glue = "";
            } else
                glue = "";
            modelContent.replace(eolOffset, bolOffset - eolOffset, glue);
            editorAdaptor.setPosition(editorAdaptor.getPosition().setModelOffset(eolOffset), true);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
