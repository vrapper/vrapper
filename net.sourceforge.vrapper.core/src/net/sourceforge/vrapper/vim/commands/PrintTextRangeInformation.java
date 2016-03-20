package net.sourceforge.vrapper.vim.commands;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class PrintTextRangeInformation implements Command {

    public static final Command INSTANCE = new PrintTextRangeInformation();

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
        UserInterfaceService service = editorAdaptor.getUserInterfaceService();
        TextContent modelContent = editorAdaptor.getModelContent();

        Selection range = editorAdaptor.getSelection();
        TextRange region = range.getRegion(editorAdaptor, NO_COUNT_GIVEN);
        Pattern pattern = Pattern.compile("[\\S]+");

        String rangeText = modelContent.getText(region);
        String contentText = modelContent.getText();

        int selectedWords = 0;
        Matcher matcher = pattern.matcher(rangeText);
        while (matcher.find()) selectedWords++;

        int totalWords = 0;
        matcher = pattern.matcher(contentText);
        while (matcher.find()) totalWords++;

        int selectedLines = modelContent.getNumberOfLines(range);
        int totalLines = modelContent.getNumberOfLines();
        
        int selectedBytes = 0;
        int totalBytes = 0;
        int selectedChars = 0;
        int totalChars = 0;
        try {
            selectedBytes = rangeText.getBytes("UTF-8").length;
            selectedChars = rangeText.toCharArray().length;
            totalBytes = contentText.getBytes("UTF-8").length;
            totalChars = contentText.toCharArray().length;
        } catch (UnsupportedEncodingException e) {
            VrapperLog.info(e.getMessage());
        }

        service.setInfoSet(true);
        service.setLastCommandResultValue("Selected " 
                        + selectedLines + " of " + totalLines + " Lines; "
                        + selectedWords + " of " + totalWords + " Words; "
                        + (totalChars != totalBytes ? (selectedChars + " of " + totalChars + " Chars; ") : "")
                        + selectedBytes + " of " + totalBytes + " Bytes");
    }
}