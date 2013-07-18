package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class GoToMarkMotion implements Motion {

    public static final Function<Motion, KeyStroke> LINEWISE_CONVERTER = new Function<Motion, KeyStroke>() {
        public Motion call(KeyStroke arg) {
            return new GoToMarkMotion(true, String.valueOf(arg.getCharacter()));
        }
    };

    public static final Function<Motion, KeyStroke> CHARWISE_CONVERTER = new Function<Motion, KeyStroke>() {
        public Motion call(KeyStroke arg) {
            return new GoToMarkMotion(false, String.valueOf(arg.getCharacter()));
        }
    };

    private final boolean lineWise;
    private final String id;

    public GoToMarkMotion(boolean lineWise, String id) {
        this.lineWise = lineWise;
        this.id = id;
    }

    public Position destination(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        Position markPos = editorAdaptor.getCursorService().getMark(id);
        if (markPos == null) {
            throw new CommandExecutionException("Mark not set");
        }
        if (lineWise) {
            TextContent tc = editorAdaptor.getModelContent();
            LineInformation line = tc.getLineInformationOfOffset(markPos.getModelOffset());
            int offset = VimUtils.getFirstNonWhiteSpaceOffset(tc, line);
            editorAdaptor.getCursorService().setMark(CursorService.LAST_JUMP_MARK, editorAdaptor.getPosition());
            return editorAdaptor.getCursorService().newPositionForModelOffset(offset);
        }
        return markPos;
    }

    public boolean updateStickyColumn() {
        return true;
    }

    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    public Motion withCount(int count) {
        return this;
    }

    public BorderPolicy borderPolicy() {
        return lineWise ? BorderPolicy.LINE_WISE : BorderPolicy.EXCLUSIVE;
    }

    public boolean isJump() {
        return true;
    }

}
