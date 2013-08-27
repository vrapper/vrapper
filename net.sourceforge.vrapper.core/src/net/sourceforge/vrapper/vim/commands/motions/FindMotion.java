package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/** Motion responsible for 't', 'T', 'f' and 'F' motions.
 * Finds next occurrence of a character in current line.
 * 
 * @author Krzysiek Goj
 */
public class FindMotion extends FindBalancedMotion {

    public static Function<Motion, KeyStroke> keyConverter(final boolean upToTarget, final boolean reversed) {
        return new Function<Motion, KeyStroke>() {
            public Motion call(KeyStroke keyStroke) {
                return new FindMotion(keyStroke.getCharacter(), upToTarget, reversed).registrator;
            }
        };

    }

    public FindMotion(char target, boolean upToTarget, boolean reversed) {
        super(target, '\0', upToTarget, reversed);
    }

    @Override
    protected int getEndSearchOffset(TextContent content, int offset) {
        LineInformation line = content.getLineInformationOfOffset(offset);
        int end = backwards ? line.getBeginOffset() : line.getEndOffset() - 1;
        return end;
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

        public StickyColumnPolicy stickyColumnPolicy() {
            return FindMotion.this.stickyColumnPolicy();
        }
    };

}
