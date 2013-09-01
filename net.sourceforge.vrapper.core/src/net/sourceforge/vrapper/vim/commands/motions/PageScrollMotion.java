package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;

public class PageScrollMotion extends CountAwareMotion {
    public static final Motion SCROLL_PGUP = new PageScrollMotion(Type.PAGE_UP);
    public static final Motion SCROLL_PGDN = new PageScrollMotion(Type.PAGE_DOWN);
    public static final Motion SCROLL_HALF_PGDN = new PageScrollMotion(Type.HALF_PAGE_DOWN);
    public static final Motion SCROLL_HALF_PGUP = new PageScrollMotion(Type.HALF_PAGE_UP);

    public enum Type {
        PAGE_DOWN {
            int calculateTopLine(ViewPortInformation view, int count, int scroll) {
                return view.getTopLine() + (view.getNumberOfLines() + 1) * count;
            }
            int calculateCursorLine(ViewPortInformation view, int current, int scrolloff, int scroll) {
                return view.getTopLine() + scrolloff;
            }
        },

        PAGE_UP {
            int calculateTopLine(ViewPortInformation view, int count, int scroll) {
                return view.getTopLine() - (view.getNumberOfLines() + 1) * count;
            }
            int calculateCursorLine(ViewPortInformation view, int current, int scrolloff, int scroll) {
                int line = view.getBottomLine() - scrolloff;
                if (line >= current || line <= 0) {
                    return 0;
                } else {
                    return line;
                }
            }
        },

        HALF_PAGE_DOWN {
            int calculateTopLine(ViewPortInformation view, int count, int scroll) {
                return view.getTopLine() + scroll;
            }
            int calculateCursorLine(ViewPortInformation view, int current, int scrolloff, int scroll) {
                return current + scroll;
            }
            boolean updateScrollOption() { return true; }
        },

        HALF_PAGE_UP {
            int calculateTopLine(ViewPortInformation view, int count, int scroll) {
                return view.getTopLine() - scroll;
            }
            int calculateCursorLine(ViewPortInformation view, int current, int scrolloff, int scroll) {
                return current - scroll;
            }
            boolean updateScrollOption() { return true; }
        };

        abstract int calculateTopLine(ViewPortInformation view, int count, int scroll);
        abstract int calculateCursorLine(ViewPortInformation view, int current, int scrolloff, int scroll);
        boolean updateScrollOption() { return false; }
    };

    private final Type type;

    private PageScrollMotion(Type type) {
        super();
        this.type = type;
    }

    public BorderPolicy borderPolicy() {
        return BorderPolicy.LINE_WISE;
    }

    public StickyColumnPolicy stickyColumnPolicy() {
        return StickyColumnPolicy.ON_CHANGE;
    }

    @Override
    public Position destination(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        TextContent viewContent = editorAdaptor.getViewContent();
        if (viewContent.getTextLength() == 0)
            return null;
        ViewportService viewport = editorAdaptor.getViewportService();
        ViewPortInformation view = viewport.getViewPortInformation();
        Configuration config = editorAdaptor.getConfiguration();
        if (type.updateScrollOption() && count != NO_COUNT_GIVEN) {
            config.set(Options.SCROLL, count);
        }
        int scroll = config.get(Options.SCROLL);
        if (scroll == 0) {
            scroll = view.getNumberOfLines() / 2;
        }
        if (count == NO_COUNT_GIVEN) {
            count = 1;
        }
        Position pos = editorAdaptor.getCursorService().getPosition();
        int lastChar = viewContent.getTextLength() - 1;
        int lastLineNumber = viewContent.getLineInformationOfOffset(lastChar).getNumber();
        LineInformation line = viewContent.getLineInformationOfOffset(pos.getViewOffset());
        int currenCursorLine = line.getNumber();
        int scrolloff = config.get(Options.SCROLL_OFFSET);
        int topline = type.calculateTopLine(view, count, scroll);
        int newModelLine;
        if (topline >= lastLineNumber) {
            newModelLine = viewport.viewLine2ModelLine(lastLineNumber) + 1;
        } else {
            editorAdaptor.getViewportService().setTopLine(Math.max(0, topline));
            view = editorAdaptor.getViewportService().getViewPortInformation();
            int cursorViewLine = type.calculateCursorLine(view, currenCursorLine, scrolloff, scroll);
            int newCursorViewLine = Math.min(lastLineNumber + 1, Math.max(0, cursorViewLine));
            newModelLine = viewport.viewLine2ModelLine(newCursorViewLine);
        }
        Position newpos = editorAdaptor.getCursorService().stickyColumnAtModelLine(newModelLine);
        return newpos;
//        editorAdaptor.getCursorService().setPosition(newpos, false);
    }


}
