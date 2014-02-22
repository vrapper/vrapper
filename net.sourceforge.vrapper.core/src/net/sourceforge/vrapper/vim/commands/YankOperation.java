package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.RegisterManager;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class YankOperation extends SimpleTextOperation {

    public static final YankOperation INSTANCE = new YankOperation(null);
    
    private final String register;
    private final boolean updateCursor;

    YankOperation(String register) {
        this(register, true);
    }

    YankOperation(String register, boolean updateCursor) {
        this.register = register;
        this.updateCursor = updateCursor;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
        if (register != null) {
            RegisterManager registerManager = editorAdaptor.getRegisterManager();
            registerManager.setActiveRegister(register);
        }
        doIt(editorAdaptor, region, contentType, true, updateCursor);
    }

    public TextOperation repetition() {
        return null;
    }

    public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType, boolean setLastYank) {
        doIt(editorAdaptor, range, contentType, setLastYank, true);
    }

    public static void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType, boolean setLastYank, boolean updateCursor) {
    	if(range == null) {
    		return;
    	}
    	
        String text = editorAdaptor.getModelContent().getText(range.getLeftBound().getModelOffset(), range.getModelLength());
        //if we're expecting lines and this text doesn't end in a newline,
        //manually append a newline to the end
        //(this to handle yanking the last line of a file)
        if (contentType == ContentType.LINES && (text.length() == 0 || ! VimUtils.isNewLine(text.substring(text.length()-1)))) {
            text += editorAdaptor.getConfiguration().getNewLine();
        }
        
        CursorService cur = editorAdaptor.getCursorService();
        cur.setMark(CursorService.LAST_CHANGE_START, range.getLeftBound());
        int exclude = VimUtils.endsWithNewLine(text);
        if (exclude == 0) {
            exclude = 1;
        }
        cur.setMark(CursorService.LAST_CHANGE_END, range.getRightBound().addModelOffset(-exclude));
        
        RegisterContent content = new StringRegisterContent(contentType, text);
        
        if (contentType == ContentType.LINES && NormalMode.NAME.equals(editorAdaptor.getCurrentModeName())) {
            //if this is line-wise, move cursor to first line in selection but keep stickyColumn
        	//(keep stickyColumn for the 'yy' case)
            int lineNo = editorAdaptor.getModelContent().getLineInformationOfOffset(range.getLeftBound().getModelOffset()).getNumber();
            Position stickyPosition = editorAdaptor.getCursorService().stickyColumnAtModelLine(lineNo);
            if (updateCursor) {
                editorAdaptor.getCursorService().setPosition(stickyPosition, StickyColumnPolicy.ON_CHANGE);
            }
        }
        else {
            if (contentType == ContentType.TEXT_RECTANGLE) {
                content = BlockWiseSelection.getTextBlockContent(editorAdaptor, range);
                editorAdaptor.setPosition(range.getLeftBound(), StickyColumnPolicy.ON_CHANGE);
            } else {
                Position cursor = editorAdaptor.getCursorService().getPosition();
                Position newPos = range.getLeftBound();
                //if cursor is at beginning of selection, leave it there
                if(cursor.getModelOffset() != newPos.getModelOffset()) {
                    //move cursor to beginning of selection
                    cur.setPosition(newPos, StickyColumnPolicy.ON_CHANGE);
                }
            }
        }

        RegisterManager registerManager = editorAdaptor.getRegisterManager();
        registerManager.getActiveRegister().setContent(content);
        if(setLastYank && registerManager.isDefaultRegisterActive()) {
        	registerManager.setLastYank(content);
        }
    }

}
