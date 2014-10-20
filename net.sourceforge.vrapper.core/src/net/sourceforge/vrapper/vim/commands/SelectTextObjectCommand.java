package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.modes.AbstractVisualMode;
import net.sourceforge.vrapper.vim.modes.BlockwiseVisualMode;
import net.sourceforge.vrapper.vim.modes.LinewiseVisualMode;
import net.sourceforge.vrapper.vim.modes.VisualMode;

public class SelectTextObjectCommand extends CountAwareCommand {

    private final TextObject textObject;

    public SelectTextObjectCommand(final TextObject textObject) {
        this.textObject = textObject;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, int count) throws CommandExecutionException {
        Selection oldSelection = editorAdaptor.getSelection();
        TextRange region = textObject.getRegion(editorAdaptor, count);
        //if selection was calculated to be the same as before
        //it probably means we're chaining i{i{, keep increasing count
        //until we expand into next the region
        int orgCount = count;
        while(textObject instanceof DelimitedTextObject
                && oldSelection.getLeftBound().getModelOffset() <= region.getLeftBound().getModelOffset()
                && oldSelection.getRightBound().getModelOffset() >= region.getRightBound().getModelOffset()) {
            // TextObject might not use count, abort after a while to prevent an endless loop.
            // FIXME: See if we can do this chaining functionality without looping over count. 
            if (count > orgCount + 10) {
                break;
            }
            count++;
            //this will throw a CommandExecutionException and break out of the
            //while loop if no delimiter can be found larger than the current
            //selection
            region = textObject.getRegion(editorAdaptor, count);
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
            String selectionOption = editorAdaptor.getConfiguration().get(Options.SELECTION);
            boolean isInclusive = Selection.INCLUSIVE.equals(selectionOption);
            selection = new SimpleSelection(editorAdaptor.getCursorService(), isInclusive, region);
            newMode = VisualMode.NAME;
            break;
        default: throw new CommandExecutionException("WTF");
        }
        //Makes sure to update the sticky column.
        editorAdaptor.setPosition(selection.getTo(), StickyColumnPolicy.ON_CHANGE);
        editorAdaptor.setSelection(selection);
        editorAdaptor.changeMode(newMode, AbstractVisualMode.FIX_SELECTION_HINT);
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
