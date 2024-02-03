package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * :marks [args]
 * List the contents of all marks,
 * or list contents of [args] marks.
 */
public class ListMarksCommand extends AbstractMessagesCommand {

    private String toDisplay;

    public ListMarksCommand() {
        this("");
    }
    
    public ListMarksCommand(String toDisplay) {
        this.toDisplay = toDisplay;
    }

    @Override
    protected String getMessages(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        StringBuilder sb = new StringBuilder();

        CursorService cursor = editorAdaptor.getCursorService();
        FileService files = editorAdaptor.getFileService();
        List<String> names;
        if(toDisplay.length() > 0) {
            toDisplay = toDisplay.replaceAll(" ", "");
            // Split into chars.
            names = Arrays.asList(toDisplay.split("(?!^)"));
        }
        else {
            Set<String> allNames = cursor.getAllMarks();
            names = new ArrayList<String>(allNames);
        }
        Collections.sort(names);

        Position mark;
        LineInformation line;
        String filename;
        String lineNo;
        String colNo;
        sb.append("mark line  col file/text\n");
        for(String name : names) {
            mark = cursor.getMark(name);

            if (name.startsWith(CursorService.INTERNAL_MARK_PREFIX)) {
                continue;
            }
            else if (cursor.isGlobalMark(name)) {
                filename = files.getFileNameOfGlobalMark(name);
                if(filename.equals(files.getCurrentFileName())) {
                    //The mark is global, but it's for this file.
                    //We can display the info.
                    line = editorAdaptor.getModelContent().getLineInformationOfOffset(mark.getModelOffset());
                }
                else {
                    //To get the LineInformation of a different file
                    //I'd need that other file's EditorAdaptor instance.
                    //I'm going to be lazy and just give up here.
                    line = null;
                }
            }
            else if(mark == null) {
                //getMark() returns null for global marks
                //but if this mark isn't global and still null
                //it really is an unknown mark
                filename = "?";
                line = null;
            }
            else {
                //local mark
                filename = files.getCurrentFileName();
                line = editorAdaptor.getModelContent().getLineInformationOfOffset(mark.getModelOffset());
            }
            
            if(line == null) {
                lineNo = "?";
                colNo = "?";
            }
            else {
                lineNo = line.getNumber() + 1 + ""; //lines are 1-based
                colNo = mark.getModelOffset() - line.getBeginOffset() + ""; //columns are 0-based
            }

            sb.append(
               String.format(" %1s %6s %4s %s\n", name, lineNo, colNo, filename)
            );
        }

        return sb.toString();
    }

}
