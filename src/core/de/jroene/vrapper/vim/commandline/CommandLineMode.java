package de.jroene.vrapper.vim.commandline;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.action.Action;
import de.jroene.vrapper.vim.action.CloseAction;
import de.jroene.vrapper.vim.action.CompositeAction;
import de.jroene.vrapper.vim.action.ConfigAction;
import de.jroene.vrapper.vim.action.SaveAction;


/**
 * Command Line Mode, activated with ':'.
 * 
 * @author Matthias Radig
 */
public class CommandLineMode extends AbstractCommandMode {

    private static final EvaluatorMapping mapping;
    static {
        mapping = new EvaluatorMapping();
        Action save = new SaveAction();
        mapping.add("w", save);
        CloseAction close = new CloseAction(false);
        Action saveAndClose = new CompositeAction(save, close);
        mapping.add("wq", saveAndClose);
        mapping.add("x", saveAndClose);
        mapping.add("q", close);
        mapping.add("q!", new CloseAction(true));
        mapping.add("set", buildConfigEvaluator());
        Evaluator remap = new KeyMapper();
        mapping.add("no", remap);
        mapping.add("noremap", remap);
    }

    private static Evaluator buildConfigEvaluator() {
        EvaluatorMapping config = new EvaluatorMapping();
        config.add("autoindent", ConfigAction.AUTO_INDENT);
        config.add("noautoindent", ConfigAction.NO_AUTO_INDENT);
        config.add("globalregisters", ConfigAction.GLOBAL_REGISTERS);
        config.add("noglobalregisters", ConfigAction.LOCAL_REGISTERS);
        config.add("linewisemouse", ConfigAction.LINE_WISE_MOUSE_SELECTION);
        config.add("nolinewisemouse", ConfigAction.NO_LINE_WISE_MOUSE_SELECTION);
        return config;
    }

    public CommandLineMode(VimEmulator vim) {
        super(vim);
    }

    @Override
    public void parseAndExecute(String first, String command) {
        StringTokenizer nizer = new StringTokenizer(command);
        List<String> tokens = new ArrayList<String>();
        while (nizer.hasMoreTokens()) {
            tokens.add(nizer.nextToken().trim());
        }
        mapping.evaluate(vim, tokens.iterator());
    }
}
