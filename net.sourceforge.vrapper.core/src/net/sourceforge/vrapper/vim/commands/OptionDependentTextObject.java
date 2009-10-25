package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

// FIXME: fix it when new settings subsystem is done
public class OptionDependentTextObject extends AbstractTextObject {

    private final TextObject onTrue;
    private final TextObject onFalse;
    private final Option<Boolean> option;

    public OptionDependentTextObject(Option<Boolean> option, Motion onTrue, Motion onFalse) {
        this.option = option;
        this.onTrue = new MotionTextObject(onTrue);
        this.onFalse = new MotionTextObject(onFalse);
    }

    public ContentType getContentType(Configuration configuration) {
        if (configuration.get(option).booleanValue())
            return onTrue.getContentType(configuration);
        else
            return onFalse.getContentType(configuration);
    }

    public TextRange getRegion(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        if (editorAdaptor.getConfiguration().get(option).booleanValue())
            return onTrue.getRegion(editorAdaptor, count);
        else
            return onFalse.getRegion(editorAdaptor, count);
    }

}
