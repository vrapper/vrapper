package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;

/**
 * Provides access to different registers.
 *
 * @author Matthias Radig
 */
public interface RegisterManager {

    Register getRegister(String name);
    Register getDefaultRegister();
    Register getActiveRegister();
    Register getLastEditRegister();
    void setActiveRegister(String name);
    void setActiveRegister(Register register);
    void activateDefaultRegister();
    void activateLastEditRegister();
    Command getLastEdit();
    void setLastEdit(Command edit);
    void setLastFindMotion(FindMotion motion);
    FindMotion getLastFindMotion();
    Search getSearch();
    void setSearch(Search search);
    boolean isDefaultRegisterActive();
}
