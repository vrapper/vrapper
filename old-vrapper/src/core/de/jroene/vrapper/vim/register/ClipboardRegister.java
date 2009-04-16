package de.jroene.vrapper.vim.register;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;

import de.jroene.vrapper.vim.VimConstants;

public class ClipboardRegister implements Register {

    public RegisterContent getContent() {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        for (DataFlavor df : c.getAvailableDataFlavors()) {
            if (df.isFlavorTextType()) {
                String s;
                try {
                    s = (String) c.getContents(df).getTransferData(df);
                    if (s.endsWith(VimConstants.REGISTER_NEWLINE)
                            || s.startsWith(VimConstants.REGISTER_NEWLINE)) {
                        return new RegisterContent(true, s.trim());
                    }
                    return new RegisterContent(false, s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return RegisterContent.DEFAULT_CONTENT;
    }

    public void setContent(RegisterContent content) {
        Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
        String s = content.getPayload();
        if (content.isLineWise()) {
            s += VimConstants.REGISTER_NEWLINE;
        }
        c.setContents(new StringSelection(s), null);
    }

}
