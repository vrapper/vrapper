package net.sourceforge.vrapper.vim.modes.commandline;

import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CloseCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.RedoCommand;
import net.sourceforge.vrapper.vim.commands.SaveCommand;
import net.sourceforge.vrapper.vim.commands.UndoCommand;
import net.sourceforge.vrapper.vim.commands.VimCommandSequence;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

/**
 * Command Line Mode, activated with ':'.
 *
 * @author Matthias Radig
 */
// TODO: still a lot to do here :-D
public class CommandLineParser extends AbstractCommandParser {

    private static final EvaluatorMapping mapping;
    static {
        mapping = new EvaluatorMapping();
        Command save = new SaveCommand();
        mapping.add("w", save);
        CloseCommand close = new CloseCommand(false);
        Command saveAndClose = new VimCommandSequence(save, close);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", close);
        mapping.add("q!", new CloseCommand(true));
        //        mapping.add("set", buildConfigEvaluator());
        Evaluator noremap = new KeyMapper(false,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator map = new KeyMapper(true,
                AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME);
        Evaluator nnoremap = new KeyMapper(false, NormalMode.KEYMAP_NAME);
        Evaluator nmap = new KeyMapper(true, NormalMode.KEYMAP_NAME);
        mapping.add("map", map);
        mapping.add("nmap", nmap);
        mapping.add("no", noremap);
        mapping.add("noremap", noremap);
        mapping.add("nno", nnoremap);
        mapping.add("nnoremap", nnoremap);
        //        Action formatAll = new FormatAllAction();
        //        mapping.add("formatall", formatAll);
        //        mapping.add("format", formatAll);
        //        mapping.add("fm", formatAll);
        UndoCommand undo = new UndoCommand();
        RedoCommand redo = new RedoCommand();
        mapping.add("red", redo);
        mapping.add("redo", redo);
        mapping.add("undo", undo);
        mapping.add("u", undo);
        //        mapping.add("$", new TokenWrapper(new GotoMove(true)));
    }
    //
    //    private static Evaluator buildConfigEvaluator() {
    //        EvaluatorMapping config = new EvaluatorMapping();
    //        config.add("autoindent", ConfigAction.AUTO_INDENT);
    //        config.add("noautoindent", ConfigAction.NO_AUTO_INDENT);
    //        config.add("smartindent", ConfigAction.SMART_INDENT);
    //        config.add("nosmartindent", ConfigAction.NO_SMART_INDENT);
    //        config.add("globalregisters", ConfigAction.GLOBAL_REGISTERS);
    //        config.add("noglobalregisters", ConfigAction.LOCAL_REGISTERS);
    //        config.add("linewisemouse", ConfigAction.LINE_WISE_MOUSE_SELECTION);
    //        config.add("nolinewisemouse", ConfigAction.NO_LINE_WISE_MOUSE_SELECTION);
    //        config.add("startofline", ConfigAction.START_OF_LINE);
    //        config.add("nostartofline", ConfigAction.NO_START_OF_LINE);
    //        config.add("sol", ConfigAction.START_OF_LINE);
    //        config.add("nosol", ConfigAction.NO_START_OF_LINE);
    //        return config;
    //    }

    public CommandLineParser(EditorAdaptor vim) {
        super(vim);
    }

    @Override
    public void parseAndExecute(String first, String command) {
        //        try {
        //            // if the command is a number, jump to the given line
        //            Integer.parseInt(command);
        //        } catch (NumberFormatException e) {
        //            // do nothing
        //        }
        StringTokenizer nizer = new StringTokenizer(command);
        Queue<String> tokens = new LinkedList<String>();
        while (nizer.hasMoreTokens()) {
            tokens.add(nizer.nextToken().trim());
        }
        mapping.evaluate(editor, tokens);
    }
}
