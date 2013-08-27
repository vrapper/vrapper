package net.sourceforge.vrapper.vim.modes;

import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.ctrlKey;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * See :help ins-completion 
 * For now, we only support the ^L command within InsertExpandMode.
 * Anything else will return to InsertMode.
 */
public class InsertExpandMode extends InsertMode {

    public static final String NAME = "insert expand mode";
    public static final String DISPLAY_NAME = "^X mode (^L)";
    private static final Pattern indentPattern = Pattern.compile("(^\\s*)\\S*");
    private String lastIndent = "";
    private String lastPrefix = "";
    private String lastSuffix = "";
    private List<String> lastMatches = new ArrayList<String>();
    private int lastIndex = 0;
    private int lastLineNo = 0;

    public InsertExpandMode(EditorAdaptor editorAdaptor) {
        super(editorAdaptor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @Override
    public boolean handleKey(KeyStroke stroke) {
        if(stroke.equals(ctrlKey('l'))) {
            doLineCompletion();
            return true;
        }
        else {
            //return to insert mode if any other key pressed
            editorAdaptor.changeModeSafely(InsertMode.NAME, InsertMode.RESUME_ON_MODE_ENTER);
            return super.handleKey(stroke);
        }
    }
    
    // ^X^L
    private void doLineCompletion() {
        TextContent model = editorAdaptor.getModelContent();
        Position cursorPos = editorAdaptor.getCursorService().getPosition();
        int cursorOffset = cursorPos.getModelOffset();
        LineInformation lineInfo = model.getLineInformationOfOffset(cursorOffset);
        String line = model.getText(lineInfo.getBeginOffset(), lineInfo.getLength());
        if(lastLineNo != lineInfo.getNumber() || ! line.startsWith(lastIndent + lastPrefix)) {
            rebuildLineMatches(cursorOffset - lineInfo.getBeginOffset(), line, lineInfo.getNumber(), model);
        }

        if(lastMatches.size() == 1) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("Pattern not found");
        }
        else {
            lastIndex = (lastIndex + 1) % lastMatches.size();
            String newString = lastIndent + lastMatches.get(lastIndex) + lastSuffix;
            model.replace(lineInfo.getBeginOffset(), lineInfo.getLength(), newString);
            int newCursor = lineInfo.getBeginOffset() + lastIndent.length() + lastMatches.get(lastIndex).length();
            editorAdaptor.getCursorService().setPosition(cursorPos.setModelOffset(newCursor), StickyColumnPolicy.ON_CHANGE);
            if(lastIndex == 0) {
                editorAdaptor.getUserInterfaceService().setInfoMessage("Back at original");
            }
            else {
                editorAdaptor.getUserInterfaceService().setInfoMessage("match " + lastIndex + " of " + (lastMatches.size() -1));
            }
        }
    }
    
    /**
     * Go through the entire file and build a list of all lines
     * that start with the same prefix as this line (at the cursor position).
     * 
     * @param cursorPos - index into 'line' where cursor is
     * @param line - line of text in the file
     * @param lineNo - line number of 'line'
     * @param model - model content
     */
    private void rebuildLineMatches(int cursorPos, String line, int lineNo, TextContent model) {
    	Matcher matcher = indentPattern.matcher(line);
    	if(matcher.find()) {
    	    lastIndent = matcher.group(1);
    	}
    	else {
    	    lastIndent = "";
    	}

        lastPrefix = line.substring(lastIndent.length(), cursorPos);
        lastSuffix = line.substring(cursorPos);
        lastLineNo = lineNo;
        //will be incremented to 1 on first iteration
        lastIndex = 0;
        lastMatches.clear();
        //index 0 will be original state
        lastMatches.add(lastPrefix);

        //rebuild list of all lines that start with lastPrefix
        LineInformation matchLine;
        String matchText;
        for(int i=0; i < model.getNumberOfLines(); i++) {
            if(i == lineNo) {
                //skip self
                continue;
            }
            matchLine = model.getLineInformation(i);
            matchText = model.getText(matchLine.getBeginOffset(), matchLine.getLength());
            matchText = matchText.replaceFirst("^\\s+", "");
            if(matchText.startsWith(lastPrefix)) {
                lastMatches.add(matchText);
            }
        }
    }
}
