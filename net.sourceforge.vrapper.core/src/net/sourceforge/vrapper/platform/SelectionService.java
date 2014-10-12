package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.vim.commands.Selection;

public interface SelectionService {
    /**
     * Sets the selection.
     * On some implementations (including Eclipse) this also moves cursor to selection's end.
     * @param selection - new selection; null to deselect all
     */
    void setSelection(Selection selection);

    /**
     * @return current selection, returns zero-length selection if no selection is show in the
     * current Eclipse editor.
     */
    Selection getSelection();

}
