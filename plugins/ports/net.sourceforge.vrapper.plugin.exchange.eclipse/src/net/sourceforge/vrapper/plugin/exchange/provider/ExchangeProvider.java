package net.sourceforge.vrapper.plugin.exchange.provider;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafState;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.operatorCmds;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionState;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.LINE_WISE;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.exchange.commands.ClearExchangeRegionCommand;
import net.sourceforge.vrapper.plugin.exchange.commands.ExchangeOperation;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextOperationCommand;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.modes.KeyMapResolver;

public class ExchangeProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new ExchangeProvider();

    public ExchangeProvider() {
        name = "Exchange State Provider";
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> normalModeBindings() {
        final LineEndMotion lineEndMotion = new LineEndMotion(LINE_WISE);
        final Command doLinewise = new TextOperationTextObjectCommand((TextOperation)ExchangeOperation.INSTANCE, new MotionTextObject(lineEndMotion));
        return 
            transitionState('c', union(
                    state(transitionBind('x', union(leafState('c', (Command)ClearExchangeRegionCommand.INSTANCE),
                                                    leafState('x', doLinewise)))),
                            operatorCmds('x', (TextOperation)ExchangeOperation.INSTANCE, textObjectProvider.textObjects())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public State<String> normalModeKeymap() {
        return state(
                transitionBind('c', leafBind('x', KeyMapResolver.OMAP_NAME)));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected State<Command> visualModeBindings() {
        final Command exchangeVisual = new SelectionBasedTextOperationCommand(ExchangeOperation.INSTANCE);
        return state(leafBind('X', exchangeVisual));
    }

}
