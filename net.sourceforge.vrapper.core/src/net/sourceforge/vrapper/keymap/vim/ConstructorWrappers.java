package net.sourceforge.vrapper.keymap.vim;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.vim.commands.BorderPolicy.LINE_WISE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sourceforge.vrapper.keymap.CovariantState;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyBinding;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.SimpleKeyBinding;
import net.sourceforge.vrapper.keymap.SimpleTransition;
import net.sourceforge.vrapper.keymap.SpecialKey;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.PerformOperationOnSearchResultCommand;
import net.sourceforge.vrapper.vim.commands.ChangeCaretShapeCommand;
import net.sourceforge.vrapper.vim.commands.ChangeToSearchModeCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.MotionTextObject;
import net.sourceforge.vrapper.vim.commands.SelectionBasedTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.TextObject;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperationTextObjectCommand;
import net.sourceforge.vrapper.vim.commands.motions.LineEndMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.SearchResultMotion;

/**
 * Static utility methods to construct keymaps.
 * These Java-ugliness-hiding static methods are intended to be statically imported.
 * @author Krzysiek Goj
 */
public class ConstructorWrappers {
    private static final Map<String, KeyStroke> keyNames = createKeyMap();

    @SuppressWarnings("serial")
    static final Map<SpecialKey, String> specialKeyNames = Collections.unmodifiableMap(new HashMap<SpecialKey, String>() {{
        for (SpecialKey key: SpecialKey.values()) {
            String keyName = key.toString();
            put(key, "<"+keyName+">");
        }
        put(SpecialKey.ARROW_LEFT,  "<LEFT>");
        put(SpecialKey.ARROW_RIGHT, "<RIGHT>");
        put(SpecialKey.ARROW_UP,    "<UP>");
        put(SpecialKey.ARROW_DOWN,  "<DOWN>");
    }});

    //    private static final Pattern pattern = Pattern.compile("<(.+)>");

    public static Iterable<KeyStroke> parseKeyStrokes(String s) {
        List<KeyStroke> result = new ArrayList<KeyStroke>();
        for (int i = 0; i < s.length(); i++) {
            char next = s.charAt(i);
            if (next == '<') {
                StringBuilder sb = new StringBuilder();
                while (next != '>' && ++i < s.length()) {
                    next = s.charAt(i);
                    sb.append(next);
                }
                KeyStroke stroke = null;
                if (sb.length() > 0) {
                    sb.deleteCharAt(sb.length()-1);
                    String key = sb.toString().toUpperCase();
                    stroke = parseSpecialKey(key);
                    if (stroke == null) {
                        VrapperLog.info("Key code <" + key + "> is unknown. Ignoring.");
                    }
                }
                if (stroke != null) {
                    result.add(stroke);
                }
            } else {
                result.add(key(next));
            }
        }
        return result;
    }
    
    /**
     * Parse the KeyStoke found within '<' and '>' tags.
     * For example:
     * <Insert>
     * <Left>
     * <S-Home>
     * <A-X>
     * <M-Left>
     * @param key - String found within '<' and '>'
     * @return KeyStroke representing key's Key
     */
    private static KeyStroke parseSpecialKey(String key) {
    	KeyStroke stroke = null;
    	//Sanity check for recursion.
    	if (key.length() > 20 || key.length() == 0) {
    		return null;
    	}
    	if(key.startsWith("S-")) { //Shift
    		KeyStroke k = parseSpecialKey( key.substring(2) );
    		if(k != null) {
    			if (k.getSpecialKey() == null && ! k.withCtrlKey() && k.getCharacter() > ' ') {
    				//for combinations like A-S-x. Never convert S-C-x to uppercase!
    				stroke = new SimpleKeyStroke(Character.toUpperCase(k.getCharacter()),
    						true, k.withAltKey(), k.withCtrlKey());
    			} else {
    				stroke = new SimpleKeyStroke(k, true, k.withAltKey(), k.withCtrlKey());
    			}
    		}
    	} else if(key.startsWith("A-") || key.startsWith("M-")) { //Alt (Meta)
    		KeyStroke k = parseSpecialKey(key.substring(2));
    		if(k != null) {
    			stroke = new SimpleKeyStroke(k, k.withShiftKey(), true, k.withCtrlKey());
    		}
    	} else if (key.startsWith("C-")) { //Control
    		KeyStroke k = parseSpecialKey(key.substring(2));
    		if (k != null) {
    			stroke = new SimpleKeyStroke(k, k.withShiftKey(), k.withAltKey(), true);
    		}
    	} else if (keyNames.containsKey(key)) {
    		stroke = keyNames.get(key);

    	} else if (key.length() == 1 && key.charAt(0) >= ' ') {
    		//normal character, not special key (e.g., <A-x>)
    		//force lower-case, let the shift modifier convert it back to uppercase if needed.
    		stroke = new SimpleKeyStroke(key.toLowerCase().charAt(0));
    	}
    	// else we return null, maybe some unknown special key?
    	return stroke;
    }

    public static String keyStrokesToString(Iterable<KeyStroke> strokes) {
        StringBuilder sb = new StringBuilder();
        for (KeyStroke stroke : strokes) {
            sb.append(keyStrokeToString(stroke));
        }
        return sb.toString();
    }

    public static String keyStrokeToString(KeyStroke stroke) {
        if (stroke.getSpecialKey() == null) {
            String key = String.valueOf(stroke.getCharacter());
            if (stroke.getCharacter() >= ' ') {
                switch (stroke.getCharacter()) {
                case '<':
                    return "<LT>";
                case '>':
                    return "<GT>";
                case ' ':
                    return "<SPACE>";
                default:
                    return key;
                }
            }
            return "<C-"+key+">";
        }
        return specialKeyNames.get(stroke.getSpecialKey());
    }

    public static KeyStroke key(char key) {
        return new SimpleKeyStroke(key);
    }

    public static KeyStroke ctrlKey(char key) {
        return new SimpleKeyStroke(Character.toLowerCase(key), false, false, true);
    }

    public static KeyStroke key(SpecialKey key) {
        return new SimpleKeyStroke(key);
    }

    public static<T> KeyBinding<T> binding(char k, Transition<T> transition) {
        return new SimpleKeyBinding<T>(key(k), transition);
    }

    public static<T> KeyBinding<T> binding(SpecialKey k, Transition<T> transition) {
        return new SimpleKeyBinding<T>(key(k), transition);
    }

    public static<T> KeyBinding<T> binding(KeyStroke stroke, Transition<T> transition) {
        return new SimpleKeyBinding<T>(stroke, transition);
    }

    public static<T> Transition<T> leaf(T value) {
        return new SimpleTransition<T>(value);
    }

    public static<T> Transition<T> transition(State<T> state) {
        return new SimpleTransition<T>(state);
    }

    public static<T> Transition<T> transition(T value, State<T> state) {
        return new SimpleTransition<T>(value, state);
    }

    public static<T> State<T> state(KeyBinding<T>... bindings) {
        return new HashMapState<T>(asList(bindings));
    }

    public static<T> KeyBinding<T> leafBind(KeyStroke k, T value) {
        return binding(k, leaf(value));
    }

    public static<T> KeyBinding<T> leafBind(char k, T value) {
        return binding(k, leaf(value));
    }

    public static<T> KeyBinding<T> leafBind(SpecialKey k, T value) {
        return binding(k, leaf(value));
    }

    public static<T> KeyBinding<T> leafCtrlBind(char k, T value) {
        return binding(ctrlKey(k), leaf(value));
    }

    public static<T> KeyBinding<T> transitionBind(char k, State<T> state) {
        return binding(k, transition(state));
    }

    public static<T> KeyBinding<T> transitionBind(KeyStroke k, State<T> state) {
        return binding(k, transition(state));
    }

    public static<T> KeyBinding<T> transitionBind(char k, T value, State<T> state) {
        return binding(k, transition(value, state));
    }

    public static<T> KeyBinding<T> transitionBind(char k, KeyBinding<T>... bindings) {
        return binding(k, transition(state(bindings)));
    }

    @SuppressWarnings("unchecked")
    public static<T> State<T> leafState(char k, T value) {
        return state(leafBind(k, value));
    }

    @SuppressWarnings("unchecked")
    public static<T> State<T> leafState(KeyStroke k, T value) {
        return state(leafBind(k, value));
    }

    @SuppressWarnings("unchecked")
    public static<T> State<T> transitionState(char k, State<T> state) {
        return state(transitionBind(k, state));
    }

    @SuppressWarnings("unchecked")
    public static<T> State<T> transitionState(KeyStroke k, State<T> state) {
        return state(transitionBind(k, state));
    }

    public static SelectionBasedTextObjectCommand operatorMoveCmd(Command operator, Motion move) {
        return new SelectionBasedTextObjectCommand(operator, new MotionTextObject(move));
    }

    public static State<Command> counted(State<Command> wrapped) {
        return CountingState.wrap(wrapped);
    }


    public static ChangeCaretShapeCommand changeCaret(CaretType caret) {
        return ChangeCaretShapeCommand.getInstance(caret);
    }

    @SuppressWarnings("unchecked")
    private static State<Command> operatorPendingState(char key,
            State<Command> doubleKey, State<Command> operatorCmds) {
        return state(binding(key,
                transition(changeCaret(CaretType.HALF_RECT),
                        counted(union(doubleKey, operatorCmds)))));
    }

    @SuppressWarnings("unchecked")
    public static State<Command> operatorCmdsWithUpperCase(char key, TextOperation command, TextObject eolMotion, State<TextObject> textObjects) {
        assert Character.isLowerCase(key);
        Command doToEOL = new TextOperationTextObjectCommand(command, eolMotion);
        return union(
                leafState(Character.toUpperCase(key), doToEOL), // FIXME: was: counted(...)
                operatorCmds(key, command, textObjects));
    }

    @SuppressWarnings("unchecked")
	public static State<Command> operatorCmds(char key, TextOperation command, State<TextObject> textObjects) {
        LineEndMotion lineEndMotion = new LineEndMotion(LINE_WISE);
        Command doLinewise = new TextOperationTextObjectCommand(command, new MotionTextObject(lineEndMotion));
        State<Command> doubleKey = leafState(key, doLinewise);
        State<Command> operatorCmds = union(
		    	leafState('/', (Command) new ChangeToSearchModeCommand(false, new PerformOperationOnSearchResultCommand(command, SearchResultMotion.FORWARD))),
		    	leafState('?', (Command) new ChangeToSearchModeCommand(true, new PerformOperationOnSearchResultCommand(command, SearchResultMotion.FORWARD))),
	    		new OperatorCommandState(command, textObjects)
    	);
        return operatorPendingState(key, doubleKey, operatorCmds);
    }

    public static State<Command> operatorCmds(char key, Command operator, State<TextObject> textObjects) {
        Command doLinewise = operatorMoveCmd(operator, new LineEndMotion(LINE_WISE));
        State<Command> doubleKey = leafState(key, doLinewise);
        State<Command> operatorCmds = new OperatorCommandState(operator, textObjects);
        return operatorPendingState(key, doubleKey, operatorCmds);
    }
    
    public static State<Command> prefixedOperatorCmds(char prefix, char key, TextOperation command, State<TextObject> textObjects) {
        LineEndMotion lineEndMotion = new LineEndMotion(LINE_WISE);
        Command doLinewise = new TextOperationTextObjectCommand(command, new MotionTextObject(lineEndMotion));
        @SuppressWarnings("unchecked")
        State<Command> doubleKey = state(
                leafBind(key, doLinewise), // e.g. for 'g??'
                transitionBind(prefix, leafBind(key, doLinewise))); // e.g. for 'g?g?'
        State<Command> operatorCmds = new OperatorCommandState(command, textObjects);
        return transitionState(prefix, operatorPendingState(key, doubleKey, operatorCmds));
    }

    public static State<Command> prefixedOperatorCmds(char prefix, char key, Command operator, State<TextObject> textObjects) {
        Command doLinewise = operatorMoveCmd(operator, new LineEndMotion(LINE_WISE));
        @SuppressWarnings("unchecked")
        State<Command> doubleKey = state(
                leafBind(key, doLinewise), // e.g. for 'g??'
                transitionBind(prefix, leafBind(key, doLinewise))); // e.g. for 'g?g?'
        State<Command> operatorCmds = new OperatorCommandState(operator, textObjects);
        return transitionState(prefix, operatorPendingState(key, doubleKey, operatorCmds));
    }

    public static <T> State<T> convertKeyStroke(Function<T, KeyStroke> converter, Set<KeyStroke> keystrokes) {
        return new KeyStrokeConvertingState<T>(converter, keystrokes);
    }

    public static<T1, T2 extends T1> State<T1> covariant(State<T2> wrapped) {
        return new CovariantState<T1, T2>(wrapped);
    }

    private static Map<String, KeyStroke> createKeyMap() {
        HashMap<String, KeyStroke> map = new HashMap<String, KeyStroke>();
        // special keys
        for (SpecialKey key : SpecialKey.values()) {
            map.put(key.name().toUpperCase(), key(key));
        }
        map.put("DEL",     key(SpecialKey.DELETE));
        map.put("INS",     key(SpecialKey.INSERT));
        map.put("BS",      key(SpecialKey.BACKSPACE));
        map.put("RETURN",  key(SpecialKey.RETURN));
        map.put("ENTER",   map.get("RETURN"));
        map.put("CR",      map.get("RETURN"));
        map.put("PAGEUP",  key(SpecialKey.PAGE_UP));
        map.put("PAGEDOWN",key(SpecialKey.PAGE_DOWN));
        map.put("UP",      key(SpecialKey.ARROW_UP));
        map.put("DOWN",    key(SpecialKey.ARROW_DOWN));
        map.put("LEFT",    key(SpecialKey.ARROW_LEFT));
        map.put("RIGHT",   key(SpecialKey.ARROW_RIGHT));
        map.put("TAB",     key(SpecialKey.TAB));
        map.put("SPACE",   key(' '));
        map.put("GT",      key('>'));
        map.put("LT",      key('<'));
        
        //function keys
        SpecialKey[] values = SpecialKey.values();
        for (int i=0, start=SpecialKey.F1.ordinal(); i < 20; ++i)
        	map.put("F" + i+1, key(values[start+i]));
        
        // add these ctrl keys to keep parseSpecialKey working
        map.put("C-@", new SimpleKeyStroke('@', false, false, true));
        map.put("C-A", new SimpleKeyStroke('a', false, false, true));
        map.put("C-B", new SimpleKeyStroke('b', false, false, true));
        map.put("C-C", new SimpleKeyStroke('c', false, false, true));
        map.put("C-D", new SimpleKeyStroke('d', false, false, true));
        map.put("C-E", new SimpleKeyStroke('e', false, false, true));
        map.put("C-F", new SimpleKeyStroke('f', false, false, true));
        map.put("C-G", new SimpleKeyStroke('g', false, false, true));
        map.put("C-H", new SimpleKeyStroke('h', false, false, true));
        map.put("C-I", new SimpleKeyStroke('i', false, false, true));
        map.put("C-J", new SimpleKeyStroke('j', false, false, true));
        map.put("C-K", new SimpleKeyStroke('k', false, false, true));
        map.put("C-L", new SimpleKeyStroke('l', false, false, true));
        map.put("C-M", new SimpleKeyStroke('m', false, false, true));
        map.put("C-N", new SimpleKeyStroke('n', false, false, true));
        map.put("C-O", new SimpleKeyStroke('o', false, false, true));
        map.put("C-P", new SimpleKeyStroke('p', false, false, true));
        map.put("C-Q", new SimpleKeyStroke('q', false, false, true));
        map.put("C-R", new SimpleKeyStroke('r', false, false, true));
        map.put("C-S", new SimpleKeyStroke('s', false, false, true));
        map.put("C-T", new SimpleKeyStroke('t', false, false, true));
        map.put("C-U", new SimpleKeyStroke('u', false, false, true));
        map.put("C-V", new SimpleKeyStroke('v', false, false, true));
        map.put("C-W", new SimpleKeyStroke('w', false, false, true));
        map.put("C-X", new SimpleKeyStroke('x', false, false, true));
        map.put("C-Y", new SimpleKeyStroke('y', false, false, true));
        map.put("C-Z", new SimpleKeyStroke('z', false, false, true));
        map.put("C-[", new SimpleKeyStroke('[', false, false, true));
        map.put("C-\\",new SimpleKeyStroke('\\', false, false, true));
        map.put("C-]", new SimpleKeyStroke(']', false, false, true));
        map.put("C-^", new SimpleKeyStroke('^', false, false, true));
        map.put("C-_", new SimpleKeyStroke('_', false, false, true));
        return map;
    }
}
