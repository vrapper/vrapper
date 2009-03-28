package de.jroene.vrapper.vim.commandline;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.jroene.vrapper.vim.VimEmulator;
import de.jroene.vrapper.vim.VimInputEvent;

public class KeyMapper implements Evaluator {

    private static final Pattern pattern = Pattern.compile("<(.+)>");
    private static final Map<String, VimInputEvent> keymap = createKeyMap();

    public Object evaluate(VimEmulator vim, Queue<String> command) {
        String lhs = command.poll();
        String rhs = command.poll();
        if (lhs != null && rhs != null) {
            vim.getNormalMode().overrideMapping(
                    parseInputEvent(lhs),
                    parseInputEvent(rhs));

        }
        return null;
    }

    private VimInputEvent parseInputEvent(String s) {
        Matcher m = pattern.matcher(s);
        if (m.matches()) {
            String key = m.group(1).toUpperCase();
            if (keymap.containsKey(key)) {
                return keymap.get(key);
            }
        }
        return new VimInputEvent.Character(s.charAt(0));
    }

    private static Map<String, VimInputEvent> createKeyMap() {
        HashMap<String, VimInputEvent> map = new HashMap<String, VimInputEvent>();
        // special keys
        map.put("DEL",     VimInputEvent.DELETE);
        map.put("INS",     VimInputEvent.INSERT);
        map.put("BS",      VimInputEvent.BACKSPACE);
        map.put("CR",      VimInputEvent.RETURN);
        map.put("RETURN",  VimInputEvent.RETURN);
        map.put("ENTER",   VimInputEvent.RETURN);
        map.put("HOME",    VimInputEvent.HOME);
        map.put("END",     VimInputEvent.END);
        map.put("PAGEUP",  VimInputEvent.END);
        map.put("PAGEDOWN",VimInputEvent.PAGE_DOWN);
        map.put("UP",      VimInputEvent.ARROW_UP);
        map.put("DOWN",    VimInputEvent.ARROW_DOWN);
        map.put("LEFT",    VimInputEvent.ARROW_LEFT);
        map.put("RIGHT",   VimInputEvent.ARROW_RIGHT);
        // ctrl keys
        map.put("C-@", new VimInputEvent.Character('\u0000'));
        map.put("C-A", new VimInputEvent.Character('\u0001'));
        map.put("C-B", new VimInputEvent.Character('\u0002'));
        map.put("C-C", new VimInputEvent.Character('\u0003'));
        map.put("C-D", new VimInputEvent.Character('\u0004'));
        map.put("C-E", new VimInputEvent.Character('\u0005'));
        map.put("C-F", new VimInputEvent.Character('\u0006'));
        map.put("C-G", new VimInputEvent.Character('\u0007'));
        map.put("C-H", new VimInputEvent.Character('\u0008'));
        map.put("C-I", new VimInputEvent.Character('\t'));
        map.put("C-J", new VimInputEvent.Character('\n'));
        map.put("C-K", new VimInputEvent.Character('\u000B'));
        map.put("C-L", new VimInputEvent.Character('\u000C'));
        map.put("C-M", new VimInputEvent.Character('\r'));
        map.put("C-N", new VimInputEvent.Character('\u000E'));
        map.put("C-O", new VimInputEvent.Character('\u000F'));
        map.put("C-P", new VimInputEvent.Character('\u0010'));
        map.put("C-Q", new VimInputEvent.Character('\u0011'));
        map.put("C-R", new VimInputEvent.Character('\u0012'));
        map.put("C-S", new VimInputEvent.Character('\u0013'));
        map.put("C-T", new VimInputEvent.Character('\u0014'));
        map.put("C-U", new VimInputEvent.Character('\u0015'));
        map.put("C-V", new VimInputEvent.Character('\u0016'));
        map.put("C-W", new VimInputEvent.Character('\u0017'));
        map.put("C-X", new VimInputEvent.Character('\u0018'));
        map.put("C-Y", new VimInputEvent.Character('\u0019'));
        map.put("C-Z", new VimInputEvent.Character('\u001A'));
        map.put("C-[", new VimInputEvent.Character('\u001B'));
        map.put("C-\\",new VimInputEvent.Character('\u001C'));
        map.put("C-]", new VimInputEvent.Character('\u001D'));
        map.put("C-^", new VimInputEvent.Character('\u001E'));
        map.put("C-_", new VimInputEvent.Character('\u001F'));
        return map;
    }

}
