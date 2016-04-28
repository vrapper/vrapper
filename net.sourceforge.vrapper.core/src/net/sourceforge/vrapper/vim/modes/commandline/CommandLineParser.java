package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.SubstitutionDefinition;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.AnonymousMacroOperation;
import net.sourceforge.vrapper.vim.commands.AsciiCommand;
import net.sourceforge.vrapper.vim.commands.ChangeModeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToInsertModeCommand;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;
import net.sourceforge.vrapper.vim.commands.DummyCommand;
import net.sourceforge.vrapper.vim.commands.DummyTextObject;
import net.sourceforge.vrapper.vim.commands.EditFileCommand;
import net.sourceforge.vrapper.vim.commands.ExCommandOperation;
import net.sourceforge.vrapper.vim.commands.ExSearchCommand;
import net.sourceforge.vrapper.vim.commands.FindFileCommand;
import net.sourceforge.vrapper.vim.commands.LineRangeOperationCommand;
import net.sourceforge.vrapper.vim.commands.ListBuffersCommand;
import net.sourceforge.vrapper.vim.commands.ListMarksCommand;
import net.sourceforge.vrapper.vim.commands.ListRegistersCommand;
import net.sourceforge.vrapper.vim.commands.ListUserCommandsCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.OpenInGvimCommand;
import net.sourceforge.vrapper.vim.commands.ReadExternalOperation;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.RepeatLastSubstitutionCommand;
import net.sourceforge.vrapper.vim.commands.RetabOperation;
import net.sourceforge.vrapper.vim.commands.SaveAllCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.SetLocalOptionCommand;
import net.sourceforge.vrapper.vim.commands.SetOptionCommand;
import net.sourceforge.vrapper.vim.commands.SortOperation;
import net.sourceforge.vrapper.vim.commands.SubstitutionOperation;
import net.sourceforge.vrapper.vim.commands.SwitchBufferCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.ConfirmSubstitutionMode;
import net.sourceforge.vrapper.vim.modes.ContentAssistMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

/**
 * Command Line Mode, activated with ':'.
 *
 * @author Matthias Radig
 */
public class CommandLineParser extends AbstractCommandParser {

    public static final Pattern EDIT_CMD_PATTERN = Pattern.compile(""
                    + "^"
                    + "(e|edit)"
                    + "\\s+");
    public static final Pattern FIND_CMD_PATTERN = Pattern.compile("^"
                    + "(find|fin)"
                    + "\\s+");
    public static final Pattern TABF_CMD_PATTERN = Pattern.compile("^"
                    + "(tabf|tabfind)"
                    + "\\s+");
    public static final Pattern CD_CMD_PATTERN = Pattern.compile("^cd\\s+");
    public static final Pattern SPLIT_CMD_PATTERN = Pattern.compile("^"
                    + "(split|sp)"
                    + "\\s+");
    public static final Pattern VSPLIT_CMD_PATTERN = Pattern.compile("^"
                    + "(vsplit|vs)"
                    + "\\s+");

	private Matcher matcher;

	private final EvaluatorMapping mapping;
    private final FilePathTabCompletion tabComplete;

    static EvaluatorMapping coreCommands() {
        Evaluator noremap = new KeyMapper.Map(false, AbstractVisualMode.KEYMAP_NAME,
                NormalMode.KEYMAP_NAME, KeyMapResolver.OMAP_NAME);
        Evaluator map = new KeyMapper.Map(true, AbstractVisualMode.KEYMAP_NAME,
                NormalMode.KEYMAP_NAME, KeyMapResolver.OMAP_NAME);
        Evaluator nnoremap = new KeyMapper.Map(false, NormalMode.KEYMAP_NAME);
        Evaluator nmap = new KeyMapper.Map(true, NormalMode.KEYMAP_NAME);
        Evaluator onoremap = new KeyMapper.Map(false, KeyMapResolver.OMAP_NAME);
        Evaluator omap = new KeyMapper.Map(true, KeyMapResolver.OMAP_NAME);
        Evaluator vnoremap = new KeyMapper.Map(false, VisualMode.KEYMAP_NAME);
        Evaluator vmap = new KeyMapper.Map(true, VisualMode.KEYMAP_NAME);
        Evaluator inoremap = new KeyMapper.Map(false, InsertMode.KEYMAP_NAME);
        Evaluator imap = new KeyMapper.Map(true, InsertMode.KEYMAP_NAME);
        Evaluator canoremap = new KeyMapper.Map(false, ContentAssistMode.KEYMAP_NAME);
        Evaluator camap = new KeyMapper.Map(true, ContentAssistMode.KEYMAP_NAME);
        Evaluator cnoremap = new KeyMapper.Map(false, AbstractCommandLineMode.COMMANDLINE_KEYMAP_NAME);
        Evaluator cmap = new KeyMapper.Map(true, AbstractCommandLineMode.COMMANDLINE_KEYMAP_NAME);
        Command ascii = AsciiCommand.INSTANCE;
        Command save = SaveCommand.INSTANCE;
        Command saveAll = SaveAllCommand.INSTANCE;
        Command close = CloseCommand.CLOSE;
        Command closeAll = CloseCommand.CLOSE_ALL;
        Command saveAndClose = new VimCommandSequence(save, close);
        Command saveAndCloseAll = new VimCommandSequence(saveAll, closeAll);
        Evaluator quit = new EvaluatorWithExclaim(CloseCommand.CLOSE, CloseCommand.FORCED_CLOSE);
        Evaluator quitAll = new EvaluatorWithExclaim(CloseCommand.CLOSE_ALL, CloseCommand.FORCED_CLOSE_ALL);
        Evaluator quitOthers = new EvaluatorWithExclaim(CloseCommand.CLOSE_OTHERS, CloseCommand.FORCED_CLOSE_OTHERS);
        
        Evaluator unmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nunmap = new KeyMapper.Unmap(NormalMode.KEYMAP_NAME);
        Evaluator ounmap = new KeyMapper.Unmap(KeyMapResolver.OMAP_NAME);
        Evaluator vunmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME);
        Evaluator iunmap = new KeyMapper.Unmap(InsertMode.KEYMAP_NAME);
        Evaluator clear = new KeyMapper.Clear(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nclear = new KeyMapper.Clear(NormalMode.KEYMAP_NAME);
        Evaluator oclear = new KeyMapper.Clear(KeyMapResolver.OMAP_NAME);
        Evaluator vclear = new KeyMapper.Clear(AbstractVisualMode.KEYMAP_NAME);
        Evaluator iclear = new KeyMapper.Clear(InsertMode.KEYMAP_NAME);
        Command gotoEOF = new MotionCommand(GoToLineMotion.LAST_LINE);
        Evaluator nohlsearch = HighlightSearch.CLEAR_HIGHLIGHT;
        final Evaluator printWorkingDir = new Evaluator() {
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
        Evaluator chDir = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
            	String dir = command.isEmpty() ? "/" : command.poll();
            	vim.getRegisterManager().setCurrentWorkingDirectory(dir);
            	//immediately perform a pwd to show new dir
            	printWorkingDir.evaluate(vim, command);
            	return null;
            }
        };
        Evaluator editFile = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            	if(command.isEmpty()) {
            		vim.getFileService().refreshFile() ;
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
            	TextObject selection = new DummyTextObject(null);
            	if(vim.getSelection().getModelLength() > 0) {
            	    selection = vim.getSelection();
            	}
        		
            	try {
					new SortOperation(commandStr).execute(vim, 0, selection);
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
                    RetabOperation retabOperation = new RetabOperation(commandStr);
                    retabOperation.execute(vim, retabOperation.getDefaultRange(vim, 0, vim.getPosition()));
                } catch (CommandExecutionException e) {
                    vim.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
                
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
        Evaluator let = new LetExpressionEvaluator();
        Evaluator userCommand = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                if(command.isEmpty()) {
                    try {
                        new ListUserCommandsCommand().execute(vim);
                    } catch (CommandExecutionException e) {
                        vim.getUserInterfaceService().setErrorMessage(e.getMessage());
                    }
                    return null;
                }

                String name = command.poll();
                if(Character.isLowerCase(name.charAt(0))) {
                    vim.getUserInterfaceService().setErrorMessage("User defined commands must start with an uppercase letter");
                    return null;
                }
                
                if(command.isEmpty()) {
                    try {
                        new ListUserCommandsCommand(name).execute(vim);
                    } catch (CommandExecutionException e) {
                        vim.getUserInterfaceService().setErrorMessage(e.getMessage());
                    }
                    return null;
                }

                String args = "";
                while(command.size() > 0)
                    args += command.poll() + " ";

                //add this command to the mappings for future use
                vim.getPlatformSpecificStateProvider().getCommands().addUserDefined(name, args);

                return null;
            }
        };
        Evaluator normal = new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                try {
                    StringBuilder args = new StringBuilder();
                    if (command.size() > 0) {
                        args.append(command.poll());
                    }
                    while (command.size() > 0) {
                        args.append(' ').append(command.poll());
                    }
                    LineRange range;
                    TextRange nativeSelection = vim.getNativeSelection();
                    if (nativeSelection.getModelLength() > 0
                            && SelectionService.VRAPPER_SELECTION_ACTIVE.equals(nativeSelection)) {
                        range = SimpleLineRange.fromSelection(vim, vim.getSelection());
                    } else if (nativeSelection.getModelLength() > 0) {
                        // Native selection is exclusive, use the TextRange
                        range = SimpleLineRange.fromTextRange(vim, nativeSelection);
                    } else {
                        range = SimpleLineRange.singleLine(vim, vim.getPosition());
                    }
                    new AnonymousMacroOperation(args.toString()).execute(vim, range);
                }
                catch (CommandExecutionException e) {
                    vim.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
                return null;
            }
        };
        
        EvaluatorMapping mapping = new EvaluatorMapping();
        // options
        mapping.add("set", buildConfigEvaluator(/*local=*/false));
        mapping.add("setlocal", buildConfigEvaluator(/*local=*/true));
        mapping.add("so", sourceConfigFile);
        mapping.add("source", sourceConfigFile);
        mapping.add("let", let);
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
        mapping.add("command", userCommand);
        mapping.add("noremap", noremap);
        mapping.add("no", noremap);
        mapping.add("nnoremap", nnoremap);
        mapping.add("nn", nnoremap);
        mapping.add("onoremap", onoremap);
        mapping.add("ono", onoremap);
        mapping.add("inoremap", inoremap);
        mapping.add("ino", inoremap);
        mapping.add("cno", cnoremap);
        mapping.add("cnoremap", cnoremap);
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
        mapping.add("cm", cmap);
        mapping.add("cmap", cmap);
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
        mapping.add("normal", normal);
        mapping.add("startinsert", startInsert);
        mapping.add("registers", registers);
        mapping.add("display", registers);
        mapping.add("marks", marks);
        mapping.add("ls", new CommandWrapper(ListBuffersCommand.INSTANCE));
        mapping.add("buffers", new CommandWrapper(ListBuffersCommand.INSTANCE));
        return mapping;
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
        for (Option<Set<String>> o : Options.STRINGSET_OPTIONS) {
            ConfigCommand<Set<String>> status = new PrintOptionCommand<Set<String>>(o);
            for (String alias: o.getAllNames()) {
                config.add(alias+"?", status);
            }
        }
        addActionsToBooleanOption(config, Options.GLOBAL_REGISTERS, ConfigAction.GLOBAL_REGISTERS, ConfigAction.NO_GLOBAL_REGISTERS);

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

    public CommandLineParser(EditorAdaptor vim, EvaluatorMapping commands) {
        super(vim);
        this.mapping = commands;
        this.tabComplete = new FilePathTabCompletion(vim);
    }

    @Override
    public void setFromVisual(boolean isFromVisual) {
        super.setFromVisual(isFromVisual);
        if (isFromVisual) {
            //display '<,'> to represent visual selection
            commandLine.resetContents("'<,'>");
        }
    }
    
    private boolean patternMatch(Pattern pattern, String input) {
    	matcher = pattern.matcher(input);
        return matcher.find();
    }
    
    private int getMatcherLength(int group) {
		if (matcher == null)
			return 0;
    	return matcher.end(group) - matcher.start(group);
    }

    @Override
    protected String completeArgument(String commandLineContents, KeyStroke e) {
        int cmdLen = 0;
        boolean paths = false;
        boolean dirsOnly = false;

        if (patternMatch(EDIT_CMD_PATTERN, commandLineContents.toString())) {
            cmdLen = getMatcherLength(0);
        } else if (patternMatch(FIND_CMD_PATTERN, commandLineContents.toString()) ||
                        patternMatch(TABF_CMD_PATTERN, commandLineContents.toString())) {
            cmdLen = getMatcherLength(0);
            paths = true;
        } else if (patternMatch(CD_CMD_PATTERN, commandLineContents.toString())) {
            cmdLen = getMatcherLength(0);
            dirsOnly = true;
        } else if (patternMatch(SPLIT_CMD_PATTERN, commandLineContents.toString())) {
            // add support for tab-completion on :split command
            // even though the Split Editor Plugin must be installed
            // to execute the :split command
            cmdLen = getMatcherLength(0);
        } else if (patternMatch(VSPLIT_CMD_PATTERN, commandLineContents.toString())) {
            cmdLen = getMatcherLength(0);
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
        // set the @: register even if this command is invalid
        editor.getRegisterManager().setLastCommand(first + command + "<cr>");

        //remove any superfluous ':' preceding the command
        if (first != null) {
            while (command.startsWith(first)) {
                command = command.substring(1);
            }
        }

        //command chaining using " | "
        if (command.indexOf(" | ") > -1 && ! command.startsWith("com")) {
            String[] commands = command.split(" | ");
            editor.getHistory().beginCompoundChange();
            editor.getHistory().lock("chained-commands");
            boolean performedChain = false;
            try {
                Command c;
                for (String chainCommand : commands) {
                    if ( ! "|".equals(chainCommand)) {
                        //recurse!
                        c = parseAndExecute(first, chainCommand);
                        if (c != null) {
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
            catch (CommandExecutionException e) {
                if (performedChain) {
                    //if this error wasn't due to us executing a string that
                    //wasn't actually a set of commands chained together then
                    //display the error.
                    editor.getUserInterfaceService().setErrorMessage(e.getMessage());
                }
            }
            catch (NullPointerException e) {
                //must not have been a chain
            }
            finally {
                editor.getHistory().unlock("chained-commands");
                editor.getHistory().endCompoundChange();
            }

            //If the first chained command succeeded then this really was a set
            //of commands chained together and we can exit here.  If that first
            //command couldn't be parsed, maybe this was just a '|' within a
            //command and wasn't intended to chain things together.  So, if the
            //first command failed, continue into parseAndExecute() with the
            //original unmodified command.
            //(this is to handle the case of :s/ | /foo/g)
            if (performedChain) {
                return null;
            }
        }

        //shortcut: if command is just a number, jump to the given line
        try {
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
        // Certain ex commands can be entered without a line range, in that case "." is implied.
        if (LineRangeOperationCommand.isCurrentLineOperation(command)) {
            return new LineRangeOperationCommand("." + command);
        }
        
        EvaluatorMapping platformCommands = editor.getPlatformSpecificStateProvider().getCommands();
        
        /** First, check for all operations which are not whitespace-delimited **/
       
        //not a number but starts with a number, %, $, /, ?, +, -, ', . (dot), or , (comma)
        //might be a line range operation
        if (command.length() > 1 && LineRangeOperationCommand.isLineRangeOperation(command))
        {
        	final LineRangeOperationCommand rangeOp = new LineRangeOperationCommand(command);
        	// Parse the remainder as a regular command.
        	StringTokenizer nizer = new StringTokenizer(rangeOp.getOperationStr());
        	LinkedList<String> tokens = new LinkedList<String>();
        	while (nizer.hasMoreTokens())
        	    tokens.add(nizer.nextToken().trim());
        	// Check if there is a mapping for the operation.
        	if (platformCommands != null && platformCommands.contains(tokens.peek())) {
        	    return new RunSelectionAwareEvaluatorCommand(rangeOp, platformCommands, tokens, isFromVisual());
        	} else {
        	    if (mapping != null && mapping.contains(tokens.peek())) {
        	        return new RunSelectionAwareEvaluatorCommand(rangeOp, mapping, tokens, isFromVisual());
        	    } else {
        	        // Handle predefined operations (y/d/c...).
        	        return rangeOp;
        	    }
        	}
        }
        
        //If starts with '/' or '?' but isn't a line range operation (checked above)
        //then it must be a search request.  Perform search without switching modes.
        if (command.startsWith("/") || command.startsWith("?")) {
            return new ExSearchCommand(""+command.charAt(0), command.substring(1));
        }
        
        //might be a substitution definition
        Command substitution = parseSubstitution(command);
        if (substitution != null)
        	return substitution;
        
        //might be a Global / inVerted global command
        if (command.length() > 1 && (command.startsWith("g") || command.startsWith("v"))
        		&& VimUtils.isPatternDelimiter(""+command.charAt(1))) {
    		return new TextOperationTextObjectCommand(
				new ExCommandOperation(command), new DummyTextObject(null)
    		);
        }
        
        // Read command output operation
        if (ReadExternalOperation.isValid(editor, command)) {
            // Execute on the current line.
            return new LineRangeOperationCommand("." + command);
        }
        
        /** Now check against list of known commands (whitespace-delimited) **/
        
        //tokenize based on whitespace
        StringTokenizer nizer = new StringTokenizer(command);
        LinkedList<String> tokens = new LinkedList<String>();
        while (nizer.hasMoreTokens())
            tokens.add(nizer.nextToken().trim());
        
        if (tokens.isEmpty()) {
        	//someone hit "enter" without providing a command
        	return null;
        }
            
        //separate '!' from command name
        if (tokens.peek().endsWith("!")) {
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

        Command switchBuffer = parseSwitchBufferCommand(tokens);
        if (switchBuffer != null) {
            return switchBuffer;
        }

        //see if a command is defined for the first token
        if (platformCommands != null && platformCommands.contains(tokens.peek())) {
            return new RunEvaluatorCommand(platformCommands, tokens);
        }
        else if (mapping != null && mapping.contains(tokens.peek())) {
            return new RunEvaluatorCommand(mapping, tokens);
        }
        else { //see if there is a partial match
            String commandName;
            if (platformCommands != null &&
                    (commandName = platformCommands.getNameFromPartial(tokens.peek())) != null) {
            	tokens.set(0, commandName);
            	return new RunEvaluatorCommand(platformCommands, tokens);
            }
            else {
            	if (mapping != null &&
            	        (commandName = mapping.getNameFromPartial(tokens.peek())) != null) {
            		tokens.set(0, commandName);
            		return new RunEvaluatorCommand(mapping, tokens);
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
        if (command.equals("s")) {
            return RepeatLastSubstitutionCommand.CURRENT_LINE_ONLY;
        }
        //any non-alphanumeric character can be a delimiter
        //(this check is to avoid treating ":set" as a substitution)
        if (command.startsWith("s") && VimUtils.isPatternDelimiter(""+command.charAt(1))) {
            SubstitutionDefinition subDef;
            try {
                subDef = new SubstitutionDefinition(command, editor.getRegisterManager());
            }
            catch(IllegalArgumentException e) {
                return new DummyCommand(e.getMessage());
            }
            if(subDef.hasFlag('c')) {
                int line = editor.getModelContent().getLineInformationOfOffset(
                        editor.getCursorService().getPosition().getModelOffset()).getNumber();
                //move into "confirm" mode
                return new ChangeModeCommand(ConfirmSubstitutionMode.NAME,
                        new ConfirmSubstitutionMode.SubstitutionConfirm(subDef, line, line));
            } else {
                //null TextRange is a special case for "current line"
                return new TextOperationTextObjectCommand(
                        new SubstitutionOperation(subDef), new DummyTextObject(null));
            }
        }
        //not a substitution
        return null;
    }

    /**
     * Parses the :b[uffer] command more or less like Vim would do. Vim accepts anything from
     * <code>b</code> to <code>buffer</code> as a buffer switch command, and it is equally liberal
     * with spaces after the command. For example, <code>:b1</code> or <code>:bu#</code> are just as
     * valid as <code>:b 1</code> or <code>:buffer #</code>.
     */
    private Command parseSwitchBufferCommand(LinkedList<String> originalTokens) {
        LinkedList<String> tokens = new LinkedList<String>(originalTokens);
        // Ignore "!"
        tokens.remove("!");
        String cmdStr = tokens.peek();
        Matcher bufferPrefixMatcher = SwitchBufferCommand.BUFFER_CMD_PATTERN.matcher(cmdStr);

        if (bufferPrefixMatcher.lookingAt()) {
            // Either we have a false positive (e.g. ':buffers') or a parameter is glued to :buffer.

            if (bufferPrefixMatcher.end() < cmdStr.length()) {
                String cmdRemaining = cmdStr.substring(bufferPrefixMatcher.end());
                Matcher glued = SwitchBufferCommand.BUFFER_CMD_GLUED_ARG_PATTERN.matcher(cmdRemaining);

                if (glued.matches()) {
                    if (glued.groupCount() == 1 && glued.group(1) != null) {
                        return new SwitchBufferCommand(glued.group(1));
                    } else if (cmdRemaining.endsWith("#")) {
                        return SwitchBufferCommand.INSTANCE;
                    } else {
                        return new SwitchBufferCommand("%");
                    }
                } else {
                    // Likely the ':buffers' command
                    return null;
                }
            } else if (tokens.size() <= 1) {
                // No parameters specified, no-op.
                return new SwitchBufferCommand("%");
            } else {
                return new SwitchBufferCommand(tokens.get(1));
            }
        } else {
            return null;
        }
    }

    /**
     * Runs an evaluator with a temporary selection based on the ex line range given by the user.
     */
    class RunSelectionAwareEvaluatorCommand implements Command {
        private LineRangeOperationCommand range = null;
        private Evaluator action = null;
        private Queue<String> tokens = null;
        private boolean isFromVisual;
    
        public RunSelectionAwareEvaluatorCommand(LineRangeOperationCommand range, Evaluator action, Queue<String> tokens,
                boolean isFromVisual) {
            this.range = range;
            this.action = action;
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
            LineRange lineRange = range.parseRangeDefinition(editorAdaptor);
            TextRange selectionRange;
            if (lineRange == null) {
                selectionRange = null;
            } else if (linewise) {
                selectionRange = lineRange.getRegion(editorAdaptor, NO_COUNT_GIVEN);
            } else {
                selectionRange = StartEndTextRange.exclusive(lineRange.getFrom(), lineRange.getTo());
            }
            editorAdaptor.setNativeSelection(selectionRange);
            action.evaluate(editorAdaptor, tokens);
            editorAdaptor.setSelection(null);
        }
    
    }

    public class RunEvaluatorCommand implements Command {
        private Evaluator mappping = null;
        private Queue<String> tokens = null;
    
        public RunEvaluatorCommand(Evaluator mappping, Queue<String> tokens) {
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
    
    }

    private enum ConfigAction implements Evaluator {

        GLOBAL_REGISTERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.GLOBAL_REGISTERS, Boolean.TRUE);
                vim.useGlobalRegisters();
                return null;
            }
        },
        NO_GLOBAL_REGISTERS {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) {
                vim.getConfiguration().set(Options.GLOBAL_REGISTERS, Boolean.FALSE);
                vim.useLocalRegisters();
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

        public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
            return vim.getConfiguration().get(option)
                 ? onTrue.evaluate(vim, command)
                 : onFalse.evaluate(vim, command);
        }
    }
}
