package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class FindMotion extends AbstractModelSideMotion {

    private final char target;
    private final boolean upToTarget;
    private final boolean backwards;

    public static Function<Motion, KeyStroke> keyConverter(final boolean upToTarget, final boolean reversed) {
        return new Function<Motion, KeyStroke>() {
            public Motion call(KeyStroke keyStroke) {
                return new FindMotion(keyStroke.getCharacter(), upToTarget, reversed).registrator;
            }
        };

    }

    public FindMotion(char target, boolean upToTarget, boolean reversed) {
        this.target = target;
        this.upToTarget = upToTarget;
        this.backwards = reversed;
    }

    @Override
    protected int destination(int offset, TextContent content, int count) throws CommandExecutionException {
        LineInformation line = content.getLineInformationOfOffset(offset);
        int end = backwards ? line.getBeginOffset() : line.getEndOffset() - 1;
        int step = backwards ? -1 : 1;
        for(int n = 0; n < count; n++) {
            while (offset != end) {
                offset += step;
                if(content.getText(offset, 1).charAt(0) == target)
                    break;
            }
        }
        if(offset >= content.getTextLength() || content.getText(offset, 1).charAt(0) != target) {
            throw new CommandExecutionException("'" + target + "' not found");
        }
        if(!upToTarget) {
            offset -= step;
        }
        return offset;
    }

    public BorderPolicy borderPolicy() {
        return backwards ? BorderPolicy.EXCLUSIVE : BorderPolicy.INCLUSIVE;
    }

    public FindMotion reversed() {
        return new FindMotion(target, upToTarget, !backwards);
    }

    private Motion registrator = new CountAwareMotion() {
        @Override
        public Position destination(EditorAdaptor editorAdaptor, int count)
                throws CommandExecutionException {
            editorAdaptor.getRegisterManager().setLastFindMotion(FindMotion.this);
            return FindMotion.this.destination(editorAdaptor, count);
        }

        public BorderPolicy borderPolicy() {
            return FindMotion.this.borderPolicy();
        }

        public boolean updateStickyColumn() {
            return FindMotion.this.updateStickyColumn();
        }
    };

}
