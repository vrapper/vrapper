package net.sourceforge.vrapper.eclipse.platform;


import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;

public class SWTClipboardRegister implements Register {
	
	private Clipboard clipboard;
	
	public SWTClipboardRegister(Display d) {
		clipboard = new Clipboard(d);
	}

    public RegisterContent getContent() {
    	String s = (String) clipboard.getContents(TextTransfer.getInstance());
    	if (s == null) {
    		return RegisterContent.DEFAULT_CONTENT;
    	}
        if (s.endsWith(VimConstants.REGISTER_NEWLINE)
                || s.startsWith(VimConstants.REGISTER_NEWLINE)) {
            return new StringRegisterContent(ContentType.LINES, s);
        }
        return new StringRegisterContent(ContentType.TEXT, s);
    }

    public void setContent(RegisterContent content) {
        String s = content.getText();
        clipboard.setContents(new Object[] { s }, new Transfer[] { TextTransfer.getInstance() });
    }

}
