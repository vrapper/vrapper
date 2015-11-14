package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class PrintOffsetInformation implements Command {

    public static final Command INSTANCE = new PrintOffsetInformation();

    @Override
    public Command withCount(int count) {
        return this;
    }

    @Override
    public int getCount() {
        return NO_COUNT_GIVEN;
    }

    @Override
    public Command repetition() {
        return null;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        Position position = editorAdaptor.getPosition();
        UserInterfaceService service = editorAdaptor.getUserInterfaceService();
        int visualOffset = editorAdaptor.getCursorService().getVisualOffset(position);
        service.setInfoSet(true);
        LineInformation modelLine = editorAdaptor.getModelContent().getLineInformationOfOffset(position.getModelOffset());
        LineInformation viewLine = editorAdaptor.getViewContent().getLineInformationOfOffset(position.getViewOffset());
        service.setLastCommandResultValue("Position M " + position.getModelOffset() + " / "
        + position.getViewOffset() + " V; line " + modelLine.getNumber() + " / "
                + viewLine.getNumber() + " view line; horizontal offset " + visualOffset);
        // TODO Print extra information about selection in case of visual mode.
        // Better off in a seperate command or sub-mode
    }
}