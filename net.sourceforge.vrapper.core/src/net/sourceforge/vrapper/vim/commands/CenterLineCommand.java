package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.ViewportService;
import net.sourceforge.vrapper.utils.ViewPortInformation;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.ViewPortMotion;

public class CenterLineCommand extends CountIgnoringNonRepeatableCommand {

    public void execute(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        ViewportService view = editorAdaptor.getViewportService();
        ViewPortInformation info = view.getViewPortInformation();
        int middle = ViewPortMotion.Type.MIDDLE.calculateLine(info);
        int line = editorAdaptor.getViewContent().getLineInformationOfOffset(
                editorAdaptor.getPosition().getViewOffset()).getNumber();
        int offset = line - middle;
        int target = Math.max(info.getTopLine()+offset, 0);
        view.setTopLine(target);
    }

}
