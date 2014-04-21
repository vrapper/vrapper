package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.GlobalConfiguration;
import net.sourceforge.vrapper.vim.ConfigurationListener;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.Register;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.eclipse.swt.widgets.Display;

public class SWTRegisterManager extends DefaultRegisterManager {

    protected SWTClipboardRegister clipboardRegister;

    public SWTRegisterManager(Display d, GlobalConfiguration globalConfig) {
        super();
        clipboardRegister = new SWTClipboardRegister(d);
        registers.put(RegisterManager.REGISTER_NAME_CLIPBOARD, clipboardRegister);
        registers.put(RegisterManager.REGISTER_NAME_SELECTION, clipboardRegister);

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
