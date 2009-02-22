package de.jroene.vrapper.vim.register;

import java.util.HashMap;
import java.util.Map;

import de.jroene.vrapper.vim.Search;
import de.jroene.vrapper.vim.token.FindMove;
import de.jroene.vrapper.vim.token.Token;

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
    private FindMove lastCharSearch;
    private Token lastEdit;
    private Search search;

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
                return new RegisterContent(false, search.getKeyword());
            }
        };
        registers.put("/", searchRegister);
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

    public Token getLastEdit() {
        return lastEdit;
    }

    public void setLastEdit(Token lastChange) {
        this.lastEdit = lastChange;
    }

    public FindMove getLastCharSearch() {
        return (FindMove) lastCharSearch.clone();
    }

    public void setLastCharSearch(FindMove findMove) {
        this.lastCharSearch = (FindMove) findMove.clone();
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }
}
