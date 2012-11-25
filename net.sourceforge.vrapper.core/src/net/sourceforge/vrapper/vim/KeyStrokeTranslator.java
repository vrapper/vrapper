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
    private int numUnconsumed = 0;

    public KeyStrokeTranslator() {
        unconsumedKeyStrokes = new LinkedList<RemappedKeyStroke>();
        resultingKeyStrokes  = new LinkedList<RemappedKeyStroke>();
    }

    public boolean processKeyStroke(KeyMap keymap, KeyStroke key) {
        Transition<Remapping> trans;
        if (currentState == null) {
            trans = keymap.press(key);
            if (trans == null) {
            	//no mapping begins with this key
                return false;
            }
            //begin new mapping, make sure values are reset
            resultingKeyStrokes.clear();
            numUnconsumed = 0;
        } else {
            trans = currentState.press(key);
        }
        if (trans != null) {
            // mapping exists
            if (trans.getValue() != null) {
                lastValue = trans.getValue();
                numUnconsumed = unconsumedKeyStrokes.size();
                unconsumedKeyStrokes.clear();
            } else {
                // as long as no preliminary result is found, keystrokes
                // should not be evaluated again
                boolean recursive = !unconsumedKeyStrokes.isEmpty() || lastValue != null;
                unconsumedKeyStrokes.add(new RemappedKeyStroke(key, recursive));
                numUnconsumed++;
            }
            if (trans.getNextState() == null) {
            	//mapping did not complete
            	unconsumedKeyStrokes.clear();
                prependLastValue();
                currentState = null;
            } else {
                currentState = trans.getNextState();
            }
        } else {
            // mapping ends here
        	unconsumedKeyStrokes.clear();
            prependLastValue();
            currentState = null;
        }
        return true;
    }

    public Queue<RemappedKeyStroke> resultingKeyStrokes() {
        return resultingKeyStrokes;
    }

    public int numUnconsumedKeys() {
    	//how many keys are being swallowed by this completed mapping?
        return numUnconsumed;
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
