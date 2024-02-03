package net.sourceforge.vrapper.plugin.surround.state;


/**
 * Holds a pair of delimiters which need to be added or used to replace a previous pair of
 *  delimiters in the editor.
 * 
 * @author Bert Jacobs
 */
public interface DelimiterHolder {
    public String getLeft();
    public String getRight();
}
