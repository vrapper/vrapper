package net.sourceforge.vrapper.eclipse.platform;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.StringRegisterContent;
import net.sourceforge.vrapper.vim.register.TextBlockRegisterContent;

import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;

/**
 * Text block register converter to store in system clipboard.
 */
class RegisterSelection extends ByteArrayTransfer {
    private static final String REGISTER = "text/vrapper-block-selection";
    private static final int TYPEID = registerType(REGISTER);
    private static RegisterSelection _instance = new RegisterSelection();

    public static RegisterSelection getInstance() {
        return _instance;
    }

    public void javaToNative(Object object, TransferData transferData) {
        if (object == null || !(object instanceof TextBlockRegisterContent))
            return;
        if (isSupportedType(transferData)) {
            TextBlockRegisterContent registerContent = (TextBlockRegisterContent) object;
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                DataOutputStream writeOut = new DataOutputStream(out);
                writeOut.writeInt(registerContent.getPayloadType().ordinal());
                writeOut.writeInt(registerContent.getVisualWidth());
                writeOut.writeInt(registerContent.getNumLines());
                for (int j = 0; j < registerContent.getNumLines(); j++) {
                    byte[] buffer = registerContent.getLine(j).getBytes();
                    writeOut.writeInt(buffer.length);
                    writeOut.write(buffer);
                }
                byte[] buffer = out.toByteArray();
                writeOut.close();
                super.javaToNative(buffer, transferData);

            } catch (IOException e) {
            }
        }
    }

    public Object nativeToJava(TransferData transferData) {
        TextBlockRegisterContent registerContent = null;
        if (isSupportedType(transferData)) {
            byte[] buffer = (byte[]) super.nativeToJava(transferData);
            if (buffer == null)
                return null;
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(buffer);
                DataInputStream readIn = new DataInputStream(in);
                ContentType ct = ContentType.values()[readIn.readInt()];
                int vOffset = readIn.readInt();
                int lines = readIn.readInt();
                registerContent = new TextBlockRegisterContent(vOffset);
                for (int i = 0; i < lines; ++i) {
                    int len = readIn.readInt();
                    byte[] line = new byte[len];
                    readIn.read(line);
                    registerContent.appendLine(new String(line));
                }
                readIn.close();
            } catch (IOException ex) {
                return null;
            }
        }
        return registerContent;
    }

    @Override
    protected String[] getTypeNames() {
        return new String[] {REGISTER};
    }

    @Override
    protected int[] getTypeIds() {
        return new int[] {TYPEID};
    }
}

public class SWTClipboardRegister implements Register {

    private Clipboard clipboard;
    private int clipboardId;

    public SWTClipboardRegister(Display d, int clipboardId) {
        this.clipboardId = clipboardId;
        clipboard = new Clipboard(d);
    }

    public RegisterContent getContent() {
        TextBlockRegisterContent registerContent = (TextBlockRegisterContent) clipboard
                .getContents(RegisterSelection.getInstance());
        if (registerContent != null) {
            return registerContent;
        }
        String s = (String) clipboard.getContents(TextTransfer.getInstance(), clipboardId);
        if (s == null) {
            return RegisterContent.DEFAULT_CONTENT;
        }
        // don't assume copied text matches OS newline type
        if (s.length() > 0
                &&
                (VimUtils.isNewLine("" + s.charAt(0)) || VimUtils.isNewLine(""
                        + s.charAt(s.length() - 1)))) {
            return new StringRegisterContent(ContentType.LINES, s);
        }
        return new StringRegisterContent(ContentType.TEXT, s);
    }

    public void setContent(RegisterContent content) {
        ContentType contentType = content.getPayloadType();
        if (contentType == ContentType.TEXT_RECTANGLE) {
            clipboard.setContents(new Object[] {content},
                    new Transfer[] {RegisterSelection.getInstance()},
                    clipboardId);
        } else {
            String s = content.getText();
            clipboard.setContents(new Object[] {s},
                    new Transfer[] {TextTransfer.getInstance()},
                    clipboardId);
        }
    }

}
