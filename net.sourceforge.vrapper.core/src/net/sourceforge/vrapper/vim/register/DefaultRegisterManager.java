package net.sourceforge.vrapper.vim.register;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;


/**
 * Simple implementation of {@link RegisterManager} which holds its registers in
 * a {@link Map}, and addresses them by their names.
 *
 * @author Matthias Radig
 */
public class DefaultRegisterManager implements RegisterManager {

    protected final Map<String, Register> registers;
    private Register activeRegister;
    private final Register defaultRegister;
    private final Register lastEditRegister;
    private Search search;
    private Command lastEdit;
    private FindMotion findMotion;

    public DefaultRegisterManager() {
        this.registers = new HashMap<String, Register>();
        this.defaultRegister = new SimpleRegister();
        this.lastEditRegister = new SimpleRegister();
        this.activeRegister = defaultRegister;
        Register lastInsertRegister = new ReadOnlyRegister() {
            public RegisterContent getContent() {
                return lastEditRegister.getContent();
            }
        };
        registers.put(".", lastInsertRegister);
        Register searchRegister = new ReadOnlyRegister() {
            public RegisterContent getContent() {
                return new StringRegisterContent(ContentType.TEXT, search.getKeyword());
            }
        };
        registers.put("/", searchRegister);
        // FIXME: AWTClipboardRegister is obviously underlying platform dependency
        registers.put("*", new AWTClipboardRegister());
    }

    public Register getRegister(String name) {
        String key = name.toLowerCase();
        if (!registers.containsKey(key)) {
            registers.put(key, new NamedRegister(defaultRegister));
        }
        Register r = registers.get(key);
        if (!name.equals(key)) {
            r = new AppendRegister(r);
        }
        return r;
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

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    public Command getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Command lastEdit) {
        this.lastEdit = lastEdit;
    }

    public FindMotion getLastFindMotion() {
        return findMotion;
    }

    public void setLastFindMotion(FindMotion motion) {
        findMotion = motion;
    }

    public void setActiveRegister(Register register) {
        activeRegister = register;
    }

    public boolean isDefaultRegisterActive() {
        return activeRegister == defaultRegister;
    }

}
