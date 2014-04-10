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
    private boolean mappingSucceeded = false;

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
            unconsumedKeyStrokes.clear();
            mappingSucceeded = true;
        } else {
            trans = currentState.press(key);
        }
        if (trans != null) {
            // mapping exists
            if (trans.getValue() != null) {
            	//mapping completed successfully
                lastValue = trans.getValue();
                unconsumedKeyStrokes.clear();
            } else { //mapping pending
                // as long as no preliminary result is found, keystrokes
                // should not be evaluated again
                boolean recursive = !unconsumedKeyStrokes.isEmpty() || lastValue != null;
                unconsumedKeyStrokes.add(new RemappedKeyStroke(key, recursive));
            }
            if (trans.getNextState() == null) {
            	//mapping completed
                prependLastValue();
                currentState = null;
            } else {
            	//mapping still pending
                currentState = trans.getNextState();
            }
        } else {
            // mapping was not completed
            unconsumedKeyStrokes.add(new RemappedKeyStroke(key, true));
            prependUnconsumed();
            prependLastValue();
            currentState = null;
            mappingSucceeded = false;
        }
        return true;
    }

    public Queue<RemappedKeyStroke> resultingKeyStrokes() {
        return resultingKeyStrokes;
    }

    public boolean didMappingSucceed() {
        return mappingSucceeded;
    }

    private void prependUnconsumed() {
        //Check if any unmatched keys are in the global map 
        for (int i = 0; i < unconsumedKeyStrokes.size(); i++) {
            KeyStroke key = unconsumedKeyStrokes.get(i);
            if (KeyMap.GLOBAL_MAP.containsKey(key)) {
                unconsumedKeyStrokes.set(i, new RemappedKeyStroke(KeyMap.GLOBAL_MAP.get(key), false));
            }
        }
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
