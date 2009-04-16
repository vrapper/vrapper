package newpackage.vim.register;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import kg.totality.core.utils.ContentType;
import de.jroene.vrapper.vim.VimConstants;

// XXX: we're mixing SWT with AWT here :->
// it's a little bit evil ;-)
public class AWTClipboardRegister implements Register {

	@Override
    public RegisterContent getContent() {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        for (DataFlavor df : c.getAvailableDataFlavors()) {
            if (df.isFlavorTextType()) {
                String s;
                try {
                    s = (String) c.getContents(df).getTransferData(df);
                    if (s.endsWith(VimConstants.REGISTER_NEWLINE)
                            || s.startsWith(VimConstants.REGISTER_NEWLINE)) {
                        return new StringRegisterContent(ContentType.LINES, s.trim());
                    }
                    return new StringRegisterContent(ContentType.TEXT, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return RegisterContent.DEFAULT_CONTENT;
    }

	@Override
    public void setContent(RegisterContent content) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        String s = content.getText();
        if (content.getPayloadType() == ContentType.LINES) {
            s += VimConstants.REGISTER_NEWLINE;
        }
        c.setContents(new StringSelection(s), null);
    }

}
