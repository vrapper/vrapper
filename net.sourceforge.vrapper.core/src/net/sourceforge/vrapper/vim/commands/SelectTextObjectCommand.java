package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.BlockwiseVisualMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class SelectTextObjectCommand extends CountAwareCommand {

    private final TextObject textObject;
    //each selection is a new instance, have to make this static
    //to persist between invocations
    private static int chainingCount = 0;

    public SelectTextObjectCommand(final TextObject textObject) {
        this.textObject = textObject;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Selection oldSelection = editorAdaptor.getSelection();
        TextRange region = textObject.getRegion(editorAdaptor, count);
        //if selection was calculated to be the same as before
        //it probably means we're chaining i{i{, expand to next region
        if(textObject instanceof DelimitedTextObject
                && oldSelection.getLeftBound().getModelOffset() == region.getLeftBound().getModelOffset()
                && oldSelection.getRightBound().getModelOffset() == region.getRightBound().getModelOffset()) {

            try {
                //get region again and see if selection expands
                //(this should work if the cursor is not on a delimiter character)
                region = textObject.getRegion(editorAdaptor, chainingCount);
            }
            catch(CommandExecutionException e) {
                chainingCount = 0;
                region = textObject.getRegion(editorAdaptor, chainingCount);
            }

            if(oldSelection.getLeftBound().getModelOffset() == region.getLeftBound().getModelOffset()
                    && oldSelection.getRightBound().getModelOffset() == region.getRightBound().getModelOffset()) {
                //selection didn't change, cursor is probably on the delimiter
                //increase the count and get region again
                chainingCount += chainingCount == 0 ? 2 : 1;
                region = textObject.getRegion(editorAdaptor, chainingCount);
            }
            else {
                //cursor is not on a delimiter anymore
                chainingCount = 0;
            }
        }
        else {
            //new selection, reset chaining
            chainingCount = 0;
        }

        Selection selection;
        String newMode;
        switch (textObject.getContentType(editorAdaptor.getConfiguration())) {
        case TEXT_RECTANGLE: 
            selection = new BlockWiseSelection(editorAdaptor, region.getStart(), region.getEnd());
            newMode = BlockwiseVisualMode.NAME;
            break;
        case LINES:
            /**
             * TODO: ugly casting. The problem is, that if textObject already
             * returns a LineWiseSelection, then by creating new instance of
             * LineWiseSelection from the old one we extend the selected range
             * by one line. This is due implementation of LineWiseSelection,
             * whose getEnd() points to the line AFTER the actual selection. It
             * might be solved by refactoring TextRange with introduction of
             * linewise range (by line numbers)
             */
            if (region instanceof LineWiseSelection) {
                selection = (LineWiseSelection) region;
            }
            else {
                selection = new LineWiseSelection(editorAdaptor, region.getStart(), region.getEnd());
            }
            newMode = LinewiseVisualMode.NAME;
            break;
        case TEXT:
            selection = new SimpleSelection(region);
            newMode = VisualMode.NAME;
            break;
        default: throw new CommandExecutionException("WTF");
        }
        editorAdaptor.setSelection(selection);
        editorAdaptor.changeMode(newMode, AbstractVisualMode.FIX_SELECTION_HINT);
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
