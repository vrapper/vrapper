package net.sourceforge.vrapper.vim.modes;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.KeyMapProvider;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.modes.commandline.AbstractCommandParser;
import net.sourceforge.vrapper.vim.register.Register;

/**
 * When you're in InsertMode, you can hit Ctrl+R and it will ask for the
 * name of a register.  As soon as you enter a register name, it will paste
 * the contents of that register and keep you in InsertMode.
 *
 * This class exists just for the "ask for the name of a register" part.
 * The command line will appear with '"' and the user enters a single character.
 * As soon as that character is entered, we dump the contents of that register
 * without waiting for the user to hit 'enter' or anything else.
 */
public class PasteRegisterMode extends AbstractCommandLineMode {

	public static final String DISPLAY_NAME = "PASTE REGISTER";
    public static final String NAME = "paste register";

	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	public String getName() {
		return NAME;
	}
	
    public PasteRegisterMode(EditorAdaptor editorAdaptor) {
		super(editorAdaptor);
	}

	public KeyMap resolveKeyMap(KeyMapProvider provider) {
		return null;
	}

	@Override
	protected char activationChar() {
		return '"';
	}

	@Override
	protected AbstractCommandParser createParser() {
		return new PasteRegisterParser(editorAdaptor);
	}
	
	private class PasteRegisterParser extends AbstractCommandParser {

		public PasteRegisterParser(EditorAdaptor vim) {
			super(vim);
		}
		
		@Override
		public void type(KeyStroke e) {
            if (e.equals(KEY_RETURN) || e.equals(KEY_ESCAPE) || e.equals(KEY_CTRL_C)) {
            	editor.changeModeSafely(InsertMode.NAME);
            	return;
            }
            
            buffer.append(e.getCharacter());
            //first character is '"', take the second character and use it
            if(buffer.length() > 1) {
            	parseAndExecute("\"", ""+e.getCharacter());
            	//return to insert mode
            	editor.changeModeSafely(InsertMode.NAME);
            }
		}

		@Override
		public Command parseAndExecute(String first, String command) {
			//Ideally, I would just send the register's contents to the PasteOperation;
			//but that requires a TextObject and I don't know how to convert the contents
			//of the register into a TextObject.  So instead, I'm actually performing
			//most of what the PasteOperation would do if I could use it.
			Register reg = editor.getRegisterManager().getRegister(command);
			String text = reg.getContent().getText();
			
			if(text.length() > 0) {
				TextContent content = editor.getModelContent();
				int offset = editor.getCursorService().getPosition().getModelOffset();
				int position = offset + text.length() - 1;
				content.replace(offset, 0, text);
				Position destination = editorAdaptor.getCursorService().newPositionForModelOffset(position);
				editorAdaptor.setPosition(destination, true);
			}
			return null;
		}
		
	}

}
