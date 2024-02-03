package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Moves to the first, last or middle visible line.
 *
 * @author Matthias Radig
 */
public class ViewPortMotion extends GoToLineMotion {

    public static final ViewPortMotion HIGH = new ViewPortMotion(Type.HIGH);
    public static final ViewPortMotion MIDDLE = new ViewPortMotion(Type.MIDDLE);
    public static final ViewPortMotion LOW = new ViewPortMotion(Type.LOW);
    private final Type type;

    private ViewPortMotion(Type type) {
        super();
        this.type = type;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count, Position fromPosition)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        ViewPortInformation view = editorAdaptor.getViewportService().getViewPortInformation();
        int scrolloff = editorAdaptor.getConfiguration().get(Options.SCROLL_OFFSET);
        int line = type.calculateLine(view, scrolloff);
        int offset = type.calculateOffset(count);
        int result = line+offset;
        result = Math.max(result, view.getTopLine());
        result = Math.min(result, view.getBottomLine());
        // add 1 to result as GoToLineMotion is 1-based
        int dest = editorAdaptor.getViewportService().viewLine2ModelLine(result);
        return super.destination(editorAdaptor, dest+1, fromPosition);
    }

    public enum Type {
        HIGH {
            @Override
            public int calculateLine(ViewPortInformation view, int scrolloff) {
                return view.getTopLine()+scrolloff;
            }

            @Override
            int calculateOffset(int times) {
                return times - 1;
            }
        },
        MIDDLE {
            @Override
            public int calculateLine(ViewPortInformation view, int scrolloff) {
                return view.getTopLine() + view.getNumberOfLines()/2;
            }

            @Override
            int calculateOffset(int times) {
                return 0;
            }
        },
        LOW {
            @Override
            public int calculateLine(ViewPortInformation view, int scrolloff) {
                return view.getBottomLine() - scrolloff;
            }

            @Override
            int calculateOffset(int times) {
                return 1 - times;
            }
        };

        public abstract int calculateLine(ViewPortInformation view, int scrolloff);

        abstract int calculateOffset(int times);

    }

}
