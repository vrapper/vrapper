package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;



/**
 * Placeholder for Java-ugliness-hiding static methods intended to be statically imported
 * @author Krzysiek Goj
 */
public class ConstructorWrappers {

    // Motions
    public static Motion go(String where, BorderPolicy borderPolicy) {
        return new EclipseMoveCommand("org.eclipse.ui.edit.text.goto." + where, borderPolicy);
    }

    public static Motion javaGoTo(String where, BorderPolicy borderPolicy) {
        // FIXME: this is temporary, keymap should be language-independent
        return new EclipseMoveCommand("org.eclipse.jdt.ui.edit.text.java.goto." + where, borderPolicy);
    }

    // VimCommands
    public static Command go(String where) {
        return new EclipseCommand("org.eclipse.ui.edit.text.goto." + where);
    }

    public static Command seq(Command... commands) {
        return new VimCommandSequence(commands);
    }

    public static Command dontRepeat(final Command wrapped) {
        return new AbstractCommand() {
            public CountAwareCommand repetition() {
                return null;
            }
            public void execute(EditorAdaptor editorAdaptor) {
                wrapped.execute(editorAdaptor);
            }
            public Command withCount(int count) {
                return wrapped;

            }
        };
    }

    public static Command cmd(String command) {
        return new EclipseCommand(command);
    }

    public static Command motionCmd(Motion motion) {
        return new MotionCommand(motion);
    }

    public static Command edit(String command) {
        return new EclipseCommand("org.eclipse.ui.edit." + command);
    }

    public static EclipseCommand editText(String command) {
        return new EclipseCommand("org.eclipse.ui.edit.text." + command);
    }

    public static Command javaEditText(String cmd) {
        // FIXME: this is temporary, keymap should be language-independent
        return new EclipseCommand("org.eclipse.jdt.ui.edit.text.java." + cmd);
    }

}