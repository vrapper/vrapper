package net.sourceforge.vrapper.eclipse.commands;

import java.util.Locale;

import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.vim.AbstractPlugState;
import net.sourceforge.vrapper.vim.commands.BorderPolicy;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

public class EclipseMotionPlugState extends AbstractPlugState<Motion> {

    public static final String MOTIONPREFIX = "(eclipse-motion:";
    public static final State<Motion> INSTANCE = new EclipseMotionPlugState();

    @Override
    public Transition<Motion> press(String id) {
        if (id.toLowerCase(Locale.ENGLISH).startsWith(MOTIONPREFIX)) {
            // Clip off prefix and last ')'
            String commandId = id.substring(MOTIONPREFIX.length(), id.length() -1);
            return new SimpleTransition<Motion>(new EclipseCommandMotion(commandId, BorderPolicy.EXCLUSIVE));
        } else {
            return null;
        }
    }

    @Override
    public State<Motion> union(State<Motion> other) {
        return new UnionState<Motion>(this, other);
    }
}
