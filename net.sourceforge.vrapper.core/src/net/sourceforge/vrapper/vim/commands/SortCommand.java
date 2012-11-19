package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.platform.SimpleConfiguration;

/**
 * Implements Collections.sort() on all the lines in the current file.
 * @author Brian Detweiler
 *
 */
public class SortCommand extends CountIgnoringNonRepeatableCommand {
	
    public static final SortCommand INSTANCE = new SortCommand();

    private SortCommand() {
        super();
    }
    
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        int line = editorAdaptor.getViewContent().getLineInformationOfOffset(
                editorAdaptor.getPosition().getViewOffset()).getNumber();
        doIt(editorAdaptor, line);
	}

    public void doIt(EditorAdaptor editorAdaptor, int line) {
    
    	SimpleConfiguration config = new SimpleConfiguration();
    	String nl = config.getNewLine();
    	
    	int length = editorAdaptor.getModelContent().getTextLength();
    	String editorContent = editorAdaptor.getModelContent().getText(0, length);
   
    	char[] editorContentArr = editorContent.toCharArray();
  
    	List<String> editorContentList = new ArrayList<String>();
    	String s = "";
    	for(char c : editorContentArr) {
    		s += c;
    		if(nl.equalsIgnoreCase(c + "")) {
    			editorContentList.add(s);
    			s = "";
    		}
    	}
    	
    	// TODO: Still running into some problems with newlines, but mostly working
    	Collections.sort(editorContentList);
  
    	int size = editorContentList.size();
    	int count = 0;
    	String replacementText = "";
    	for(String editorLine : editorContentList) {
    		++count;
    		if(count == size && editorLine.endsWith(nl))
    			editorLine = editorLine.substring(0, editorLine.length() - 1);
    		replacementText += editorLine;
    	}
    	
		System.out.println(replacementText);
    
		editorAdaptor.getModelContent().replace(0, length, replacementText);
    }
}