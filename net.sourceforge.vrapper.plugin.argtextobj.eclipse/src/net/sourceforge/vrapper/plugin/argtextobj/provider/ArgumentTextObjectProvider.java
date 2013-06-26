package net.sourceforge.vrapper.plugin.argtextobj.provider;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.VisualTextObjectState;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.argtextobj.commands.ArgumentTextObject;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.YankOperation;

public class ArgumentTextObjectProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ArgumentTextObjectProvider();

    public ArgumentTextObjectProvider() {
        name = "argtextobj State Provider";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {

        final State<TextObject> argObjects = getState();

        final TextOperation delete = DeleteOperation.INSTANCE;
        final TextOperation change = ChangeOperation.INSTANCE;
        final TextOperation yank   = YankOperation.INSTANCE;

        return union(
                operatorCmds('d', delete, argObjects),
                operatorCmds('c', change, argObjects),
                operatorCmds('y', yank,   argObjects));
    }

    @SuppressWarnings("unchecked")
    private State<TextObject> getState() {
        final TextObject innerArgument = ArgumentTextObject.INNER;
        final TextObject outerArgument = ArgumentTextObject.OUTER;

        final State<TextObject> argObjects = state(
                            transitionBind('i',
                                    state(leafBind('a', innerArgument))),
                            transitionBind('a',
                                    state(leafBind('a', outerArgument))));
        return argObjects;
    }

    public class VisualArgumentTextObjectState extends ConvertingState<Command, TextObject>  {
        VisualArgumentTextObjectState(State<TextObject> objects) {
            super(VisualTextObjectState.converter, objects);
        }
    };

    @Override
    protected State<Command> visualModeBindings() {
        final State<TextObject> argObjects = getState();
        return new VisualArgumentTextObjectState(argObjects);
    }
}
