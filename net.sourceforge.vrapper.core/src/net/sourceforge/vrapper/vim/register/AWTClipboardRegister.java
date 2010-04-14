package net.sourceforge.vrapper.vim.register;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.vim.VimConstants;

// XXX: we're mixing SWT with AWT here
// it's a little bit evil ;-)
public class AWTClipboardRegister implements Register {

    public RegisterContent getContent() {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        DataFlavor df = getDataFlavor(c);
        if (df != null) {
            String s;
            try {
                s = (String) c.getContents(df).getTransferData(df);
                if (s.endsWith(VimConstants.REGISTER_NEWLINE)
                        || s.startsWith(VimConstants.REGISTER_NEWLINE)) {
                    return new StringRegisterContent(ContentType.LINES, s.trim()+VimConstants.REGISTER_NEWLINE);
                }
                return new StringRegisterContent(ContentType.TEXT, s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return RegisterContent.DEFAULT_CONTENT;
    }

    private DataFlavor getDataFlavor(Clipboard c) {
        if (c.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            return DataFlavor.stringFlavor;
        } else {
            for (DataFlavor f : c.getAvailableDataFlavors()) {
                if (f.isFlavorTextType()) {
                    return f;
                }
            }
        }
        return null;
    }

    public void setContent(RegisterContent content) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        String s = content.getText();
        if (content.getPayloadType() == ContentType.LINES) {
            s += VimConstants.REGISTER_NEWLINE;
        }
        c.setContents(new StringSelection(s), null);
    }

}
