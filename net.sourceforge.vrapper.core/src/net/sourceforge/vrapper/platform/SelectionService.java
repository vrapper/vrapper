package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.commands.Selection;

public interface SelectionService {
    /**
     * Sets the selection.
     * On some implementations (including Eclipse) this also moves cursor to selection's start.
     * @param selection - new selection; null to deselect all
     */
    void setSelection(Selection selection);
    
    void setSelection(Selection selection, boolean leaveVisualMode);
    
    void setYankOperation(boolean yankOperation);

    /**
     * @return new selection; null if nothing is selected
     */
    Selection getSelection();

}
