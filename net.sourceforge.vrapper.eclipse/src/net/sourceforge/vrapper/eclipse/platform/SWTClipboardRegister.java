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
        if (s.endsWith(VimConstants.REGISTER_NEWLINE)
                || s.startsWith(VimConstants.REGISTER_NEWLINE)) {
            return new StringRegisterContent(ContentType.LINES, normalizeLineBreaks(s));
        }
        return new StringRegisterContent(ContentType.TEXT, s);
    }

    public void setContent(RegisterContent content) {
        String s = content.getText();
        if (content.getPayloadType() == ContentType.LINES) {
            s += VimConstants.REGISTER_NEWLINE;
        }
        clipboard.setContents(new Object[] { s }, new Transfer[] { TextTransfer.getInstance() });
    }
    
    private String normalizeLineBreaks(String s) {
    	// remove leading and trailing line breaks, the add a single line break at the end
    	return s.replaceAll("^[\\n\\r]|[\\n\\r]$", "")+VimConstants.REGISTER_NEWLINE;
    }

}
