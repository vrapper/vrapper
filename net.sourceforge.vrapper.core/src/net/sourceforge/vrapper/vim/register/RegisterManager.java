package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

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
    void activateDefaultRegister();
    void activateLastEditRegister();
    Command getLastEdit();
    void setLastEdit(Command edit);
    Motion getForwardMotion();
    Motion getBackwardMotion();
    void setMotionPair(Motion forward, Motion backward);
//    Search getSearch();
//    void setSearch(Search search);
}
