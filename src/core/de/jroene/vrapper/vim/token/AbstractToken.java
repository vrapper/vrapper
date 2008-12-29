package de.jroene.vrapper.vim.token;

/**
 * Implements the clone method like it is specified in interface {@link Token},
 * without {@link CloneNotSupportedException} as it will never be thrown, and
 * the result is already cast to Token.
 *
 * @author Matthias Radig
 */
public abstract class AbstractToken implements Token {

    @Override
    public final Token clone() {
        try {
            return (Token) super.clone();
        } catch (CloneNotSupportedException e) {
            // does not happen
            e.printStackTrace();
            return null;
        }
    }

}
