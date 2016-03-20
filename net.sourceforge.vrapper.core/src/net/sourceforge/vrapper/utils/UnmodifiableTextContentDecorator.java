package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.Platform;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.LocalConfiguration;
import net.sourceforge.vrapper.vim.ConfigurationListener;
import net.sourceforge.vrapper.vim.Options;

/**
 * Makes a {@link TextContent} ignore changes depending on the current value of the
 * {@link Options#MODIFIABLE} setting.
 */
public class UnmodifiableTextContentDecorator implements TextContent {
    
    private TextContent textContent;
    private boolean modifiable = true;
    private UserInterfaceService uiService;
    private FileService fileService;

    public UnmodifiableTextContentDecorator(TextContent target, LocalConfiguration configuration,
            Platform platform) {
        this.textContent = target;
        this.uiService = platform.getUserInterfaceService();
        fileService = platform.getFileService();
        configuration.addListener(new ConfigurationListener() {
            @Override
            public <T> void optionChanged(Option<T> option, T oldValue, T newValue) {
                if (option.equals(Options.MODIFIABLE)) {
                    modifiable = Boolean.TRUE.equals(newValue); // Avoids casting a generic type.
                }
            }
        });
    }

    @Override
    public LineInformation getLineInformation(int line) {
        return textContent.getLineInformation(line);
    }

    @Override
    public LineInformation getLineInformationOfOffset(int offset) {
        return textContent.getLineInformationOfOffset(offset);
    }

    @Override
    public int getNumberOfLines() {
        return textContent.getNumberOfLines();
    }

    @Override
    public int getNumberOfLines(TextRange range) {
        return textContent.getNumberOfLines(range);
    }

    @Override
    public void replace(int index, int length, String s) {
        if (allowChanges()) {
            textContent.replace(index, length, s);
        }
    }

    @Override
    public void smartInsert(int index, String s) {
        if (allowChanges()) {
            textContent.smartInsert(index, s);
        }
    }

    @Override
    public void smartInsert(String s) {
        if (allowChanges()) {
            textContent.smartInsert(s);
        }
    }

    @Override
    public String getText() {
        return textContent.getText();
    }

    @Override
    public String getText(int index, int length) {
        return textContent.getText(index, length);
    }

    @Override
    public String getText(TextRange range) {
        return textContent.getText(range);
    }

    @Override
    public int getTextLength() {
        return textContent.getTextLength();
    }

    @Override
    public Space getSpace() {
        return textContent.getSpace();
    }

    protected boolean allowChanges() {
        if (modifiable && fileService.isEditable() && fileService.checkModifiable()) {
            return true;
            
        // Note: the result from fileService.isEditable() should not be cached. fs.checkModifiable()
        // can influence the return value of this function.
        } else if ( ! fileService.isEditable()) {
            uiService.setErrorMessage("Cannot modify contents, file is not editable!");
        } else {
            uiService.setErrorMessage("Cannot modify contents, 'modifiable' is off!");
        }
        return false;
    }
    
    public String toString() {
        return "(UTD) {" + textContent.toString() + "}";
    }
}
