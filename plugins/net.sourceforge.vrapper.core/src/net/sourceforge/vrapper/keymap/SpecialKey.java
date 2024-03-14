package net.sourceforge.vrapper.keymap;

public enum SpecialKey {
	ESC, ARROW_LEFT, ARROW_RIGHT, ARROW_DOWN, ARROW_UP, RETURN, HOME, END, PAGE_UP, PAGE_DOWN,
	INSERT, DELETE, BACKSPACE, TAB,
	F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12, F13, F14, F15, F16, F17, F18, F19, F20,
	/**
	 * This key is for use in the "trigger" side of a remap. The mapping code will recognize it if
	 * the configured key has been entered.
	 */
	LEADER,
	/**
	 * Special key which is always sent before a sequence of characters identifying a plugin
	 * function.
	 * <p>
	 * <b>NOTE</b>: this is reserved for PlugKeyStroke instances, it should never be used
	 * by the
	 * input logic.
	 */
	PLUG;
}
