package de.jroene.vrapper.vim;

/**
 * Insert Mode of the vim emulator. Returns to normal mode on press of escape.
 * Does not do anything else. (Keystrokes will be passed to the underlying editor)
 *
 * @author Matthias Radig
 */
public class InsertMode extends AbstractMode {

    public InsertMode(VimEmulator vim) {
        super(vim);
    }
    public boolean type(VimInputEvent e) {
        if(VimInputEvent.ESCAPE.equals(e)) {
            Platform p = vim.getPlatform();
            LineInformation line = p.getLineInformation();
            int position = p.getPosition();
            if(position > line.getBeginOffset()) {
                p.setPosition(position-1);
            }
            vim.toNormalMode();
        }
        return true;
    }

}
