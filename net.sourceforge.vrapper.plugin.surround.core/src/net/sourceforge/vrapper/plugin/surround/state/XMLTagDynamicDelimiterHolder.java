package net.sourceforge.vrapper.plugin.surround.state;

import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.DelimitedText;

public class XMLTagDynamicDelimiterHolder extends AbstractDynamicDelimiterHolder {

    public XMLTagDynamicDelimiterHolder() {
        super("tag", "<");
    }

    @Override
    public DelimiterHolder update(EditorAdaptor vim, int count, DelimitedText toWrap,
            String newDelimiterInput) throws CommandExecutionException {
        TextRange leftDelimiterRange = toWrap.leftDelimiter(vim, count);
        TextRange rightDelimiterRange = toWrap.rightDelimiter(vim, count);
        
        String leftDelimiter = vim.getModelContent().getText(leftDelimiterRange);
        String rightDelimiter = vim.getModelContent().getText(rightDelimiterRange);
        
        String[] inputs = newDelimiterInput.split(" ");
        
        String tagName = inputs[0];
        
        String startTag;
        
        String endTag = "</" + tagName.substring(1);
        if (! endTag.endsWith(">")) {
            endTag = endTag + ">";
        }
        
        // Keep the previous tag's attributes if it actually was a tag and when the input doesn't end with ">"
        // NOTE: Surround.vim always throws away previous tag's attributes. This feature is actually an extension.
        if (leftDelimiter.startsWith("<") && leftDelimiter.endsWith(">")
                && rightDelimiter.startsWith("</") && rightDelimiter.endsWith(">")
                && ! newDelimiterInput.endsWith(">")) {
            startTag = newDelimiterInput;
            
            // Check if old tag had any attributes.
            int firstSpace = leftDelimiter.indexOf(' ');
            if (firstSpace > -1) {
                startTag = startTag + leftDelimiter.substring(firstSpace);
            } else {
                startTag = startTag + '>';
            }
        } else {
            startTag = newDelimiterInput;
            if ( ! startTag.endsWith(">")) {
                startTag = startTag + ">";
            }
        }
        return new SimpleDelimiterHolder(startTag, endTag);
    }
}
