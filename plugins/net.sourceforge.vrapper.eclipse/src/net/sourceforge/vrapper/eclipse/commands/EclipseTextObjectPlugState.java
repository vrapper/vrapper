package net.sourceforge.vrapper.eclipse.commands;

import java.util.Locale;

import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.vim.AbstractPlugState;
import net.sourceforge.vrapper.vim.commands.TextObject;

public class EclipseTextObjectPlugState extends AbstractPlugState<TextObject> {

    public static final String TEXTOBJPREFIX = "(eclipse-textobj:";
    public static final State<TextObject> INSTANCE = new EclipseTextObjectPlugState();

    @Override
    public Transition<TextObject> press(String id) {
        if (id.toLowerCase(Locale.ENGLISH).startsWith(TEXTOBJPREFIX)) {
            // Clip off prefix and last ')'
            String commandId = id.substring(TEXTOBJPREFIX.length(), id.length() -1);
            return new SimpleTransition<TextObject>(new EclipseCommandTextObject(commandId));
        } else {
            return null;
        }
    }

    @Override
    public State<TextObject> union(State<TextObject> other) {
        return new UnionState<TextObject>(this, other);
    }
}
