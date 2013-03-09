package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.AsciiCommand;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;
import net.sourceforge.vrapper.vim.commands.EditFileCommand;
import net.sourceforge.vrapper.vim.commands.ExCommandOperation;
import net.sourceforge.vrapper.vim.commands.FindFileCommand;
import net.sourceforge.vrapper.vim.commands.LineRangeOperationCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.RepeatLastSubstitutionCommand;
import net.sourceforge.vrapper.vim.commands.SaveAllCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.SetOptionCommand;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
import net.sourceforge.vrapper.vim.commands.SortOperation;
import net.sourceforge.vrapper.vim.commands.SubstitutionOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
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
    static {
        Evaluator noremap = new KeyMapper.Map(false,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator map = new KeyMapper.Map(true,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nnoremap = new KeyMapper.Map(false, NormalMode.KEYMAP_NAME);
        Evaluator nmap = new KeyMapper.Map(true, NormalMode.KEYMAP_NAME);
        Evaluator vnoremap = new KeyMapper.Map(false, VisualMode.KEYMAP_NAME);
        Evaluator vmap = new KeyMapper.Map(true, VisualMode.KEYMAP_NAME);
        Evaluator inoremap = new KeyMapper.Map(false, InsertMode.KEYMAP_NAME);
        Evaluator imap = new KeyMapper.Map(true, InsertMode.KEYMAP_NAME);
        Command ascii = AsciiCommand.INSTANCE;
        Command save = SaveCommand.INSTANCE;
        Command saveAll = SaveAllCommand.INSTANCE;
        CloseCommand close = CloseCommand.CLOSE;
        CloseCommand closeAll = CloseCommand.CLOSE_ALL;
        Command saveAndClose = new VimCommandSequence(save, close);
        Command saveAndCloseAll = new VimCommandSequence(saveAll, closeAll);
        Evaluator unmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nunmap = new KeyMapper.Unmap(NormalMode.KEYMAP_NAME);
        Evaluator vunmap = new KeyMapper.Unmap(AbstractVisualMode.KEYMAP_NAME);
        Evaluator iunmap = new KeyMapper.Unmap(InsertMode.KEYMAP_NAME);
        Evaluator clear = new KeyMapper.Clear(AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nclear = new KeyMapper.Clear(NormalMode.KEYMAP_NAME);
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
            	while(command.size() > 0)
            		//attempt to preserve spacing in case a pattern is in use
            		//(if you attempt to sort with "/foo    bar/" it won't work)
            		commandStr += command.poll() + " ";
        		
            	try {
					new SortOperation(commandStr).execute(vim, null, ContentType.LINES);
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
        
        /* TODO: Write an interpreter that will read partial commands
         * example: :wall can be invoked by typing any of the following:
         *     :wa
         *     :wal
         *     :wall
         */
        mapping = new EvaluatorMapping();
        // options
        mapping.add("set", buildConfigEvaluator());
        mapping.add("source", sourceConfigFile);
        // save, close
        mapping.add("w", save);
    	mapping.add("up", save);
    	mapping.add("update", save);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", close);
        mapping.add("q!", CloseCommand.FORCED_CLOSE);
        mapping.add("bdelete", close);
        mapping.add("bdelete!", CloseCommand.FORCED_CLOSE);
        mapping.add("bd", close);
        mapping.add("bd!", CloseCommand.FORCED_CLOSE);
        mapping.add("qa", closeAll);
        mapping.add("qa!", CloseCommand.FORCED_CLOSE_ALL);
        mapping.add("qall", closeAll);
        mapping.add("qall!", CloseCommand.FORCED_CLOSE_ALL);
        mapping.add("quitall", closeAll);
        mapping.add("quitall!", CloseCommand.FORCED_CLOSE_ALL);
        mapping.add("wa", saveAll);
        mapping.add("wal", saveAll);
        mapping.add("wall", saveAll);
        mapping.add("wqa", saveAndCloseAll);
        mapping.add("wqal", saveAndCloseAll);
        mapping.add("wqall", saveAndCloseAll);
        mapping.add("only", CloseCommand.CLOSE_OTHERS);
        mapping.add("tabo", CloseCommand.CLOSE_OTHERS);
        mapping.add("tabonly", CloseCommand.CLOSE_OTHERS);
        mapping.add("only!", CloseCommand.FORCED_CLOSE_OTHERS);
        // non-recursive mapping
        mapping.add("noremap", noremap);
        mapping.add("no", noremap);
        mapping.add("nnoremap", nnoremap);
        mapping.add("nn", nnoremap);
        mapping.add("inoremap", inoremap);
        mapping.add("ino", inoremap);
        mapping.add("vnoremap", vnoremap);
        mapping.add("vn", vnoremap);
        // recursive mapping
        mapping.add("map", map);
        mapping.add("nmap", nmap);
        mapping.add("nm", nmap);
        mapping.add("imap", imap);
        mapping.add("im", imap);
        mapping.add("vmap", vmap);
        mapping.add("vm", vmap);
        // unmapping
        mapping.add("unmap", unmap);
        mapping.add("unm", unmap);
        mapping.add("nunmap", nunmap);
        mapping.add("nun", nunmap);
        mapping.add("vunmap", vunmap);
        mapping.add("vu", vunmap);
        mapping.add("iunmap", iunmap);
        mapping.add("iu", iunmap);
        // clearing maps
        mapping.add("mapclear", clear);
        mapping.add("mapc", clear);
        mapping.add("nmapclear", nclear);
        mapping.add("nmapc", nclear);
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
        mapping.add("e", editFile);
        mapping.add("find", findFile);
        mapping.add("tabf", findFile);
        mapping.add("tabfind", findFile);
        mapping.add("cd", chDir);
        // Sort lines in the file based on ascii values
        mapping.add("sor", sort);
        mapping.add("sort", sort);
        mapping.add("sort!", sort);
        // Display the ascii values of the character under the cursor
    	mapping.add("as",    ascii);
    	mapping.add("ascii", ascii);
    }

    private static Evaluator buildConfigEvaluator() {
        EvaluatorMapping config = new EvaluatorMapping(new ComplexOptionEvaluator());
        // boolean options
        for (Option<Boolean> o: Options.BOOLEAN_OPTIONS) {
            ConfigCommand<Boolean> enable = new SetOptionCommand<Boolean>(o, Boolean.TRUE);
            ConfigCommand<Boolean> disable = new SetOptionCommand<Boolean>(o, Boolean.FALSE);
            ConfigCommand<Boolean> toggle = new ToggleOptionCommand(o);
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
    }

    @Override
    public Command parseAndExecute(String first, String command) {
        try {
            // if the command is a number, jump to the given line
            int line = Integer.parseInt(command);
            return new MotionCommand(GoToLineMotion.FIRST_LINE.withCount(line));
        } catch (NumberFormatException e) {
            // do nothing
        }
        
        /** First, check for all operations which are not whitespace-delimited **/
       
        //not a number but starts with a number, $, /, ?, +, -, ', . (dot), or , (comma)
        //might be a line range operation
        if(command.length() > 1 && LineRangeOperationCommand.isLineRangeOperation(command))
        	return new LineRangeOperationCommand(command);
        
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
        
        // Reverse sort toggle will be treated as just another argument
        if(command.startsWith("sort!")) {
        	command = command.replace("sort!", "sort !");
        }
        
        /** Now check against list of known commands (whitespace-delimited) **/
        
        //tokenize based on whitespace
        StringTokenizer nizer = new StringTokenizer(command);
        Queue<String> tokens = new LinkedList<String>();
        while(nizer.hasMoreTokens())
            tokens.add(nizer.nextToken().trim());
        
        //see if a command is defined for the first token
        EvaluatorMapping platformCommands = editor.getPlatformSpecificStateProvider().getCommands();
        if(platformCommands != null && platformCommands.contains(tokens.peek()))
            platformCommands.evaluate(editor, tokens);
        else
            mapping.evaluate(editor, tokens);
        
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
    	else if(command.startsWith("%s")) { //global substitution
    		Position start = editor.getCursorService().newPositionForModelOffset( 0 );
    		Position end = editor.getCursorService().newPositionForModelOffset( editor.getModelContent().getTextLength() );
    		return new TextOperationTextObjectCommand(
				new SubstitutionOperation(command), new LineWiseSelection(editor, start, end)
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
