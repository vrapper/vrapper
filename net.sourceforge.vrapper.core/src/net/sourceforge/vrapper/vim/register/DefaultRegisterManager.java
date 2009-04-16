package net.sourceforge.vrapper.vim.register;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.motions.Motion;


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
//    private Search search;
	private Command lastEdit;
	private Motion forwardMotion;
	private Motion backwardMotion;

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
//        Register searchRegister = new ReadOnlyRegister() {
//            public RegisterContent getContent() {
//                return new StringRegisterContent(ContentType.TEXT, search.getKeyword());
//            }
//        };
//        registers.put("/", searchRegister);
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

//    public Search getSearch() {
//        return search;
//    }
//
//    public void setSearch(Search search) {
//        this.search = search;
//    }
//
	@Override
	public Motion getForwardMotion() {
		return forwardMotion;
	}

	@Override
	public Motion getBackwardMotion() {
		return backwardMotion;
	}

	@Override
	public void setMotionPair(Motion forwardMotion, Motion backwardMotion) {
		this.forwardMotion = forwardMotion;
		this.backwardMotion = backwardMotion;
	}

	@Override
	public Command getLastEdit() {
		return lastEdit;
	}

	@Override
	public void setLastEdit(Command lastEdit) {
		this.lastEdit = lastEdit;
	}

}
