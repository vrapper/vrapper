package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;

/*
 *  *:ret* *:retab* *:retab!*
 *  :[range]ret[ab][!] [new_tabstop]
 *      Replace all sequences of white-space containing a
 *      <Tab> with new strings of white-space using the new
 *      tabstop value given.  If you do not specify a new
 *      tabstop size or it is zero, Vim uses the current value
 *      of 'tabstop'.
 *      The current value of 'tabstop' is always used to
 *      compute the width of existing tabs.
 *      With !, Vim also replaces strings of only normal
 *      spaces with tabs where appropriate.
 *      With 'expandtab' on, Vim replaces all tabs with the
 *      appropriate number of spaces.
 *      This command sets 'tabstop' to the new value given,
 *      and if performed on the whole file, which is default,
 *      should not make any visible change.
 *      Careful: This command modifies any <Tab> characters
 *      inside of strings in a C program.  Use "\t" to avoid
 *      this (that's a good habit anyway).
 *      `:retab!` may also change a sequence of spaces by
 *       <Tab> characters, which can mess up a printf().
 *       {not in Vi}
 */
/**
 * RetabOperation
 * @author bdetweiler
 * 
 * :retab changes the tabstop value in the current configuration. It also will
 * apply those changes to the current file. This works in a two different ways.
 * 
 * If expandtab (et) is turned on, it will replace any tab characters with the
 * equivalent number of spaces in the TAB_STOP option. This is useful for getting
 * rid of unwanted tabs in a text file.
 * 
 * If expandtab (et) is NOT turned on, the TAB_STOP value is applied to the current file. 
 *
 * If a number is passed to :retab, the TAB_STOP value for the current session is
 * changed to that value and applied to the current file. Tabs are replaced with
 * the new value, and spaces are applied where a tab does not fit. 
 * 
 * TODO: Eclipse uses its own tabstop value under 
 *       Window->Preferences->General->Editors->Text Editors->"Displayed tab width"
 *       And expandtab is also an Eclipse setting under
 *       Window->Preferences->General->Editors->Text Editors->"Insert spaces for tabs"
 *       
 */
public class RetabOperation extends SimpleTextOperation {

    /** ! - replace strings of normal spaces */
    private static final String REPLACE_NORMAL_SPACE = "!";
   
    private static final String SPACE = " ";
    
    // Possible configurations for sort
    private boolean replaceNormalSpace = false;

    private Integer newTab = 0;

    public RetabOperation(String commandStr) {
        super();
        
        // If no command was given, we're done with setup
        if(commandStr == null)
            return;
      
        commandStr = commandStr.replace("retab", "");
        commandStr = commandStr.replace("reta", "");
        commandStr = commandStr.replace("ret", "");
      
        // Replaces equivalent number of spaces with tab if expandtab is not on
        if(commandStr.contains(REPLACE_NORMAL_SPACE))
            replaceNormalSpace = true;
       
        // New tab character
        if(commandStr.length() > 0) {
            try {
                newTab = Integer.parseInt(commandStr.trim());
            } catch(Exception e) {
                // Invalid value, do nothing
                return; 
            }
        }
    }
    
	@Override
	public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) throws CommandExecutionException {
        try {
            
        	TextContent content = editorAdaptor.getModelContent();
        	LineInformation startLine;
        	LineInformation endLine;
        	int length;
        	
        	if(region == null) {
        		startLine = content.getLineInformation(0);
        		endLine = content.getLineInformation(content.getNumberOfLines() - 1);
        		length = endLine.getEndOffset() - startLine.getBeginOffset();
        	}
        	else {
        		startLine = content.getLineInformationOfOffset(region.getLeftBound().getModelOffset());
        		endLine = content.getLineInformationOfOffset(region.getRightBound().getModelOffset() - 1);
        		length = region.getModelLength();
        	}
        	
            doIt(editorAdaptor, startLine, endLine, length);
        	
        } catch (Exception e) {
            throw new CommandExecutionException("retab failed: " + e.getMessage());
        }
    }

    /**
     * This is where the action happens.
     * 
     * @param editorAdaptor
     * @throws Exception
     */
    public void doIt(EditorAdaptor editorAdaptor, 
                     LineInformation startLine, 
                     LineInformation endLine, 
                     int totalLengthOfRange) throws Exception {
        
        int tabStop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
        boolean expandTab = editorAdaptor.getConfiguration().get(Options.EXPAND_TAB);
        
        if(tabStop == 0)
            tabStop = 8;
        
        Integer currenTabStop = editorAdaptor.getConfiguration().get(Options.TAB_STOP);
      
        // Can't have 0 for TAB_STOP. Leave it the same.
        if(newTab == 0)
            newTab = currenTabStop;
       
        // Tabs are converted to spaces in place, then the TAB_STOP value is changed
        // so future tabs will have the new value of spaces
        String replacementSpaces = "";
        for(int i = 0; i < currenTabStop; ++i)
            replacementSpaces += SPACE;
       
        editorAdaptor.getConfiguration().set(Options.TAB_STOP, newTab);
        
        // Throw the whole editor into an array separated by newlines
        String newline = editorAdaptor.getConfiguration().getNewLine();
        TextContent content = editorAdaptor.getModelContent();
        int totalLinesInEditor = content.getNumberOfLines();
        List<String> editorContentList = new ArrayList<String>();
        LineInformation line = null;
    
        /* 
         * Step 1: Go through and retab each line and put it into an
         *         array list so we can replace the contents of the editor
         */
        for(int i = startLine.getNumber(); i <= endLine.getNumber(); ++i) {
            line = content.getLineInformation(i);
            String lineStr = content.getText(line.getBeginOffset(), line.getLength());
         
            if(expandTab) { 
                lineStr = lineStr.replaceAll("\\t", replacementSpaces);
            } else if(replaceNormalSpace) {
                // If TAB_STOP number of spaces are encountered and expandTab is off, 
                // replace each occurrence of those spaces with a tab
                lineStr = lineStr.replaceAll(replacementSpaces, "\t");
            }
            
            editorContentList.add(lineStr);
        }

        /*
         * Step 2: Append newlines to everything but the very last line of the editor
         */
        StringBuilder replacementText = new StringBuilder();
        int count = startLine.getNumber();
        for (String editorLine : editorContentList) {
            if(count != totalLinesInEditor - 1)
                editorLine += newline;
            
            ++count;
            replacementText.append(editorLine);
        }
        
        /*
         * Step 3: Replace the contents of the editor with the freshly retabbed text
         *         This applies to a range, or the whole editor
         */
        editorAdaptor.getModelContent().replace(startLine.getBeginOffset(),
                                                totalLengthOfRange,
                                                replacementText.toString());
        //put cursor at beginning of sorted text
        editorAdaptor.setPosition(
        		editorAdaptor.getCursorService().newPositionForModelOffset(startLine.getBeginOffset()),
        		true);
    }

	public TextOperation repetition() {
		return null;
	}
}