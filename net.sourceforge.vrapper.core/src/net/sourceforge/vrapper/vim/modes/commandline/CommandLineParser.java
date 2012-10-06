package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;
import net.sourceforge.vrapper.vim.commands.ExCommandOperation;
import net.sourceforge.vrapper.vim.commands.FindFileCommand;
import net.sourceforge.vrapper.vim.commands.LineRangeOperationCommand;
import net.sourceforge.vrapper.vim.commands.LineWiseSelection;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.EditFileCommand;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.RepeatLastSubstitutionCommand;
import net.sourceforge.vrapper.vim.commands.SaveAllCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.SetOptionCommand;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;
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
        Command save = SaveCommand.INSTANCE;
        Command saveAll = SaveAllCommand.INSTANCE;
        CloseCommand close = CloseCommand.CLOSE;
        Command saveAndClose = new VimCommandSequence(save, close);
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
        mapping = new EvaluatorMapping();
        // options
        mapping.add("set", buildConfigEvaluator());
        // save, close
        mapping.add("w", save);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", close);
        mapping.add("q!", CloseCommand.FORCED_CLOSE);
        mapping.add("wa", saveAll);
        mapping.add("wal", saveAll);
        mapping.add("wall", saveAll);
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
        Evaluator numberToggle = new OptionDependentEvaluator(Options.LINE_NUMBERS, ConfigAction.NO_LINE_NUMBERS, ConfigAction.LINE_NUMBERS);
        Evaluator listToggle = new OptionDependentEvaluator(Options.SHOW_WHITESPACE, ConfigAction.NO_SHOW_WHITESPACE, ConfigAction.SHOW_WHITESPACE);
        config.add("globalregisters", ConfigAction.GLOBAL_REGISTERS);
        config.add("noglobalregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("localregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("nolocalregisters", ConfigAction.GLOBAL_REGISTERS);
        config.add("number", ConfigAction.LINE_NUMBERS);
        config.add("nonumber", ConfigAction.NO_LINE_NUMBERS);
        config.add("nu", ConfigAction.LINE_NUMBERS);
        config.add("nonu", ConfigAction.NO_LINE_NUMBERS);
        config.add("number!", numberToggle);
        config.add("nu!", numberToggle);
        config.add("list", ConfigAction.SHOW_WHITESPACE);
        config.add("nolist", ConfigAction.NO_SHOW_WHITESPACE);
        config.add("list!", listToggle);

        return config;
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
        
        //not a number but starts with a number, $, /, ?, +, -, ', . (dot), or , (comma)
        //might be a line range operation
        if(command.length() > 1 && LineRangeOperationCommand.isLineRangeOperation(command)) {
        	return new LineRangeOperationCommand(command);
        }
        
        //check against list of known commands
        //(the ones without arguments)
        StringTokenizer nizer = new StringTokenizer(command);
        Queue<String> tokens = new LinkedList<String>();
        while (nizer.hasMoreTokens()) {
            tokens.add(nizer.nextToken().trim());
        }
        EvaluatorMapping platformCommands = editor.getPlatformSpecificStateProvider().getCommands();
        if (platformCommands != null && platformCommands.contains(tokens.peek())) {
            platformCommands.evaluate(editor, tokens);
        } else {
            mapping.evaluate(editor, tokens);
        }
        
        //is this a substitution definition?
        Command substitution = parseSubstitution(command);
        if(substitution != null) {
        	return substitution;
        }
        
        //is this an Ex command?
        if(command.length() > 1 && (command.startsWith("g") || command.startsWith("v"))) {
    		return new TextOperationTextObjectCommand(
				new ExCommandOperation(command), new SimpleSelection(null)
    		);
        }
        
        if(command.startsWith("e ")) {
        	//command starts with "e " so filename starts at index 2
        	return new EditFileCommand(command.substring(2));
        }
        if(command.startsWith("find ")) {
        	//command starts with "find " so filename starts at index 5
        	return new FindFileCommand(command.substring(5));
        }
        if(command.startsWith("cd ")) {
        	//command starts with "cd " so directory starts at index 3
        	editor.getRegisterManager().setCurrentWorkingDirectory(command.substring(3));
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
    	if(command.startsWith("s") && !VimUtils.isWordCharacter(""+command.charAt(1))) {
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
