package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

/**
 * Static utility methods to wrap commands or motions.
 * These Java-ugliness-hiding static methods are intended to be statically imported.
 * @author Krzysiek Goj
 */
public class CommandWrappers {

    public static Command seq(Command... commands) {
        return new VimCommandSequence(commands);
    }

    public static Command dontRepeat(final Command wrapped) {
        return new AbstractCommand() {
            public CountAwareCommand repetition() {
                return null;
            }
            public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
                wrapped.execute(editorAdaptor);
            }
            public Command withCount(int count) {
                return wrapped;
            }
            @Override
            public String toString() {
            	return String.format("dontRepeat(%s)", wrapped);
            }
        };
    }

    public static Command repeat(int count, final Command command) {
        return new MultipleExecutionCommand(count, command);
    }

    public static Command motionCmd(Motion motion) {
        return new MotionCommand(motion);
    }

    /**
     * Silly helper method which implicitly casts the input to a {@link Command}. Useful when
     * dealing with all the generics used in the keymap types as its shorter to write
     * <code>cmd(new Instance())</code> than writing <code>(Command) new Instance()</code>.
     */
    public static Command cmd(Command command) {
        return command;
    }

}