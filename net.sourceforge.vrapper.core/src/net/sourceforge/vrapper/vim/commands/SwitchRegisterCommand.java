package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.register.Register;

public class SwitchRegisterCommand extends CountIgnoringNonRepeatableCommand {

    private final char registerName;
    private final Register register;

    public SwitchRegisterCommand(char registerName) {
        super();
        this.registerName = registerName;
        register = null;
    }

    public SwitchRegisterCommand(Register register) {
        if (register == null) {
            throw new NullPointerException();
        }
        this.register = register;
        registerName = 0;
    }

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        if (register != null) {
            editorAdaptor.getRegisterManager().setActiveRegister(register);
        } else {
            editorAdaptor.getRegisterManager().setActiveRegister(String.valueOf(registerName));
        }
    }

}
