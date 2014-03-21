package net.sourceforge.vrapper.vim.commands;

import java.io.IOException;

import net.sourceforge.vrapper.log.VrapperLog;
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
    	int col = cursor.getModelOffset() - line.getBeginOffset() + 1; //1-based, not 0-based
    	String gvim = editorAdaptor.getConfiguration().get(Options.GVIM_PATH);
    	String args = editorAdaptor.getConfiguration().get(Options.GVIM_ARGS);
        try {
            editorAdaptor.getFileService().openInGvim(gvim, args, row, col);
        } catch (IOException e) {
            VrapperLog.error("Failed to open file in Vim, file might not have been written yet.", e);
            throw new CommandExecutionException("Could not open current file externally, save first.");
        }
    }

}
