package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;



/**
 * Placeholder for Java-ugliness-hiding static methods intended to be statically imported
 * @author Krzysiek Goj
 */
public class ConstructorWrappers {

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
        };
    }

    public static Command motionCmd(Motion motion) {
        return new MotionCommand(motion);
    }

}