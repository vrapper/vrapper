package net.sourceforge.vrapper.utils;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.LocalConfiguration;
import net.sourceforge.vrapper.vim.LocalConfigurationListener;
import net.sourceforge.vrapper.vim.Options;

/**
 * Makes a {@link TextContent} ignore changes depending on the current value of the
 * {@link Options#MODIFIABLE} setting.
 */
public class UnmodifiableTextContentDecorator implements TextContent {
    
    private TextContent textContent;
    private boolean modifiable = true;
    private UserInterfaceService uiService;

    public UnmodifiableTextContentDecorator(TextContent target, LocalConfiguration configuration,
            UserInterfaceService userInterfaceService) {
        this.textContent = target;
        this.uiService = userInterfaceService;
        configuration.addListener(new LocalConfigurationListener() {
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
    public void replace(int index, int length, String s) {
        if (modifiable) {
            textContent.replace(index, length, s);
        } else {
            uiService.setErrorMessage("Cannot modify contents, 'modifiable' is off!");
        }
    }

    @Override
    public void smartInsert(int index, String s) {
        if (modifiable) {
            textContent.smartInsert(index, s);
        } else {
            uiService.setErrorMessage("Cannot modify contents, 'modifiable' is off!");
        }
    }

    @Override
    public void smartInsert(String s) {
        if (modifiable) {
            textContent.smartInsert(s);
        }
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

}
