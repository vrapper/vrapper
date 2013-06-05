package net.sourceforge.vrapper.vim.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.SelectionArea;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.TextOperation;
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
    private Register unnamedRegister;
    private Register defaultRegister;
    private final Register lastEditRegister;
    private Search search;
    private Command lastEdit, lastInsertion;
    private TextOperation lastSubstitution;
    private FindMotion findMotion;
	private SelectionArea lastActiveSelectionArea;
	private Selection lastActiveSelection;
	private String cwd = "/";

    public DefaultRegisterManager() {
        this.registers = new HashMap<String, Register>();
        this.unnamedRegister = new SimpleRegister();
        this.defaultRegister = unnamedRegister;
        this.lastEditRegister = new SimpleRegister();
        this.activeRegister = defaultRegister;
        // ""
        registers.put(RegisterManager.REGISTER_NAME_UNNAMED, unnamedRegister);
        // ".
        Register lastInsertRegister = new ReadOnlyRegister() {
            public RegisterContent getContent() {
                return lastEditRegister.getContent();
            }
        };
        registers.put(RegisterManager.REGISTER_NAME_INSERT, lastInsertRegister);
        // "/
        Register searchRegister = new ReadOnlyRegister() {
            public RegisterContent getContent() {
            	if(search == null) {
            		return RegisterContent.DEFAULT_CONTENT;
            	}
            	else {
            		return new StringRegisterContent(ContentType.TEXT, search.getKeyword());
            	}
            }
        };
        registers.put(RegisterManager.REGISTER_NAME_SEARCH, searchRegister);
        
        // "_
        // (Unmodifiable register which will protect the default register from being updated)
        Register blackholeRegister = new Register() {
            public RegisterContent getContent() {
                return RegisterContent.DEFAULT_CONTENT;
            }

            public void setContent(RegisterContent content) {
                // This register shouldn't stay active, otherwise the first "default paste" returns
                // nothing when it should simply return the content of the default register.
                activateDefaultRegister();
            }
        };
        registers.put(RegisterManager.REGISTER_NAME_BLACKHOLE, blackholeRegister);
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

    public void setDefaultRegister(Register register) {
        this.defaultRegister = register;
        registers.put(RegisterManager.REGISTER_NAME_UNNAMED, register);
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
    
    public void setLastNamedRegister(Register register) {
    	//update the '@@' macro
    	registers.put(REGISTER_NAME_LAST, register);
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

	public void setLastActiveSelection(Selection selection, SelectionArea selectionArea) {
		lastActiveSelection = selection;
		lastActiveSelectionArea = selectionArea;
	}

	public SelectionArea getLastActiveSelectionArea() {
		return lastActiveSelectionArea;
	}
	
	public Selection getLastActiveSelection() {
	    return lastActiveSelection;
	}

    public void setLastInsertion(Command command) {
        lastInsertion = command;
    }

    public Command getLastInsertion() {
        return lastInsertion;
    }

    public void setLastSubstitution(TextOperation operation) {
        lastSubstitution = operation;
    }

    public TextOperation getLastSubstitution() {
        return lastSubstitution;
    }
    
    public void setLastYank(RegisterContent register) {
    	getRegister("0").setContent(register);
    }
    
    public void setLastDelete(RegisterContent register) {
    	if( ! VimUtils.containsNewLine(register.getText())) {
    		getRegister(REGISTER_SMALL_DELETE).setContent(register);
    	}
    	else {
    		//shift all previous deletes to the next index
    		for(int i=8; i > 0; i--) {
    			String key = ""+i;
    			if(registers.containsKey(key)) {
    				getRegister(""+(i+1)).setContent(getRegister(key).getContent());
    			}
    		}
    		//set new delete to "1
    		getRegister("1").setContent(register);
    	}
    }
    
    public void setCurrentWorkingDirectory(String newDir) {
    	//if absolute path
    	if(newDir.startsWith("/")) {
    		this.cwd = newDir;
    	}
    	//relative path (append to current)
    	else {
    		if(!this.cwd.endsWith("/")) {
    			this.cwd += "/";
    		}
    		this.cwd = this.cwd + newDir;
    	}
    	
    	//delete ../ and the dir before it
    	if(this.cwd.contains("..")) {
    		String pieces[] = this.cwd.split("/");
    		ArrayList<String> dirs = new ArrayList<String>();
    		for(int i=0; i < pieces.length; i++) {
    			if("..".equals(pieces[i])) {
    				if( ! dirs.isEmpty()) {
    					dirs.remove(dirs.size()-1);
    				}
    			}
    			else {
    				dirs.add(pieces[i]);
    			}
    		}
    		if(dirs.isEmpty()) {
    			this.cwd = "/";
    		}
    		else {
    			this.cwd = StringUtils.join("/", dirs);
    		}
    	}
    }
    
    public String getCurrentWorkingDirectory() {
    	return cwd;
    }

}
