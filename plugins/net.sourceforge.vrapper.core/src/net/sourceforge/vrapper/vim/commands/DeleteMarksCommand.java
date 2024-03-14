package net.sourceforge.vrapper.vim.commands;

import java.util.Queue;
import java.util.Set;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * :delmarks a     (delete mark a)
 * :delmarks a b c (delete marks a, b, and c)
 * :delmarks abc   (delete marks a, b, and c)
 * :delmarks a-e   (delete marks a, b, c, d, e)
 * :delmarks!      (delete all marks)
 */
public class DeleteMarksCommand extends CountIgnoringNonRepeatableCommand {
	
	private Queue<String> marks;
	
	public DeleteMarksCommand(Queue<String> marks) {
		this.marks = marks;
	}

	@Override
	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		CursorService cursor = editorAdaptor.getCursorService();

		//delete all marks
		if(marks.size() == 1 && "!".equals(marks.peek())) {
			Set<String> all = cursor.getAllMarks();
			for(String mark : all) {
				//don't delete global marks (A-Z), numbered marks (0-9), or ' mark
				if( ! Character.isUpperCase(mark.charAt(0)) &&
						! Character.isDigit(mark.charAt(0))	&&
						! mark.equals("'")) {
					cursor.deleteMark(mark);
				}
			}
			marks.poll(); //don't loop below
		}

		while(marks.size() > 0) { //loop over space-delimited values
			String mark = marks.poll();
			if(mark.contains("-") && mark.length() == 3) { //range (a-z)
				String[] bounds = mark.split("-");
				//chars can be treated as ints for ascii value
				char start = bounds[0].charAt(0);
				char end = bounds[1].charAt(0);
				for(char i=start; i <= end; i++) {
					cursor.deleteMark(""+i);
				}
			}
			else { //could be a single character (a) or multiple (abc), handle both cases
				for(int i=0; i < mark.length(); i++) {
					cursor.deleteMark(""+mark.charAt(i));
				}
			}
		}

	}

}
