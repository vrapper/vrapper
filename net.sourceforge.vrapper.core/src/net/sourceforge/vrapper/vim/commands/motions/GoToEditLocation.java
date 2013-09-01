package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class GoToEditLocation extends CountAwareMotion {
	
	public static final Motion FORWARD = new GoToEditLocation(true);
	public static final Motion BACKWARDS = new GoToEditLocation(false);
	
	private boolean forward;
	
	private GoToEditLocation(boolean forward) {
		this.forward = forward;
	}

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
		if(count == NO_COUNT_GIVEN) {
			count = 1;
		}
		
		Position markPos;
		if(forward) {
			markPos = editorAdaptor.getCursorService().getNextChangeLocation(count);
		}
		else {
			markPos = editorAdaptor.getCursorService().getPrevChangeLocation(count);
		}
		
        if (markPos == null) {
            throw new CommandExecutionException( forward ? "At end of changelist" : "At start of changelist" );
        }
        
        return markPos;
	}

	public BorderPolicy borderPolicy() {
		return BorderPolicy.EXCLUSIVE;
	}

	public StickyColumnPolicy stickyColumnPolicy() {
		return StickyColumnPolicy.ON_CHANGE;
	}

	@Override
    public boolean isJump() {
        return true;
    }

}
