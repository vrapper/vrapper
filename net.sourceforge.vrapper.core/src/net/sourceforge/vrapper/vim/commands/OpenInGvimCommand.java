package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

public class OpenInGvimCommand extends CountIgnoringNonRepeatableCommand {

    public static final OpenInGvimCommand INSTANCE = new OpenInGvimCommand();

    private OpenInGvimCommand() { /* NOP */ }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	Position cursor = editorAdaptor.getPosition();
    	LineInformation line = editorAdaptor.getModelContent().getLineInformationOfOffset(cursor.getModelOffset());
    	int row = line.getNumber() + 1; //1-based, not 0-based
    	int col = cursor.getModelOffset() - line.getBeginOffset() + 1;
    	String gvim = editorAdaptor.getConfiguration().get(Options.GVIM_PATH);
        editorAdaptor.getFileService().openInGvim(gvim, row, col);
    }

}
