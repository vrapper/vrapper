package net.sourceforge.vrapper.plugin.surround.provider;

import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;

import java.util.Queue;

import net.sourceforge.vrapper.eclipse.keymap.AbstractEclipseSpecificStateProvider;
import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.platform.PlatformSpecificStateProvider;
import net.sourceforge.vrapper.plugin.surround.commands.DeleteDelimitersCommand;
import net.sourceforge.vrapper.plugin.surround.commands.FullLineTextObject;
import net.sourceforge.vrapper.plugin.surround.commands.SpacedDelimitedText;
import net.sourceforge.vrapper.plugin.surround.state.AddDelimiterState;
import net.sourceforge.vrapper.plugin.surround.state.AddVisualDelimiterState;
import net.sourceforge.vrapper.plugin.surround.state.ChangeDelimiterState;
import net.sourceforge.vrapper.plugin.surround.state.DelimiterValues;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.DelimitedText;
import net.sourceforge.vrapper.vim.commands.InsertShiftWidth;
import net.sourceforge.vrapper.vim.commands.SimpleDelimitedText;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.modes.commandline.Evaluator;

public class SurroundStateProvider extends AbstractEclipseSpecificStateProvider {
    public static final PlatformSpecificStateProvider INSTANCE = new SurroundStateProvider();
    
    protected static class SurroundEvaluator implements Evaluator {

        public Object evaluate(EditorAdaptor vim, Queue<String> command) {
            try {
                if (command.size() != 2 || command.peek().length() != 1) {
                    throw new IllegalArgumentException(":surround expects a character key and a definition");
                }
                String key = command.poll();
                String[] surroundDef = command.poll().replaceAll("(?i)<SPACE>", " ").split("\\\\r");
                if (surroundDef.length != 2) {
                    throw new IllegalArgumentException(":surround definition must contain '\\r'");
                }
                DelimiterValues.DELIMITER_HOLDERS.addDelimiterHolder(key.charAt(0), surroundDef[0], surroundDef[1]);
            } catch (Exception e) {
                vim.getUserInterfaceService().setErrorMessage(e.getMessage());
            }
            return null;
        }

    }
    public SurroundStateProvider() {
        name = "Surround State Provider";
        commands.add("surround", new SurroundEvaluator());
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

    @Override
    @SuppressWarnings("unchecked")
    protected State<Command> visualModeBindings() {
        InsertShiftWidth shift = InsertShiftWidth.INSERT;
        return state(
                transitionBind('S', new AddVisualDelimiterState(false, shift)),
                transitionBind('g', transitionBind('S',  new AddVisualDelimiterState(true, shift)))
               );
    }
}
