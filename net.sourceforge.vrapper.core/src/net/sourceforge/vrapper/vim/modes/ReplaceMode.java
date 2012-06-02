package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;

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
        super.enterMode(args);
    }

    @Override
    public void leaveMode(ModeSwitchHint... hints) {
        editorAdaptor.getEditorSettings().setReplaceMode(false);
        super.leaveMode();
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
