package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

public class CloseCommand extends CountIgnoringNonRepeatableCommand {
	
	private enum CloseOption {
		ONE,
		ALL,
		OTHERS
	}

    public static final CloseCommand FORCED_CLOSE = new CloseCommand(true, CloseOption.ONE);
    public static final CloseCommand CLOSE = new CloseCommand(false, CloseOption.ONE);
    public static final CloseCommand FORCED_CLOSE_ALL = new CloseCommand(true, CloseOption.ALL);
    public static final CloseCommand CLOSE_ALL = new CloseCommand(false, CloseOption.ALL);
    public static final CloseCommand FORCED_CLOSE_OTHERS = new CloseCommand(true, CloseOption.OTHERS);
    public static final CloseCommand CLOSE_OTHERS = new CloseCommand(false, CloseOption.OTHERS);
    
    private final boolean force;
    private final CloseOption option;

    private CloseCommand(boolean force, CloseOption option) {
        super();
        this.force = force;
        this.option = option;
    }

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
    	boolean success = false;
    	
    	switch(option) {
    	case ONE:
    		success = editorAdaptor.getFileService().close(force);
    		break;
    	case ALL:
    		success = editorAdaptor.getFileService().closeAll(force);
    		break;
    	case OTHERS:
    		success = editorAdaptor.getFileService().closeOthers(force);
    		break;
    	}
    	
    	if(! success) {
    		throw new CommandExecutionException("There are unsaved changes");
    	}
    }

}
