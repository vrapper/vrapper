package net.sourceforge.vrapper.eclipse.commands;

import java.util.Locale;

import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.vim.AbstractPlugState;
import net.sourceforge.vrapper.vim.commands.Command;

public class EclipsePlugState extends AbstractPlugState<Command> {

    public static final String COMMANDPREFIX = "(eclipse:";
    public static final State<Command> INSTANCE = new EclipsePlugState(false);
    public static final State<Command> VISUAL_INSTANCE = new EclipsePlugState(true);

    private boolean fromVisual;

    public EclipsePlugState(boolean fromVisual) {
        this.fromVisual = fromVisual;
    }

    @Override
    public Transition<Command> press(String id) {
        if (id.toLowerCase(Locale.ENGLISH).startsWith(COMMANDPREFIX)) {
            // Clip off prefix and last ')'
            String commandId = id.substring(COMMANDPREFIX.length(), id.length() -1);
            Command result;
            return new SimpleTransition<Command>(
                    new EclipseCommand(commandId).fromVisualMode(fromVisual));
        } else {
            return null;
        }
    }

    @Override
    public State<Command> union(State<Command> other) {
        return new UnionState<Command>(this, other);
    }
}
