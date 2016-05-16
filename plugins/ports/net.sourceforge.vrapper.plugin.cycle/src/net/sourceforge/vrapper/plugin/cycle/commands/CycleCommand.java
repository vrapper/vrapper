package net.sourceforge.vrapper.plugin.cycle.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;
import net.sourceforge.vrapper.vim.commands.IncrementDecrementCommand;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public class CycleCommand extends CountAwareCommand {
    
    public static final Command NEXT = new CycleCommand(true);
    public static final Command PREV = new CycleCommand(false);
    
    private static List<List<String>> groups;
    private boolean forward;
    
    private CycleCommand(boolean forward) {
        this.forward = forward;
        groups = new ArrayList<List<String>>();
        addGroup(groups, "true", "false");
        addGroup(groups, "yes", "no");
        addGroup(groups, "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday");
        addGroup(groups, "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
    }
    
    private static void addGroup(List<List<String>> groups, String... words) {
        List<String> newList = new ArrayList<String>();
        for(String word : words) {
            newList.add(word);
        }
        groups.add(newList);
    }

    public static Evaluator addCycleGroupEvaluator() {
        return new Evaluator() {
            public Object evaluate(EditorAdaptor vim, Queue<String> command) throws CommandExecutionException {
                String[] args = command.toArray(new String[0]);
                CycleCommand.addGroup(groups, args);
            	return null;
            }
        };
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        String word = VimUtils.getWordUnderCursor(editorAdaptor, false);
        if(count == NO_COUNT_GIVEN) {
            count = 1;
        }
        int modifier = forward ? count : -count;

        for(List<String> list : groups) {
            if(list.contains(word)) {
                int start = getWordIndex(word, editorAdaptor);
                int index = list.indexOf(word);
                int next = (index + list.size() + modifier) % list.size(); 
                editorAdaptor.getModelContent().replace(start, word.length(), list.get(next));
                //stop looping and don't run default IncrementDecrementCommand
                return;
            }
        }
        
        //no words matched, perform normal increment/decrement
        if(forward) {
            IncrementDecrementCommand.INCREMENT.execute(editorAdaptor, count);
        }
        else {
            IncrementDecrementCommand.DECREMENT.execute(editorAdaptor, count);
        }
            
    }
    
    //VimUtils.getWordUnderCursor calculated this word's start and end index
    //and then returned the word.  I need to find that start index.
    private int getWordIndex(String word, EditorAdaptor editorAdaptor) {
        CursorService cursor = editorAdaptor.getCursorService();
        TextContent model = editorAdaptor.getModelContent();

        int pos = cursor.getPosition().getModelOffset();
        LineInformation line = model.getLineInformationOfOffset(pos);
        //look around cursor +/- word length
        int start = Math.max(line.getBeginOffset(), pos - word.length());
        int end = Math.min(line.getEndOffset(), pos + word.length());

        String range = model.getText(start, end - start);
        return range.indexOf(word) + start;
    }

    @Override
    public CountAwareCommand repetition() {
        return this;
    }

}
