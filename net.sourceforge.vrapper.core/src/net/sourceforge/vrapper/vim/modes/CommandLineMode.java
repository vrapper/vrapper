package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyStroke;

public class CommandLineMode implements EditorMode {


	public static final String NAME = "command mode";

	@Override
	public void enterMode() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void leaveMode() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public boolean handleKey(KeyStroke stroke) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not yet implemented");
	}

}
