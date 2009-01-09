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

    private final Map<String, Register> registers;
    private Register activeRegister;
    private final Register defaultRegister;
    private final Register lastEditRegister;

    public DefaultRegisterManager() {
        this.registers = new HashMap<String, Register>();
        this.defaultRegister = new SimpleRegister();
        this.lastEditRegister = new SimpleRegister();
        this.activeRegister = defaultRegister;
    }

    public Register getRegister(String name) {
        if (!registers.containsKey(name)) {
            registers.put(name, new SimpleRegister());
        }
        return registers.get(name);
    }

    public Register getDefaultRegister() {
        return defaultRegister;
    }

    public Register getActiveRegister() {
        return activeRegister;
    }

    public void setActiveRegister(String name) {
        this.activeRegister = getRegister(name);
    }

    public void activateDefaultRegister() {
        this.activeRegister = defaultRegister;
    }

    public void activateLastEditRegister() {
        this.activeRegister = lastEditRegister;
    }

    public Register getLastEditRegister() {
        return lastEditRegister;
    }
}
