package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.AsciiCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;
import net.sourceforge.vrapper.vim.commands.EditFileCommand;
import net.sourceforge.vrapper.vim.commands.ExCommandOperation;
import net.sourceforge.vrapper.vim.commands.FindFileCommand;
import net.sourceforge.vrapper.vim.commands.LineRangeOperationCommand;
import net.sourceforge.vrapper.vim.commands.ListMarksCommand;
import net.sourceforge.vrapper.vim.commands.ListRegistersCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.OpenInGvimCommand;
import net.sourceforge.vrapper.vim.commands.ReadExternalOperation;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.RepeatLastSubstitutionCommand;
import net.sourceforge.vrapper.vim.commands.RetabOperation;
import net.sourceforge.vrapper.vim.commands.SaveAllCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SetLocalOptionCommand;
import net.sourceforge.vrapper.vim.commands.SetOptionCommand;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SortOperation;
import net.sourceforge.vrapper.vim.commands.SubstitutionOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ContentAssistMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

/**
 * Command Line Mode, activated with ':'.
 *
 * @author Matthias Radig
 */
public class CommandLineParser extends AbstractCommandParser {

    private static final EvaluatorMapping mapping;
    private final FilePathTabCompletion tabComplete;

    static {
        Evaluator noremap = new KeyMapper.Map(false,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator map = new KeyMapper.Map(true,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nnoremap = new KeyMapper.Map(false, NormalMode.KEYMAP_NAME);
        Evaluator nmap = new KeyMapper.Map(true, NormalMode.KEYMAP_NAME);
        Evaluator onoremap = new KeyMapper.Map(false, NormalMode.OMAP_NAME);
        Evaluator omap = new KeyMapper.Map(true, NormalMode.OMAP_NAME);
        Evaluator vnoremap = new KeyMapper.Map(false, VisualMode.KEYMAP_NAME);
        Evaluator vmap = new KeyMapper.Map(true, VisualMode.KEYMAP_NAME);
        Evaluator inoremap = new KeyMapper.Map(false, InsertMode.KEYMAP_NAME);
        Evaluator imap = new KeyMapper.Map(true, InsertMode.KEYMAP_NAME);
        Evaluator canoremap = new KeyMapper.Map(false, ContentAssistMode.KEYMAP_NAME);
        Evaluator camap = new KeyMapper.Map(true, ContentAssistMode.KEYMAP_NAME);
        Command ascii = AsciiCommand.INSTANCE;
        Command save = SaveCommand.INSTANCE;
        Command saveAll = SaveAllCommand.INSTANCE;
        CloseCommand close = CloseCommand.CLOSE;
        CloseCommand closeAll = CloseCommand.CLOSE_ALL;
        Command saveAndClose = new VimCommandSequence(save, close);
        Command saveAndCloseAll = new VimCommandSequence(saveAll, closeAll);
        Evaluator quit = new EvaluatorWithExclaim(CloseCommand.CLOSE, CloseCommand.FORCED_CLOSE);
        Evaluator quitAll = new EvaluatorWithExclaim(CloseCommand.CLOSE_ALL, CloseCommand.FORCED_CLOSE_ALL);
        Evaluator quitOthers = new EvaluatorWithExclaim(CloseCommand.CLOSE_OTHERS, CloseCommand.FORCED_CLOSE_OTHERS);
        
        Evaluator unmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nunmap = new KeyMapper.Unmap(NormalMode.KEYMAP_NAME);
        Evaluator ounmap = new KeyMapper.Unmap(NormalMode.OMAP_NAME);
        Evaluator vunmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME);
        Evaluator iunmap = new KeyMapper.Unmap(InsertMode.KEYMAP_NAME);
        Evaluator clear = new KeyMapper.Clear(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nclear = new KeyMapper.Clear(NormalMode.KEYMAP_NAME);
        Evaluator oclear = new KeyMapper.Clear(NormalMode.OMAP_NAME);
        Evaluator vclear = new KeyMapper.Clear(AbstractVisualMode.KEYMAP_NAME);
        Evaluator iclear = new KeyMapper.Clear(InsertMode.KEYMAP_NAME);
        Command gotoEOF = new MotionCommand(GoToLineMotion.LAST_LINE);
        Evaluator nohlsearch = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getSearchAndReplaceService().removeHighlighting();
                return null;
            }
        };
        Evaluator hlsearch = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                Search search = vim.getRegisterManager().getSearch();
                if (search != null)
                    vim.getSearchAndReplaceService().highlight(search);
                return null;
            }
        };
        Evaluator printWorkingDir = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	String dir;
            	if(vim.getConfiguration().get(Options.AUTO_CHDIR)) {
            		dir = vim.getFileService().getCurrentFilePath();
            	}
            	else {
            		dir = vim.getRegisterManager().getCurrentWorkingDirectory();
            	}
                vim.getUserInterfaceService().setInfoMessage(dir);
                return null;
            }
        };
        Evaluator editFile = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	if(command.isEmpty()) {
            		vim.getUserInterfaceService().setErrorMessage("No file name");
            		return null;
            	}
            	
                try {
					new EditFileCommand(command.poll()).execute(vim);
				} catch (CommandExecutionException e) {
				}
                return null;
            }
        };
        Evaluator findFile = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	if(command.isEmpty()) {
            		vim.getUserInterfaceService().setErrorMessage("No file name");
            		return null;
            	}
            	
            	try {
					new FindFileCommand(command.poll()).execute(vim);
				} catch (CommandExecutionException e) {
				}
            	return null;
            }
        };
        Evaluator sort = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        		String commandStr = "";
            	while(command.size() > 0) {
            		//attempt to preserve spacing in case a pattern is in use
            		//(if you attempt to sort with "/foo    bar/" it won't work)
            		commandStr += command.poll() + " ";
            	}
            	TextRange selection = null;
            	if(vim.getSelection().getModelLength() > 0) {
            	    selection = vim.getSelection();
            	}
        		
            	try {
					new SortOperation(commandStr).execute(vim, selection, ContentType.LINES);
				} catch (CommandExecutionException e) {
            		vim.getUserInterfaceService().setErrorMessage(e.getMessage());
				}
            	
            	return null;
            }
        };
        Evaluator retab = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
        		String commandStr = "";
            	while(command.size() > 0)
            		// attempt to preserve spacing
            		commandStr += command.poll() + " ";
        		
            	try {
					new RetabOperation(commandStr).execute(vim, null, ContentType.LINES);
				} catch (CommandExecutionException e) {
            		vim.getUserInterfaceService().setErrorMessage(e.getMessage());
				}
            	
            	return null;
            }
        };
        Evaluator chDir = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	String dir = command.isEmpty() ? "/" : command.poll();
            	vim.getRegisterManager().setCurrentWorkingDirectory(dir);
            	//immediately perform a pwd to show new dir
            	mapping.get("pwd").evaluate(vim, command);
            	return null;
            }
        };
        Evaluator sourceConfigFile = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	if(command.isEmpty()) {
            		vim.getUserInterfaceService().setErrorMessage("Argument required");
            		return null;
            	}
            	String filename = command.poll();
            	if( ! vim.sourceConfigurationFile(filename) ) {
            		vim.getUserInterfaceService().setErrorMessage("Can't open file " + filename);
            	}
            	return null;
            }
        };
        Evaluator startInsert = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                try {
                    if(! command.isEmpty() && command.poll().equals("!")) {
                        new ChangeToInsertModeCommand(new MotionCommand(MoveRight.INSTANCE)).execute(vim);
                    }
                    else {
                        new ChangeToInsertModeCommand().execute(vim);
                    }
				}
                catch (CommandExecutionException e) {
            		vim.getUserInterfaceService().setErrorMessage(e.getMessage());
				}
                return null;
            }
        };
        Evaluator registers = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                try {
                    String names = "";
                    while(command.size() > 0)
                        names += command.poll();

                    new ListRegistersCommand(names).execute(vim);
				}
                catch (CommandExecutionException e) {
            		vim.getUserInterfaceService().setErrorMessage(e.getMessage());
				}
                return null;
            }
        };
        Evaluator marks = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                try {
                    String names = "";
                    while(command.size() > 0)
                        names += command.poll();

                    new ListMarksCommand(names).execute(vim);
				}
                catch (CommandExecutionException e) {
            		vim.getUserInterfaceService().setErrorMessage(e.getMessage());
				}
                return null;
            }
        };
        
        mapping = new EvaluatorMapping();
        // options
        mapping.add("set", buildConfigEvaluator(/*local=*/false));
        mapping.add("setlocal", buildConfigEvaluator(/*local=*/true));
        mapping.add("source", sourceConfigFile);
        // save, close
        mapping.add("w", save);
        mapping.add("wall", saveAll);
        mapping.add("wqall", saveAndCloseAll);
        mapping.add("xall", saveAndCloseAll);
        mapping.add("xa", saveAndCloseAll);
    	mapping.add("update", save);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", quit);
        mapping.add("bdelete", quit);
        mapping.add("qall", quitAll);
        mapping.add("quitall", quitAll);
        mapping.add("only", quitOthers);
        mapping.add("tabonly", quitOthers);
        // non-recursive mapping
        mapping.add("noremap", noremap);
        mapping.add("no", noremap);
        mapping.add("nnoremap", nnoremap);
        mapping.add("nn", nnoremap);
        mapping.add("onoremap", onoremap);
        mapping.add("ono", onoremap);
        mapping.add("inoremap", inoremap);
        mapping.add("ino", inoremap);
        mapping.add("canoremap", canoremap);
        mapping.add("cano", canoremap);
        mapping.add("vnoremap", vnoremap);
        mapping.add("vn", vnoremap);
        // recursive mapping
        mapping.add("map", map);
        mapping.add("nmap", nmap);
        mapping.add("nm", nmap);
        mapping.add("omap", omap);
        mapping.add("om", omap);
        mapping.add("imap", imap);
        mapping.add("im", imap);
        mapping.add("camap", camap);
        mapping.add("cam", camap);
        mapping.add("vmap", vmap);
        mapping.add("vm", vmap);
        // unmapping
        mapping.add("unmap", unmap);
        mapping.add("unm", unmap);
        mapping.add("nunmap", nunmap);
        mapping.add("nun", nunmap);
        mapping.add("ounmap", ounmap);
        mapping.add("oun", ounmap);
        mapping.add("vunmap", vunmap);
        mapping.add("vu", vunmap);
        mapping.add("iunmap", iunmap);
        mapping.add("iu", iunmap);
        // clearing maps
        mapping.add("mapclear", clear);
        mapping.add("mapc", clear);
        mapping.add("nmapclear", nclear);
        mapping.add("nmapc", nclear);
        mapping.add("omapclear", oclear);
        mapping.add("omapc", oclear);
        mapping.add("vmapclear", vclear);
        mapping.add("vmapc", vclear);
        mapping.add("imapclear", iclear);
        mapping.add("imapc", iclear);
        UndoCommand undo = UndoCommand.INSTANCE;
        RedoCommand redo = RedoCommand.INSTANCE;
        mapping.add("red", redo);
        mapping.add("redo", redo);
        mapping.add("undo", undo);
        mapping.add("u", undo);
        mapping.add("$", new CommandWrapper(gotoEOF));
        mapping.add("nohlsearch", nohlsearch);
        mapping.add("nohls", nohlsearch);
        mapping.add("noh", nohlsearch);
        mapping.add("hlsearch", hlsearch);
        mapping.add("hls", hlsearch);
        mapping.add("pwd", printWorkingDir);
        mapping.add("edit", editFile);
        mapping.add("new", editFile);
        mapping.add("vim", OpenInGvimCommand.INSTANCE);
        mapping.add("find", findFile);
        mapping.add("tabfind", findFile);
        mapping.add("cd", chDir);
        mapping.add("sort", sort);
        mapping.add("retab", retab);
    	mapping.add("ascii", ascii);
    	mapping.add("startinsert", startInsert);
    	mapping.add("registers", registers);
    	mapping.add("display", registers);
    	mapping.add("marks", marks);
    }

    private static Evaluator buildConfigEvaluator(boolean local) {
        Evaluator ev;
        if (local) {
            ev = new ComplexLocalOptionEvaluator();
        } else {
            ev = new ComplexOptionEvaluator();
        }
        EvaluatorMapping config = new EvaluatorMapping(ev);
        // boolean options
        for (Option<Boolean> o: Options.BOOLEAN_OPTIONS) {
            ConfigCommand<Boolean> enable;
            ConfigCommand<Boolean> disable;
            ConfigCommand<Boolean> toggle;
            if (local) {
                enable  = new SetLocalOptionCommand<Boolean>(o, Boolean.TRUE);
                disable = new SetLocalOptionCommand<Boolean>(o, Boolean.FALSE);
                toggle  = new ToggleLocalOptionCommand(o);
            } else {
                enable  = new SetOptionCommand<Boolean>(o, Boolean.TRUE);
                disable = new SetOptionCommand<Boolean>(o, Boolean.FALSE);
                toggle  = new ToggleOptionCommand(o);
            }
            ConfigCommand<Boolean> status = new PrintOptionCommand<Boolean>(o);
            for (String alias: o.getAllNames()) {
                config.add(alias, enable);
                config.add("no"+alias, disable);
                config.add(alias+"!", toggle);
                config.add(alias+"?", status);
            }
        }
        for (Option<Integer> o : Options.INT_OPTIONS) {
            ConfigCommand<Integer> status = new PrintOptionCommand<Integer>(o);
            for (String alias: o.getAllNames()) {
                config.add(alias+"?", status);
            }
        }
        for (Option<String> o : Options.STRING_OPTIONS) {
            ConfigCommand<String> status = new PrintOptionCommand<String>(o);
            for (String alias: o.getAllNames()) {
                config.add(alias+"?", status);
            }
        }
        // overwrites hlsearch/nohlsearch commands
        config.add("globalregisters", ConfigAction.GLOBAL_REGISTERS);
        config.add("noglobalregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("localregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("nolocalregisters", ConfigAction.GLOBAL_REGISTERS);
        addActionsToBooleanOption(config, Options.LINE_NUMBERS, ConfigAction.LINE_NUMBERS, ConfigAction.NO_LINE_NUMBERS);
        addActionsToBooleanOption(config, Options.SHOW_WHITESPACE, ConfigAction.SHOW_WHITESPACE, ConfigAction.NO_SHOW_WHITESPACE);
        addActionsToBooleanOption(config, Options.HIGHLIGHT_CURSOR_LINE, ConfigAction.HIGHLIGHT_CURSOR_LINE, ConfigAction.NO_HIGHLIGHT_CURSOR_LINE);

        return config;
    }
    
    private static void addActionsToBooleanOption(EvaluatorMapping config,
            Option<Boolean> option, ConfigAction enable, ConfigAction disable) {
        Evaluator toggle = new OptionDependentEvaluator(option, disable, enable);
        for (String alias : option.getAllNames()) {
            config.add(alias, enable);
            config.add("no" + alias, disable);
            config.add(alias + "!", toggle);
        }
    }

    public CommandLineParser(EditorAdaptor vim) {
        super(vim);
        this.tabComplete = new FilePathTabCompletion(vim);
    }

    class LineRangeExCommandEvaluator implements Command {
        private LineRangeOperationCommand range = null;
        private Evaluator command = null;
        private Queue<String> tokens = null;
        private boolean isFromVisual;

        public LineRangeExCommandEvaluator(LineRangeOperationCommand range, Evaluator command, Queue<String> tokens,
                boolean isFromVisual) {
            this.range = range;
            this.command = command;
            this.tokens = tokens;
            this.isFromVisual = isFromVisual;
        }

        public Command repetition() {
            return null;
        }

        public Command withCount(int count) {
            return null;
        }

        public int getCount() {
            return 0;
        }

        public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
            boolean linewise = !isFromVisual || editorAdaptor.getSelection().getContentType(editorAdaptor.getConfiguration()) == ContentType.LINES;
            Selection selection = range.parseRangeDefinition(editorAdaptor, linewise);
            editorAdaptor.setSelection(selection);
            command.evaluate(editorAdaptor, tokens);
            editorAdaptor.setSelection(null);
        }

    };
    
    public class ExCommandEvaluator implements Command {
        private Evaluator mappping = null;
        private Queue<String> tokens = null;

        public ExCommandEvaluator(Evaluator mappping, Queue<String> tokens) {
            this.mappping = mappping;
            this.tokens = tokens;
        }

        public Command repetition() {
            return null;
        }

        public Command withCount(int count) {
            return null;
        }

        public int getCount() {
            return 0;
        }

        public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
            mappping.evaluate(editorAdaptor, tokens);
        }

    };

    @Override
    protected String completeArgument(String commandLineContents, KeyStroke e) {
        int cmdLen = 0;
        boolean paths = false;
        boolean dirsOnly = false;
        if (commandLineContents.toString().startsWith("e ")) {
            cmdLen = 2;
        } else if(commandLineContents.toString().startsWith("find ") ||
                commandLineContents.toString().startsWith("tabf ") ) {
            cmdLen = 5;
            paths = true;
        } else if(commandLineContents.toString().startsWith("cd ")) {
            cmdLen = 3;
            dirsOnly = true;
        } else if(commandLineContents.toString().startsWith("split ")) {
            //add support for tab-completion on :split command
            //even though the Split Editor Plugin must be installed
            //to execute the :split command
            cmdLen = 6;
        } else if(commandLineContents.toString().startsWith("vsplit ")) {
            cmdLen = 7;
        }

        if (cmdLen > 0) {
            String cmd = commandLineContents.substring(0, cmdLen);
            String prefix = commandLineContents.substring(cmdLen);
            prefix = tabComplete.getNextMatch(prefix, paths, dirsOnly, e.withShiftKey());
            return cmd + prefix;
        } else {
            //user hit TAB but it wasn't intended as a tab-completion
            //insert tab character
            commandLine.type("\t");
        }
        return null;
    }
    @Override
    public Command parseAndExecute(String first, String command) {
        if(first != null) {
            while(command.startsWith(first)) {
                //remove any superfluous ':' preceding the command
                command = command.substring(1);
            }
        }
        if(command.indexOf(" | ") > -1) {
            String[] commands = command.split(" | ");
            editor.getHistory().beginCompoundChange();
            editor.getHistory().lock("chained-commands");
            boolean performedChain = false;
            try {
                Command c;
                for(String chainCommand : commands) {
                    if( ! "|".equals(chainCommand)) {
                        //recurse!
                        c = parseAndExecute(first, chainCommand);
                        if(c != null) {
                            c.execute(editor);
                            //this really was a set of commands chained together!
                            performedChain = true;
                        }
                        else {
                            //exit on first failed command
                            break;
                        }
                    }
                }
            }
            catch(CommandExecutionException e) {
                if(performedChain) {
                    //if this error wasn't due to us executing a string that
                    //wasn't actually a set of commands chained together then
                    //display the error.
                    editor.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
            }
            editor.getHistory().unlock("chained-commands");
            editor.getHistory().endCompoundChange();

            //If the first chained command succeeded then this really was a set
            //of commands chained together and we can exit here.  If that first
            //command couldn't be parsed, maybe this was just a '|' within a
            //command and wasn't intended to chain things together.  So, if the
            //first command failed, continue into parseAndExecute() with the
            //original unmodified command.
            //(this is to handle the case of :s/ | /foo/g)
            if(performedChain) {
                return null;
            }
        }

        try {
            // if the command is a number, jump to the given line
            int line = Integer.parseInt(command);
            return new MotionCommand(GoToLineMotion.FIRST_LINE.withCount(line));
        } catch (NumberFormatException e) {
            // do nothing
        }
        // See if command is for a particular editor type only (when parsing .vrapperrc).
        if (AutoCmdParser.INSTANCE.validate(editor, command)) {
            AutoCmdParser.INSTANCE.parse(editor, command);
            if (AutoCmdParser.INSTANCE.getCommand() != null) {
                command = AutoCmdParser.INSTANCE.getCommand();
            } else {
                return AutoCmdParser.INSTANCE;
            }
        }
        // copy/move operation without range to copy/move the current line
        if (LineRangeOperationCommand.isCurrentLineCopyMove(command)) {
            return new LineRangeOperationCommand("." + command);
        }
        
        EvaluatorMapping platformCommands = editor.getPlatformSpecificStateProvider().getCommands();
        
        /** First, check for all operations which are not whitespace-delimited **/
       
        //not a number but starts with a number, %, $, /, ?, +, -, ', . (dot), or , (comma)
        //might be a line range operation
        if(command.length() > 1 && LineRangeOperationCommand.isLineRangeOperation(command))
        {
        	final LineRangeOperationCommand rangeOp = new LineRangeOperationCommand(command);
        	// Parse the remainder as a regular command.
        	StringTokenizer nizer = new StringTokenizer(rangeOp.getOperationStr());
        	LinkedList<String> tokens = new LinkedList<String>();
        	while(nizer.hasMoreTokens())
        	    tokens.add(nizer.nextToken().trim());
        	// Check if there is a mapping for the operation.
        	if (platformCommands != null && platformCommands.contains(tokens.peek())) {
        	    return new LineRangeExCommandEvaluator(rangeOp, platformCommands, tokens, isFromVisual());
        	} else {
        	    if (mapping != null && mapping.contains(tokens.peek())) {
        	        return new LineRangeExCommandEvaluator(rangeOp, mapping, tokens, isFromVisual());
        	    } else {
        	        // Handle predefined operations (y/d/c...).
        	        return rangeOp;
        	    }
        	}
        }
        
        //might be a substitution definition
        Command substitution = parseSubstitution(command);
        if(substitution != null)
        	return substitution;
        
        //might be an Ex command
        if(command.length() > 1 && (command.startsWith("g") || command.startsWith("v"))
        		&& VimUtils.isPatternDelimiter(""+command.charAt(1))) {
    		return new TextOperationTextObjectCommand(
				new ExCommandOperation(command), new SimpleSelection(null)
    		);
        }
        
        // Read command output operation
        if(ReadExternalOperation.isValid(editor, command)) {
            // Execute on the current line.
            return new LineRangeOperationCommand("." + command);
        }
        
        /** Now check against list of known commands (whitespace-delimited) **/
        
        //tokenize based on whitespace
        StringTokenizer nizer = new StringTokenizer(command);
        LinkedList<String> tokens = new LinkedList<String>();
        while(nizer.hasMoreTokens())
            tokens.add(nizer.nextToken().trim());
        
        if(tokens.isEmpty()) {
        	//someone hit "enter" without providing a command
        	return null;
        }
            
        //separate '!' from command name
        if(tokens.peek().endsWith("!")) {
        	String tok = tokens.poll();
        	if(tokens.isEmpty()) {
        		tokens.add(tok.substring(0, tok.length()-1));
        		tokens.add("!");
        	}
        	else {
        		tokens.add(0, tok.substring(0, tok.length()-1));
        		tokens.add(1, "!");
        	}
        }
        
        //see if a command is defined for the first token
        if(platformCommands != null && platformCommands.contains(tokens.peek())) {
            return new ExCommandEvaluator(platformCommands, tokens);
        }
        else if(mapping != null && mapping.contains(tokens.peek())) {
            return new ExCommandEvaluator(mapping, tokens);
        }
        else { //see if there is a partial match
            String commandName;
            if(platformCommands != null &&
                    (commandName = platformCommands.getNameFromPartial(tokens.peek())) != null) {
            	tokens.set(0, commandName);
            	return new ExCommandEvaluator(platformCommands, tokens);
            }
            else {
            	if(mapping != null &&
            	        (commandName = mapping.getNameFromPartial(tokens.peek())) != null) {
            		tokens.set(0, commandName);
            		return new ExCommandEvaluator(mapping, tokens);
            	}
            	else {
            		editor.getUserInterfaceService().setErrorMessage("Not an editor command: " + tokens.peek());
            	}
            }
        }
        
        return null;
    }
    
    /**
     * The substitution feature (:s/foo/bar/g) is complicated.
     * There are lots of nuances to it.  This method attempts
     * to keep it all contained here.
     */
    private Command parseSubstitution(String command) {
    	if(command.equals("s")) {
    		return RepeatLastSubstitutionCommand.CURRENT_LINE_ONLY;
    	}
    	//any non-alphanumeric character can be a delimiter
    	//(this check is to avoid treating ":set" as a substitution)
    	if(command.startsWith("s") && VimUtils.isPatternDelimiter(""+command.charAt(1))) {
    		//null TextRange is a special case for "current line"
    		return new TextOperationTextObjectCommand(
				new SubstitutionOperation(command), new SimpleSelection(null)
    		);
    	}
    	//not a substitution
    	return null;
    }
    
    public boolean addCommand(String commandName, Command command, boolean overwrite) {
        if (overwrite || !mapping.contains(commandName)) {
            mapping.add(commandName, command);
            return true;
        }
        return false;
    }
    
    private enum ConfigAction implements Evaluator {

        GLOBAL_REGISTERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.useGlobalRegisters();
                return null;
            }
        },
        NO_GLOBAL_REGISTERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.useLocalRegisters();
                return null;
            }
        },
        EXPAND_TAB {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.EXPAND_TAB, Boolean.TRUE);
                vim.getEditorSettings().setShowLineNumbers(true);
                return null;
            }
        },
        NO_EXPAND_TAB {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.EXPAND_TAB, Boolean.FALSE);
                vim.getEditorSettings().setShowLineNumbers(false);
                return null;
            }
        },
        LINE_NUMBERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.LINE_NUMBERS, Boolean.TRUE);
                vim.getEditorSettings().setShowLineNumbers(true);
                return null;
            }
        },
        NO_LINE_NUMBERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.LINE_NUMBERS, Boolean.FALSE);
                vim.getEditorSettings().setShowLineNumbers(false);
                return null;
            }
        },
        SHOW_WHITESPACE {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.SHOW_WHITESPACE, Boolean.TRUE);
                vim.getEditorSettings().setShowWhitespace(true);
                return null;
            }
        },
        NO_SHOW_WHITESPACE {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.SHOW_WHITESPACE, Boolean.FALSE);
                vim.getEditorSettings().setShowWhitespace(false);
                return null;
            }
        },
        HIGHLIGHT_CURSOR_LINE {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.HIGHLIGHT_CURSOR_LINE, Boolean.TRUE);
                vim.getEditorSettings().setHighlightCursorLine(true);
                return null;
            }
        },
        NO_HIGHLIGHT_CURSOR_LINE {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.HIGHLIGHT_CURSOR_LINE, Boolean.FALSE);
                vim.getEditorSettings().setHighlightCursorLine(false);
                return null;
            }
        }
            ;
    }
    
    private static class EvaluatorWithExclaim implements Evaluator {
    	Command without;
    	Command with;
    	
    	private EvaluatorWithExclaim(Command withoutExclaim, Command withExclaim) {
    		this.without = withoutExclaim;
    		this.with = withExclaim;
    	}
    	
    	public Object evaluate(EditorAdaptor vim, Queue<String> command) {
    		try {
    			if( !command.isEmpty() && command.peek().endsWith("!")) {
    				with.execute(vim);
    			}
    			else {
    				without.execute(vim);
    			}
    		} catch (CommandExecutionException e) {
    			vim.getUserInterfaceService().setErrorMessage(e.getMessage());
    		}
    		return null;
    	}
    	
    }

    private static class OptionDependentEvaluator implements Evaluator {
        private final Option<Boolean> option;
        private final Evaluator onTrue;
        private final Evaluator onFalse;
        private OptionDependentEvaluator(Option<Boolean> option,
                Evaluator onTrue, Evaluator onFalse) {
            super();
            this.option = option;
            this.onTrue = onTrue;
            this.onFalse = onFalse;
        }

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            return vim.getConfiguration().get(option)
                 ? onTrue.evaluate(vim, command)
                 : onFalse.evaluate(vim, command);
        }
    }
}
