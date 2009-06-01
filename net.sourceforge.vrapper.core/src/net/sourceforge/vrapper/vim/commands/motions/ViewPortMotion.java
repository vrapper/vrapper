package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

/**
 * Moves to the first, last or middle visible line.
 *
 * @author Matthias Radig
 */
public class ViewPortMotion extends GoToLineMotion {

    private final Type type;

    public ViewPortMotion(Type type) {
        super();
        this.type = type;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        ViewPortInformation view = editorAdaptor.getViewportService().getViewPortInformation();
        int line = type.calculateLine(view);
        int offset = type.calculateOffset(count);
        int result = line+offset;
        result = Math.max(result, view.getTopLine());
        result = Math.min(result, view.getBottomLine());
        // add 1 to result as GoToLineMotion is 1-based
        return super.destination(editorAdaptor, result+1);
    }

    public enum Type {
        HIGH {
            @Override
            int calculateLine(ViewPortInformation view) {
                return view.getTopLine();
            }

            @Override
            int calculateOffset(int times) {
                return times - 1;
            }
        },
        MIDDLE {
            @Override
            int calculateLine(ViewPortInformation view) {
                return view.getTopLine()+view.getNumberOfLines()/2;
            }

            @Override
            int calculateOffset(int times) {
                return 0;
            }
        },
        LOW {
            @Override
            int calculateLine(ViewPortInformation view) {
                return view.getBottomLine();
            }

            @Override
            int calculateOffset(int times) {
                return 1 - times;
            }
        };

        abstract int calculateLine(ViewPortInformation view);

        abstract int calculateOffset(int times);

    }

}
