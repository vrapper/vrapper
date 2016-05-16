package net.sourceforge.vrapper.plugin.subwordtextobj.provider;

import static net.sourceforge.vrapper.utils.CollectionUtils.asQueue;

import java.util.Queue;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;
import net.sourceforge.vrapper.vim.modes.commandline.KeyMapper;

public class SubwordMappingsSetupEvaluator implements Evaluator {
    private final static Evaluator MOTION_MAP_EVALUATOR = new KeyMapper.Map(false,
            AbstractVisualMode.KEYMAP_NAME, NormalMode.KEYMAP_NAME, KeyMapResolver.OMAP_NAME);
    private final static Evaluator TEXTOBJECT_MAP_EVALUATOR = new KeyMapper.Map(false,
            AbstractVisualMode.KEYMAP_NAME, KeyMapResolver.OMAP_NAME);

    @Override
    public Object evaluate(EditorAdaptor vim, Queue<String> command)
            throws CommandExecutionException {

        String mappingPrefix;
        String textObjId;
        if (command.isEmpty()) {
            mappingPrefix = "<Leader>";
            textObjId = "<Leader>";
        } else if (command.size() == 1) {
            mappingPrefix = command.poll().trim();
            textObjId = mappingPrefix;
        } else {
            mappingPrefix = command.poll().trim();
            textObjId = command.poll().trim();
        }
        if ("\"\"".equals(mappingPrefix) || "''".equals(mappingPrefix)) {
            mappingPrefix = "";
        }
        if ("\"\"".equals(textObjId) || "''".equals(textObjId) || textObjId.isEmpty()) {
            // This can't be empty
            textObjId = "<Leader>";
        }

        MOTION_MAP_EVALUATOR.evaluate(vim, asQueue(mappingPrefix + "w", "<Plug>(subword-word)"));
        MOTION_MAP_EVALUATOR.evaluate(vim, asQueue(mappingPrefix + "b", "<Plug>(subword-back)"));
        MOTION_MAP_EVALUATOR.evaluate(vim, asQueue(mappingPrefix + "e", "<Plug>(subword-end)"));

        TEXTOBJECT_MAP_EVALUATOR.evaluate(vim, asQueue("i" + textObjId, "<Plug>(subword-inner)"));
        TEXTOBJECT_MAP_EVALUATOR.evaluate(vim, asQueue("a" + textObjId, "<Plug>(subword-outer)"));
        return null;
    }
}