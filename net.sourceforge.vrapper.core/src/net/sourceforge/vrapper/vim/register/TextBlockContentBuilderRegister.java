package net.sourceforge.vrapper.vim.register;

import net.sourceforge.vrapper.utils.ContentType;

/**
 * Register wrapper to accumulate text yanks made during block operation
 * execution.
 */
public class TextBlockContentBuilderRegister implements Register {

    private final Register delegate;
    private final TextBlockRegisterContent textBlock;

    public TextBlockContentBuilderRegister(Register delegate, int visualWidth) {
        this.delegate = delegate;
        this.textBlock = new TextBlockRegisterContent(visualWidth);
    }

    public RegisterContent getContent() {
        return delegate.getContent();
    }

    public void setContent(RegisterContent content) {
        assert content.getPayloadType() == ContentType.TEXT;
        textBlock.appendLine(content.getText());
        delegate.setContent(textBlock);
    }

}
