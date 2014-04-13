package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public class DeleteOperation extends SimpleTextOperation {

    public static final DeleteOperation INSTANCE = new DeleteOperation();

    private DeleteOperation() { /* NOP */ }

    @Override
    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
        if (region == null || (region.getModelLength() == 0 && contentType != ContentType.LINES)) {
            // Nothing to delete.
            return;
        }

        try {
            editorAdaptor.getHistory().beginCompoundChange();

            //if we're in LINES mode but the text doesn't end in a newline
            //try to include the previous newline character
            //(this is mostly to handle the last line of a file)
            TextContent txtContent = editorAdaptor.getModelContent();
            int position = region.getLeftBound().getModelOffset();
            int length = region.getModelLength();
            String text = txtContent.getText(position, length);
            if (contentType == ContentType.LINES && position > 0
                    && (text.length() == 0 || ! VimUtils.isNewLine(text.substring(text.length()-1)))) {
                LineInformation line = txtContent.getLineInformationOfOffset(position);
                int previousNewlinePos = txtContent.getLineInformation(line.getNumber() - 1).getEndOffset();
                //include the previous newline by moving back a few characters (1 or 2)
                Position newPosition = region.getLeftBound().addModelOffset(-(position - previousNewlinePos));
                region = new StartEndTextRange(newPosition, region.getRightBound());
            }
            doIt(editorAdaptor, region, contentType);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public TextOperation repetition() {
        return this;
    }

    public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
    	if(range == null) {
    		return;
    	}
    	
        YankOperation.doIt(editorAdaptor, range, contentType, false);
        RegisterManager registerManager = editorAdaptor.getRegisterManager();
        if(registerManager.isDefaultRegisterActive()) {
        	//get what YankOperation just set
        	RegisterContent register = registerManager.getActiveRegister().getContent();
        	registerManager.setLastDelete(register);
        }

        TextContent txtContent = editorAdaptor.getModelContent();
        CursorService cur = editorAdaptor.getCursorService();
        int position = range.getLeftBound().getModelOffset();
        int length = range.getModelLength();
        
        txtContent.replace(position, length, "");
        
        if (contentType == ContentType.LINES) {
            // move cursor on indented position
            // this is Vim-compatible, but does everyone really want this?
            // FIXME: make this an option
            LineInformation lastLine = txtContent.getLineInformationOfOffset(position);
            int indent = VimUtils.getIndent(txtContent, lastLine).length();
            int offset = lastLine.getBeginOffset() + indent;
            cur.setPosition(cur.newPositionForModelOffset(offset), StickyColumnPolicy.ON_CHANGE);
        } else { // fix sticky column
            cur.setPosition(cur.getPosition(), StickyColumnPolicy.ON_CHANGE);
        }
    }
}
