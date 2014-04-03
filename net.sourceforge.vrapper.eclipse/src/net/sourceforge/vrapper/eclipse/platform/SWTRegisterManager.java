package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.swt.widgets.Display;

public class SWTRegisterManager extends DefaultRegisterManager {

	public SWTRegisterManager(Display d) {
		super();
        SWTClipboardRegister clipboardRegister = new SWTClipboardRegister(d);
        registers.put(RegisterManager.REGISTER_NAME_CLIPBOARD, clipboardRegister);
        registers.put(RegisterManager.REGISTER_NAME_SELECTION, clipboardRegister);
	}
}
