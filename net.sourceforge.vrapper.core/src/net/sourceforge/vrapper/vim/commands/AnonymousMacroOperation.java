package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.vim.ConstructorWrappers;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Immediately execute a set of commands without storing them
 * in a named register.
 */
public class AnonymousMacroOperation extends SimpleTextOperation {
	
	private String macro;
	
	public AnonymousMacroOperation(String macro) {
		this.macro = macro;
	}

	public TextOperation repetition() {
		return null;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange region,
			ContentType contentType) throws CommandExecutionException {
		
		//put cursor at beginning of the region
		editorAdaptor.getCursorService().setPosition(region.getLeftBound(), false);
		
		//set macro
		Iterable<KeyStroke> parsed = ConstructorWrappers.parseKeyStrokes(macro);
		editorAdaptor.getMacroPlayer().add(parsed);
		
		//run macro
		editorAdaptor.getMacroPlayer().play();
	}

}
