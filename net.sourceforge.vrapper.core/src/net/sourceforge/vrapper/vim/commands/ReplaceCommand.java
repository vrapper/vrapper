package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * Replaces the character at the current position with another one.
 *
 * @author Matthias Radig
 */
public abstract class ReplaceCommand extends AbstractModelSideCommand {

    public static final Function<Command, KeyStroke> KEYSTROKE_CONVERTER = new Function<Command, KeyStroke>() {
        public Command call(KeyStroke arg) {
            if (SpecialKey.RETURN.equals(arg.getSpecialKey())) {
                return Newline.INSTANCE;
            }
            return new Character(arg.getCharacter());
        }
    };

    @Override
    protected int execute(TextContent c, int offset, int count) {
        LineInformation line = c.getLineInformationOfOffset(offset);
        int targetOffset = offset + count - 1;
        if (targetOffset < line.getEndOffset()) {
            return replace(c, offset, count, targetOffset);
        }
        return offset;
    }

    abstract int replace(TextContent c, int offset, int count, int targetOffset);

    private static class Character extends ReplaceCommand {

        private final char replacement;

        public Character(char replacement) {
            super();
            this.replacement = replacement;
        }

        @Override
        protected int replace(TextContent c, int offset, int count, int targetOffset) {
            String s = StringUtils.multiply(""+replacement, count);
            c.replace(offset, s.length(), s);
            return targetOffset;
        }
    }

    private static class Newline extends ReplaceCommand {

        private static final Newline INSTANCE = new Newline();

        private String newLine;
        private boolean smartIndent;
        private boolean autoIndent;

        @Override
        public void execute(EditorAdaptor editorAdaptor)
                throws CommandExecutionException {
            // hack to access the configuration
            Configuration conf = editorAdaptor.getConfiguration();
            newLine = conf.getNewLine();
            smartIndent = conf.isSmartIndent();
            autoIndent = conf.isAutoIndent();
            super.execute(editorAdaptor);
        }

        @Override
        int replace(TextContent c, int offset, int count, int targetOffset) {
            LineInformation line = c.getLineInformationOfOffset(offset);
            if (smartIndent) {
                c.replace(offset, targetOffset-offset+1, "");
                c.smartInsert(offset, newLine);
            } else {
                String indent = autoIndent ? VimUtils.getIndent(c, line) : "";
                c.replace(offset, targetOffset-offset+1, newLine+indent);
            }
            LineInformation nextLine = c.getLineInformation(line.getNumber()+1);
            return VimUtils.getFirstNonWhiteSpaceOffset(c, nextLine);
        }

    }

}
