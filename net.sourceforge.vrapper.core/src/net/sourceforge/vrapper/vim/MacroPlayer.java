package net.sourceforge.vrapper.vim;

import java.util.LinkedList;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.vim.commands.PlaybackMacroCommand;
import net.sourceforge.vrapper.vim.modes.EditorMode;

/**
 * Handles playback of user-recorded macros.
 * The playlist is filled by {@link PlaybackMacroCommand}. Once the keystrokes
 * invoking the macro are handled, the {@link EditorAdaptor} starts the playback.
 * This is necessary because executing macros from a command would mean that
 * an {@link EditorMode}'s {@link EditorMode#handleKey(KeyStroke)} method
 * would be called recursivly, which results in undefined behaviour.
 *
 * @author Matthias Radig
 */
public class MacroPlayer {

    private final Queue<KeyStroke> playlist;
    private final EditorAdaptor editorAdaptor;

    MacroPlayer (EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
        playlist = new LinkedList<KeyStroke>();
    }

    /**
     * Adds a key stroke to the playlist. May be called by commands.
     */
    public void add(KeyStroke stroke) {
        playlist.add(new RemappedKeyStroke(stroke, true));
    }

    /**
     * Adds a list of keystrokes to the playlist. May be called by commands.
     */
    public void add(Iterable<KeyStroke> macro) {
        for (KeyStroke stroke : macro) {
            playlist.add(new RemappedKeyStroke(stroke, true));
        }
    }

    /**
     * Executes what is in the playlist. Should only be called by the parent
     * {@link EditorAdaptor}.
     */
    public void play() {
        ViewportService view = editorAdaptor.getViewportService();
        try {
            view.setRepaint(false);
            view.lockRepaint(this);
            while (!playlist.isEmpty()) {
                editorAdaptor.handleKeyOffRecord(playlist.poll());
            }
        } finally {
            view.unlockRepaint(this);
            view.setRepaint(true);
        }
    }
}
