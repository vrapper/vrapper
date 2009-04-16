package newpackage.glue;

import newpackage.position.TextRange;

public interface SelectionService {
    /**
     * Sets the selection.
     * On some implementations (including Eclipse) this also moves cursor to selection's start.
     * @param selection - new selection; null to deselect all
     */
    void setSelection(TextRange selection);

    /**
     * @return new selection; null if nothing is selected
     */
    TextRange getSelection();

    /**
     * Tells the platform whether to use line wise or character wise selection
     * @param lineWise
     */
    void setLineWiseSelection(boolean lineWise);

}
