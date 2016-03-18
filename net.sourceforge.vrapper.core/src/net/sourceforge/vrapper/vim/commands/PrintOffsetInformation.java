package net.sourceforge.vrapper.vim.commands;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.VimUtils;
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
        CursorService cursorService = editorAdaptor.getCursorService();
        TextContent modelContent = editorAdaptor.getModelContent();
        int modelOffset = position.getModelOffset();
        LineInformation modelLine = modelContent.getLineInformationOfOffset(modelOffset);
        LineInformation viewLine = editorAdaptor.getViewContent().getLineInformationOfOffset(position.getViewOffset());

        Pattern pattern = Pattern.compile("[\\S]+");
        int totalWords = 0;
        String modelText = modelContent.getText(0, modelContent.getTextLength());
        Matcher matcher = pattern.matcher(modelText);
        String wholeWord = VimUtils.getWordUnderCursor(editorAdaptor, true);

        int wordNumber = 0;
        String byteOffset = "";
        while (matcher.find()) {
            totalWords++;
            if (matcher.group().equals(wholeWord) && matcher.start() <= modelOffset && modelOffset <= matcher.end()) {
                wordNumber = totalWords;
                byteOffset = modelContent.getText(0, position.addModelOffset(1).getModelOffset());
            }
            matcher.start();
        }

        int totalBytes = 0;
        int byteNumber = 0;
        try {
            totalBytes = modelText.getBytes("UTF-8").length;
            byteNumber = byteOffset.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            VrapperLog.info(e.getMessage());
        }

        service.setInfoSet(true);
        int colNumber = VimUtils.calculateColForPosition(modelContent, position) + 1;
        int lineLength = VimUtils.calculateColForPosition(modelContent, cursorService.newPositionForModelOffset(modelLine.getEndOffset()));

        service.setLastCommandResultValue(
                          "Col " + colNumber + " of " + lineLength + "; "
                        + "Line " + (modelLine.getNumber() + 1) + " of " + modelContent.getNumberOfLines() + "; "
                        + "Word " + wordNumber + " of " + totalWords + "; "
                        + "Byte " + byteNumber + " of " + totalBytes + "; "
                        + "Position M " + modelOffset + " / " + position.getViewOffset() + " V; "
                        + "line " + modelLine.getNumber() + " / " + viewLine.getNumber() + " view line; "
                        + "horizontal offset " + visualOffset);
    }
}