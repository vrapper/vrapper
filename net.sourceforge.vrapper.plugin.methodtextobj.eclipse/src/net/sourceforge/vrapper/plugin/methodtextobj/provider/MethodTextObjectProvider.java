package net.sourceforge.vrapper.plugin.methodtextobj.provider;

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
import net.sourceforge.vrapper.plugin.methodtextobj.commands.MethodTextObject;
import net.sourceforge.vrapper.vim.commands.ChangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DeleteOperation;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.YankOperation;

public class MethodTextObjectProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new MethodTextObjectProvider();

    public MethodTextObjectProvider() {
        name = "methodtextobj State Provider";
    }

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {

        final State<TextObject> methodObjects = getState();

        final TextOperation delete = DeleteOperation.INSTANCE;
        final TextOperation change = ChangeOperation.INSTANCE;
        final TextOperation yank   = YankOperation.INSTANCE;

        return union(
                operatorCmds('d', delete, methodObjects),
                operatorCmds('c', change, methodObjects),
                operatorCmds('y', yank,   methodObjects));
    }

    @SuppressWarnings("unchecked")
    private State<TextObject> getState() {
        final TextObject innerArgument = MethodTextObject.INNER;
        final TextObject outerArgument = MethodTextObject.OUTER;

        final State<TextObject> argObjects = state(
        		transitionBind('i',
        				state(leafBind('m', innerArgument))),
        		transitionBind('a',
        				state(leafBind('m', outerArgument))));
        return argObjects;
    }

    public class VisualMethodTextObjectState extends ConvertingState<Command, TextObject>  {
        VisualMethodTextObjectState(State<TextObject> objects) {
            super(VisualTextObjectState.converter, objects);
        }
    };

    @Override
    protected State<Command> visualModeBindings() {
        return new VisualMethodTextObjectState( getState() );
    }
}
