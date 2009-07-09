package net.sourceforge.vrapper.vim;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import net.sourceforge.vrapper.keymap.KeyMap;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.Remapping;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;

/**
 * Determines whether keystrokes are part of a mapping or not and handles
 * the current state of multi-keystroke mappings.
 *
 * @author Matthias Radig
 */
public class KeyStrokeTranslator {

    private State<Remapping> currentState;
    private Remapping lastValue;
    private final List<RemappedKeyStroke> unconsumedKeyStrokes;
    private final LinkedList<RemappedKeyStroke> resultingKeyStrokes;

    public KeyStrokeTranslator() {
        unconsumedKeyStrokes = new LinkedList<RemappedKeyStroke>();
        resultingKeyStrokes  = new LinkedList<RemappedKeyStroke>();
    }

    public boolean processKeyStroke(KeyMap keymap, KeyStroke key) {
        Transition<Remapping> trans;
        if (currentState == null) {
            trans = keymap.press(key);
            if (trans == null) {
                return false;
            }
        } else {
            trans = currentState.press(key);
        }
        if (trans != null) {
            // mapping exists
            if (trans.getValue() != null) {
                lastValue = trans.getValue();
                unconsumedKeyStrokes.clear();
            } else {
                // as long as no preliminary result is found, keystrokes
                // should not be evaluated again
                boolean recursive = lastValue != null;
                unconsumedKeyStrokes.add(new RemappedKeyStroke(key, recursive));
            }
            if (trans.getNextState() == null) {
                prependUnconsumed();
                prependLastValue();
                currentState = null;
            } else {
                currentState = trans.getNextState();
            }
        } else {
            // mapping ends here
            boolean recursive = lastValue != null;
            unconsumedKeyStrokes.add(new RemappedKeyStroke(key, recursive));
            prependUnconsumed();
            prependLastValue();
            currentState = null;
        }
        return true;
    }

    public Queue<RemappedKeyStroke> resultingKeyStrokes() {
        return resultingKeyStrokes;
    }

    private void prependUnconsumed() {
        resultingKeyStrokes.addAll(0, unconsumedKeyStrokes);
        unconsumedKeyStrokes.clear();
    }

    private void prependLastValue() {
        if (lastValue == null) {
            return;
        }
        boolean recursive = lastValue.isRecursive();
        int i = 0;
        for (KeyStroke key : lastValue.getKeyStrokes()) {
            resultingKeyStrokes.add(i++, new RemappedKeyStroke(key, recursive));
        }
        lastValue = null;
    }
}
