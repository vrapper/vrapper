package kg.totality.core.keymap;

import kg.totality.core.utils.Function;
import de.jroene.vrapper.eclipse.VrapperPlugin;

public class ConvertingState<T1, T2> extends HashMapState<T1> {

	public ConvertingState(Function<T1, T2> converter, State<T2> wrapped) {
		for (KeyStroke key: wrapped.supportedKeys()) {
			T2 value = wrapped.press(key).getValue();
			State<T2> nextState = wrapped.press(key).getNextState();
			if (nextState != null)
				map.put(key, new ConvertingTransition<T1, T2>(converter, value, nextState));
			else if (value != null)
				map.put(key, new SimpleTransition<T1>(converter.call(value)));
			else
				VrapperPlugin.error("Empty transition in " + wrapped + " for key: " + key);
		}
	}
}
