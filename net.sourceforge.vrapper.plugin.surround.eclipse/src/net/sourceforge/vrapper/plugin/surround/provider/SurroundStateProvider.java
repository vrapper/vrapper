package net.sourceforge.vrapper.plugin.surround.provider;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.surround.commands.DeleteDelimitersCommand;
import net.sourceforge.vrapper.plugin.surround.commands.FullLineTextObject;
import net.sourceforge.vrapper.plugin.surround.commands.SpacedDelimitedText;
import net.sourceforge.vrapper.plugin.surround.state.AddDelimiterState;
import net.sourceforge.vrapper.plugin.surround.state.ChangeDelimiterState;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.SimpleDelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;

public class SurroundStateProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new SurroundStateProvider();
    
    public SurroundStateProvider() {
        name = "Surround State Provider";
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> normalModeBindings() {
        State<DelimitedText> delimitedTexts = union(
                state(
                        leafBind('a', (DelimitedText) new SimpleDelimitedText('<', '>')),
                        leafBind('(', (DelimitedText) new SpacedDelimitedText('(', ')')),
                        leafBind('[', (DelimitedText) new SpacedDelimitedText('[', ']')),
                        leafBind('{', (DelimitedText) new SpacedDelimitedText('{', '}'))
                ),
                NormalMode.delimitedTexts()
        );
        State<Command> deleteDelimiterState = new ConvertingState<Command, DelimitedText>(DeleteDelimitersCommand.CONVERTER, delimitedTexts);
        State<Command> changeDelimiterState = new ChangeDelimiterState(delimitedTexts);
        State<Command> addDelimiterState = new AddDelimiterState(
        		union(
    				state(leafBind('s', (TextObject) new FullLineTextObject())),
	        		CountingState.wrap(NormalMode.textObjects())
        		));
        return state(
                transitionBind('d', transitionBind('s', deleteDelimiterState)),
                transitionBind('c', transitionBind('s', changeDelimiterState)),
                transitionBind('y', transitionBind('s', addDelimiterState)));
    }
}
