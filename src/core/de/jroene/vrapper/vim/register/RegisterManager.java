package de.jroene.vrapper.vim.register;

/**
 * Provides access to different registers.
 *
 * @author Matthias Radig
 */
public interface RegisterManager {

    Register getRegister(String name);
    Register getDefaultRegister();
    Register getActiveRegister();
    void setActiveRegister(String name);
    void activateDefaultRegister();
}
