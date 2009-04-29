package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.StringUtils;

/**
 * Replaces the character at the current position with another one.
 *
 * @author Matthias Radig
 */
public class ReplaceCommand extends AbstractModelSideCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            return new ReplaceCommand(arg.getCharacter());
        }
    };

    private final char replacement;

    public ReplaceCommand(char replacement) {
        super();
        this.replacement = replacement;
    }

    @Override
    protected int execute(TextContent c, int offset, int count) {
        LineInformation line = c.getLineInformationOfOffset(offset);
        int targetOffset = offset + count - 1;
        if (targetOffset < line.getEndOffset()) {
            String s = StringUtils.multiply(""+replacement, count);
            c.replace(offset, s.length(), s);
            return targetOffset;
        }
        return offset;
    }

}
