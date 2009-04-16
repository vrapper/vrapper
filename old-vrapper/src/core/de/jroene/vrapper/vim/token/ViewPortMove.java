package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.vim.ViewPortInformation;
import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimUtils;

public class ViewPortMove extends AbstractRepeatableVerticalMove {

    private final Type type;

    public ViewPortMove(Type type) {
        super();
        this.type = type;
    }

    @Override
    public int calculateTarget(VimEmulator vim, int times, Token next) {
        ViewPortInformation view = vim.getPlatform().getViewPortInformation();
        int line = type.calculateLine(view);
        int offset = type.calculateOffset(times);
        return VimUtils.getSOLAwarePositionAtLine(vim, line + offset);
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
