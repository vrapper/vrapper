package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;

public interface EditorMode {
	String getName();
	void enterMode();
	void leaveMode();
	boolean handleKey(KeyStroke stroke);
}
