package de.jroene.vrapper.vim.register;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple implementation of {@link RegisterManager} which holds its registers in
 * a {@link Map}, and addresses them by their names.
 *
 * @author Matthias Radig
 */
public class DefaultRegisterManager implements RegisterManager {

    private static final String DEFAULT = DefaultRegisterManager.class
    .getCanonicalName() + ".DEFAULT_REGISTER";

    private final Map<String, Register> registers;
    private String activeRegister;

    public DefaultRegisterManager() {
        this.registers = new HashMap<String, Register>();
        this.activeRegister = DEFAULT;
    }

    public Register getRegister(String name) {
        if (!registers.containsKey(name)) {
            registers.put(name, new SimpleRegister());
        }
        return registers.get(name);
    }

    public Register getDefaultRegister() {
        return getRegister(DEFAULT);
    }

    public Register getActiveRegister() {
        return getRegister(activeRegister);
    }

    public void setActiveRegister(String name) {
        this.activeRegister = name;
    }

    public void activateDefaultRegister() {
        this.activeRegister = DEFAULT;
    }
}
