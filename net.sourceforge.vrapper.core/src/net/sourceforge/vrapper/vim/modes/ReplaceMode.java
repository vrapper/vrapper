package net.sourceforge.vrapper.vim.modes;

import java.util.HashMap;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Replace mode for overwriting existing text.
 *
 * @author Krzysiek Goj
 * @author Matthias Radig
 *
 * TODO: implement counts
 */
public class ReplaceMode extends InsertMode {

    public static final String NAME = "replace mode";
    public static final String DISPLAY_NAME = "REPLACE";
    private int startCursorOffset;
    private boolean afterNewline = false;
    private HashMap<Integer, String> replacedChars = new HashMap<Integer, String>();

    public ReplaceMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public void enterMode(ModeSwitchHint... args) throws CommandExecutionException {
        editorAdaptor.getEditorSettings().setReplaceMode(true);
        editorAdaptor.getCursorService().setCaret(CaretType.UNDERLINE);
        startCursorOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
        afterNewline = false;
        super.enterMode(args);
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints) {
        editorAdaptor.getEditorSettings().setReplaceMode(false);
        replacedChars.clear();
        super.leaveMode();
    }
    
    /**
     * Add some wacky logic to support backspace in Replace Mode. We're actually
     * using Eclipse's Overwrite mode, which replaces characters moving forward
     * but backspace works as usual.  In Vim, characters are replaced moving
     * forward (same as Eclipse) but the backspace key restores the previous
     * character rather than deleting it.  So, I have to hijack the backspace
     * key press and restore the previous character without telling Eclipse.
     */
    @Override
    public boolean handleKey(KeyStroke stroke) {
    	int cursorOffset = editorAdaptor.getCursorService().getPosition().getModelOffset();
    	
    	if(stroke.equals(BACKSPACE)) { //restore the char that used to be here
    		cursorOffset--;
    		if(replacedChars.containsKey(cursorOffset)) {
    			String toRestore = replacedChars.get(cursorOffset);
    			
    			if(afterNewline && VimUtils.isNewLine(toRestore)) {
    				//don't restore the newline, but we can start
    				//restoring characters on the next backspace
    				editorAdaptor.getModelContent().replace(cursorOffset, 1, "");
    				afterNewline = false;
    			}
    			else {
    				editorAdaptor.getModelContent().replace(cursorOffset, 1, toRestore);
    			}
    			//move cursor to before the character we just replaced
    			Position newPos = editorAdaptor.getCursorService().newPositionForModelOffset(cursorOffset);
    			editorAdaptor.getCursorService().setPosition(newPos, StickyColumnPolicy.ON_CHANGE);
    			return true;
    		}
    		else if(cursorOffset < startCursorOffset) {
    			//backspace before our start position,
    			//just move the cursor (matches Vim behavior)
    			Position newPos = editorAdaptor.getCursorService().newPositionForModelOffset(cursorOffset);
    			editorAdaptor.getCursorService().setPosition(newPos, StickyColumnPolicy.ON_CHANGE);
    			return true;
    		}
    		else {
                //We don't have a character to restore, perform the backspace as
                //usual even though that may not be the desired behavior. This
                //can happen if someone pastes a register or executes a mapping.
                //The number of key strokes entered doesn't match the number of
                //characters inserted so we didn't get a chance to save off the
    			//original characters that should be restored.
    			return super.handleKey(stroke);
    		}
    	}
    	else { //grab the current char before replacing it
    		String toReplace = editorAdaptor.getModelContent().getText(cursorOffset, 1);
    		
    		//If inserting characters after a newline, there are no characters to
    		//restore.  We don't want to grab the characters from the next line
    		//(which is what the text at this offset would be).
    		if(!afterNewline) {
    			replacedChars.put(cursorOffset, toReplace);
    		}
    		
    		if(VimUtils.isNewLine(toReplace)) {
    			afterNewline = true;
    		}
    		
            return super.handleKey(stroke);
    	}
    }
    
    /**
     * Perform a replace rather than an insert.  This method is called when
     * replaying a macro that contains a replace because InsertMode's
     * handleVirtualStroke method would've performed an insert.  All of the
     * other logic in handleVirtualStroke is valid for both ReplaceMode and
     * InsertMode so I only need to tweak the actual insert (replace).
     */
    @Override
    protected void handleVirtualInsert(TextContent content, String str) {
        content.replace(editorAdaptor.getPosition().getModelOffset(), str.length(), str);
        //replace mode moves cursor after each character replaced
        editorAdaptor.setPosition( editorAdaptor.getPosition().addModelOffset(str.length()), StickyColumnPolicy.NEVER );
    }
    
    public static class ChangeToReplaceModeCommand extends ChangeToInsertModeCommand {
    	@Override
    	public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
    		editorAdaptor.changeMode(ReplaceMode.NAME, new WithCountHint(count));
    	}
    	
    	@Override
    	public CountAwareCommand repetition() {
    		return new RepeatReplaceCommand();
    	}
    }

    private static class RepeatReplaceCommand extends CountAwareCommand {

        public void execute(EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            editorAdaptor.getHistory().beginCompoundChange();
            TextContent modelContent = editorAdaptor.getModelContent();
            String editorNewline = editorAdaptor.getConfiguration().getNewLine();
            String text = editorAdaptor.getRegisterManager().getLastEditRegister().getContent().getText();
            int pos = editorAdaptor.getPosition().getModelOffset();
            if(count == NO_COUNT_GIVEN) {
            	count = 1;
            }
            for(int j=0; j < count; j++) {
            	int start = 0;
            	for (int i = 0; i < text.length(); i++) {
            		char c = text.charAt(i);
            		if (VimUtils.isNewLine(String.valueOf(c))) {
            			String replace = text.substring(start, i);
            			String nl = text.substring(i);
            			NewLine newline = NewLine.parse(nl);
            			replace(modelContent, pos, replace);
            			modelContent.replace(pos+replace.length(), 0, editorNewline);
            			i += newline.nl.length()-1;
            			pos += replace.length()+editorNewline.length();
            			start += replace.length()+newline.nl.length();
            		}
            	}
            	String replace = text.substring(start);
            	replace(modelContent, pos, replace);
            	//prepare for next iteration if count defined
            	pos += text.length();
            }
            editorAdaptor.getHistory().endCompoundChange();
        }

        private void replace(TextContent modelContent, int pos, String replace) {
            int length = replace.length();
            String toReplace = modelContent.getText(pos, length);
            for (int i = 0; i < length; i++) {
                String c = String.valueOf(toReplace.charAt(i));
                if (VimUtils.isNewLine(c)) {
                    length = i;
                }
            }
            modelContent.replace(pos, length, replace);
        }

		@Override
		public CountAwareCommand repetition() {
			//ChangeToReplaceModeCommand repeats, RepeatReplaceCommand doesn't
			//could this be implemented a little simpler?
			//(I made it complicated to support '.' command)
			return null;
		}
    }

}
