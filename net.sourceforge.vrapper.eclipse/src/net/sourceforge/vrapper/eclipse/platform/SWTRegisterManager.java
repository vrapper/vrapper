package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.vim.ConfigurationListener;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Display;

public class SWTRegisterManager extends DefaultRegisterManager {

    protected SWTClipboardRegister clipboardRegister;
    protected SWTClipboardRegister selectionClipboardRegister;

    public SWTRegisterManager(Display d, GlobalConfiguration globalConfig) {
        super();
        clipboardRegister = new SWTClipboardRegister(d, DND.CLIPBOARD);
        registers.put(RegisterManager.REGISTER_NAME_CLIPBOARD, clipboardRegister);

        // Support for X11 selection register only enabled on Linux.
        // Alias 'selection register' to the clipboard register for any other OS.
        if (Platform.OS_LINUX.equals(Platform.getOS())) {
            selectionClipboardRegister = new SWTClipboardRegister(d, DND.SELECTION_CLIPBOARD);
            registers.put(RegisterManager.REGISTER_NAME_SELECTION, selectionClipboardRegister);
        } else {
            registers.put(RegisterManager.REGISTER_NAME_SELECTION, clipboardRegister);
        }

        final ConfigurationListener listener = new ConfigurationListener() {
            @Override
            public <T> void optionChanged(final Option<T> option, final T oldValue, final T newValue) {
                if (Options.CLIPBOARD.equals(option)) {
                    Register oldDefault = defaultRegister;
                    if ("unnamed".equals(newValue)) {
                        defaultRegister = clipboardRegister;
                    } else {
                        defaultRegister = unnamedRegister;
                    }
                    // Reset active register
                    if (activeRegister == oldDefault) {
                        activeRegister = defaultRegister;
                    }
                }
            }
        };
        globalConfig.addListener(listener);
    }
}
