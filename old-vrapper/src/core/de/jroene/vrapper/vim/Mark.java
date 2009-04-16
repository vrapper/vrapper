package de.jroene.vrapper.vim;

/**
 * A marked position in the text.
 *
 * @author Matthias Radig
 */
public class Mark {

    private final LineInformation line;
    private final int position;

    public Mark(LineInformation line, int position) {
        super();
        this.line = line;
        this.position = position;
    }

    public LineInformation getLine() {
        return line;
    }

    public int getPosition() {
        return position;
    }
}
