package de.jroene.vrapper.vim.register;

/**
 * Holds information for delete, yank, put operations.
 *
 * @author Matthias Radig
 */
public class RegisterContent {

    private final boolean lineWise;
    private final String payload;
    public RegisterContent(boolean lineWise, String payload) {
        super();
        this.lineWise = lineWise;
        this.payload = payload;
    }
    public boolean isLineWise() {
        return lineWise;
    }
    public String getPayload() {
        return payload;
    }

}
