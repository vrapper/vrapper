package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.Rect;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Replaces the character at the current position with another one.
 *
 * @author Matthias Radig
 */
public abstract class ReplaceCommand extends AbstractModelSideCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        @Override
        public Command call(final KeyStroke arg) {
            if (SpecialKey.RETURN.equals(arg.getSpecialKey())) {
                return Newline.INSTANCE;
            }
            return new Character(arg.getCharacter());
        }
    };

    @Override
    protected int execute(final TextContent c, final int offset, final int count) {
        final LineInformation line = c.getLineInformationOfOffset(offset);
        final int targetOffset = offset + count - 1;
        if (targetOffset < line.getEndOffset()) {
            return replace(c, offset, count, targetOffset);
        }
        return offset;
    }

    abstract int replace(TextContent c, int offset, int count, int targetOffset);
    
    public static class Visual extends ReplaceCommand {
        private final char replaceChar;
        protected int selectionOffset;
        protected String selectionText;

        public Visual(final char replacement) {
            super();
            this.replaceChar = replacement;
        }

        @Override
        public void execute(final EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
        	//grab current selection
        	final TextRange selectionRange = editorAdaptor.getSelection().getRegion(editorAdaptor, 0);
        	selectionOffset = selectionRange.getLeftBound().getModelOffset();
        	selectionText = editorAdaptor.getModelContent().getText(selectionRange);
        	replace(editorAdaptor.getModelContent(), selectionOffset, 1, selectionOffset);
            editorAdaptor.changeMode(NormalMode.NAME);
        }

		@Override
        protected int replace(final TextContent c, final int offset, final int count, final int targetOffset) {
        	String s = "";
        	for(int i=0; i < selectionText.length(); i++) {
        		//replace every character *except* newlines
        		s += VimUtils.isNewLine(""+selectionText.charAt(i)) ? selectionText.charAt(i) : replaceChar;
        	}
            c.replace(selectionOffset, selectionText.length(), s);
            return selectionOffset;
        }
        
        public static final Function<Command, KeyStroke> VISUAL_KEYSTROKE = new Function<Command, KeyStroke>() {
        	@Override
            public Command call(final KeyStroke arg) {
        		return new Visual(arg.getCharacter());
        	}
        };
    
    }
    
    public static class VisualBlock extends Visual {
        
        public VisualBlock(final char replacement) {
            super(replacement);
        }
        
        @Override
        public void execute(final EditorAdaptor editorAdaptor) throws CommandExecutionException {
            
            final HistoryService history = editorAdaptor.getHistory();
            history.beginCompoundChange();
            history.lock("block-action");
            
            final TextContent textContent = editorAdaptor.getModelContent();
            final Rect rect = BlockWiseSelection.getRect(textContent, editorAdaptor.getSelection());
            final int selectionStart = rect.getULPosition(editorAdaptor).getModelOffset();
            
            final int height = rect.height();
            final int width = rect.width();
            for (int i=0; i < height; i++) {
                final Position ul = rect.getULPosition(editorAdaptor);
                selectionText = textContent.getText(SelectionBasedTextOperationCommand.newRange(ul, width));
                selectionOffset = ul.getModelOffset();
            	replace(textContent, ul.getModelOffset(), 1, selectionStart);
                rect.top++;
            }
            
            history.unlock("block-action");
            history.endCompoundChange();
            
            editorAdaptor.changeMode(NormalMode.NAME);
        }
        
        public static final Function<Command, KeyStroke> VISUALBLOCK_KEYSTROKE = new Function<Command, KeyStroke>() {
        	@Override
            public Command call(final KeyStroke arg) {
        		return new VisualBlock(arg.getCharacter());
        	}
        };
    }

    private static class Character extends ReplaceCommand {

        private final char replacement;

        public Character(final char replacement) {
            super();
            this.replacement = replacement;
        }

        @Override
        protected int replace(final TextContent c, final int offset, final int count, final int targetOffset) {
            final String s = StringUtils.multiply(""+replacement, count);
            c.replace(offset, s.length(), s);
            return targetOffset;
        }
    }

    private static class Newline extends ReplaceCommand {

        private static final Newline INSTANCE = new Newline();

        private String newLine;
        private boolean smartIndent;
        private boolean autoIndent;

        @Override
        public void execute(final EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            // hack to access the configuration
            final Configuration conf = editorAdaptor.getConfiguration();
            newLine = conf.getNewLine();
            smartIndent = conf.get(Options.SMART_INDENT);
            autoIndent = conf.get(Options.AUTO_INDENT);
            super.execute(editorAdaptor);
        }

        @Override
        int replace(final TextContent c, final int offset, final int count, final int targetOffset) {
            final LineInformation line = c.getLineInformationOfOffset(offset);
            if (smartIndent) {
                c.replace(offset, targetOffset-offset+1, "");
                c.smartInsert(offset, newLine);
            } else {
                final String indent = autoIndent ? VimUtils.getIndent(c, line) : "";
                c.replace(offset, targetOffset-offset+1, newLine+indent);
            }
            final LineInformation nextLine = c.getLineInformation(line.getNumber()+1);
            return VimUtils.getFirstNonWhiteSpaceOffset(c, nextLine);
        }

    }

}
