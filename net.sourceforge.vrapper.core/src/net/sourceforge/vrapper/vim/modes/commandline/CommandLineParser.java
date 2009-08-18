package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.ConfigCommand;
import net.sourceforge.vrapper.vim.commands.MotionCommand;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
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
        mapping = new EvaluatorMapping();
        // options
        mapping.add("set", buildConfigEvaluator());
        // save, close
        mapping.add("w", save);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", close);
        mapping.add("q!", CloseCommand.FORCED_CLOSE);
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
    }

    private static Evaluator buildConfigEvaluator() {
        EvaluatorMapping config = new EvaluatorMapping();
        // boolean options
        for (Option<Boolean> o : Options.BOOLEAN_OPTIONS) {
            ConfigCommand<Boolean> enable = new ConfigCommand<Boolean>(o, Boolean.TRUE);
            ConfigCommand<Boolean> disable = new ConfigCommand<Boolean>(o, Boolean.FALSE);
            config.add(o.getId(), enable);
            config.add("no"+o.getId(), disable);
            for (String alias : o.getAlias()) {
                config.add(alias, enable);
                config.add("no"+alias, disable);
            }
        }
        config.add("globalregisters", ConfigAction.GLOBAL_REGISTERS);
        config.add("noglobalregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("localregisters", ConfigAction.NO_GLOBAL_REGISTERS);
        config.add("nolocalregisters", ConfigAction.GLOBAL_REGISTERS);
        return config;
    }

    public CommandLineParser(EditorAdaptor vim) {
        super(vim);
    }

    @Override
    public void parseAndExecute(String first, String command) {
        try {
            // if the command is a number, jump to the given line
            int line = Integer.parseInt(command);
            MotionCommand.doIt(editor, GoToLineMotion.FIRST_LINE.withCount(line));
            return;
        } catch (NumberFormatException e) {
            // do nothing
        } catch (CommandExecutionException e) {
            editor.getUserInterfaceService().setErrorMessage(e.getMessage());
            return;
        }
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
        };
    }
}
