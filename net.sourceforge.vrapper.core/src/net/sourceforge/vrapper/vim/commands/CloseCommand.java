package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class CloseCommand extends CountIgnoringNonRepeatableCommand {

    public static final CloseCommand FORCED_CLOSE = new CloseCommand(true, false);
    public static final CloseCommand CLOSE = new CloseCommand(false, false);
    public static final CloseCommand FORCED_CLOSE_ALL = new CloseCommand(true, true);
    public static final CloseCommand CLOSE_ALL = new CloseCommand(false, true);
    
    private final boolean force;
    private final boolean closeAll;

    private CloseCommand(boolean force, boolean all) {
        super();
        this.force = force;
        this.closeAll = all;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
    	if(closeAll) {
    		if (!editorAdaptor.getFileService().closeAll(force)) {
    			throw new CommandExecutionException("There are unsaved changes");
    		}
    	}
    	else {
    		if (!editorAdaptor.getFileService().close(force)) {
    			throw new CommandExecutionException("There are unsaved changes");
    		}
    	}
    }

}
