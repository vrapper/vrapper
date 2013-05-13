package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

/**
 * Marker class which signals that a {@link DelimiterHolder} needs to be updated before use.
 * @author Bert Jacobs
 */
public abstract class AbstractDynamicDelimiterHolder implements DelimiterHolder {
    
    private String displayName;
    private String template;
    
    public AbstractDynamicDelimiterHolder(String displayName, String template) {
        this.displayName = displayName;
        this.template = template;
    }
    
    public String getDelimiterDisplayName() {
        return displayName;
    }
    
    public String getTemplate() {
        return template;
    }
    
    @Override
    public String getLeft() {
        throw new IllegalStateException("Delimiters weren't updated!");
    }

    @Override
    public String getRight() {
        throw new IllegalStateException("Delimiters weren't updated!");
    }
    
    /**
     * Create a new DelimiterHolder instance based on the text whose delimiters need to be replaced.
     * @param vim {@link EditorAdaptor} for the current editor.
     * @param toWrap {@link DelimitedText} instance of the text whose delimiters will have to be
     *  replaced.
     * @param newDelimiterInput TODO
     * @return a new {@link DelimiterHolder} instance containing the updated delimiters. 
     */
    public abstract DelimiterHolder update(EditorAdaptor vim, DelimitedText toWrap, String newDelimiterInput) throws CommandExecutionException;
}
