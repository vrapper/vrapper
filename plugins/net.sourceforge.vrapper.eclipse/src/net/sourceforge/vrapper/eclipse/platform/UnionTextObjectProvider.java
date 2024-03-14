package net.sourceforge.vrapper.eclipse.platform;

import java.util.List;

import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificTextObjectProvider;
import net.sourceforge.vrapper.vim.TextObjectProvider;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class UnionTextObjectProvider implements
        PlatformSpecificTextObjectProvider {
    
    private String name;
    
    private State<DelimitedText> delimitedTexts = EmptyState.getInstance();
    private State<TextObject> textObjects = EmptyState.getInstance();

    public UnionTextObjectProvider(String name, List<PlatformSpecificTextObjectProvider> providers){
        this.name = name;
        for (TextObjectProvider provider: providers) {
            State<DelimitedText> providerDelimitedTexts = provider.delimitedTexts();
            if (providerDelimitedTexts == null) {
                throw new NullPointerException(provider.getClass().getCanonicalName()
                        + "#delimitedTexts() returned null!");
            } else {
                delimitedTexts = delimitedTexts.union(providerDelimitedTexts);
            }
            State<TextObject> providerTextObjects = provider.textObjects();
            if (providerTextObjects == null) {
                throw new NullPointerException(provider.getClass().getCanonicalName()
                        + "#textObjects() returned null!");
            } else {
                textObjects = textObjects.union(providerTextObjects);
            }
        }
    }

    @Override
    public State<DelimitedText> delimitedTexts() {
        return delimitedTexts;
    }

    @Override
    public State<TextObject> textObjects() {
        return textObjects;
    }

    @Override
    public String getName() {
        return name;
    }

}
