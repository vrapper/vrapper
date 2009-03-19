package de.jroene.vrapper.vim.commandline;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import de.jroene.vrapper.vim.VimEmulator;
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
        Evaluator save = new ActionWrapper(new SaveAction());
        mapping.add("w", save);
        mapping.add("wq", save);
        mapping.add("x", save);
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
