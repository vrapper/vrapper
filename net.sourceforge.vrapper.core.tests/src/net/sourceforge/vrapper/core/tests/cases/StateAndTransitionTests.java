package net.sourceforge.vrapper.core.tests.cases;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;

import org.junit.Test;

public class StateAndTransitionTests {

	private Integer obj1 = 1;
	private Integer obj2 = 2;
	private Integer	 answer = 42;

	@SuppressWarnings("unchecked")
	private State<Integer> state = state(
			leafBind('1', obj1),
			leafBind('2', obj2),
			transitionBind('4',
					leafBind('2', answer)));

	@Test
	public void testHashMapStateSimpleBehaviour() {
		assertSame(obj1, state.press(key('1')).getValue());
		assertSame(obj2, state.press(key('2')).getValue());
		assertNull(state.press(key('3')));

		assertNull(state.press(key('1')).getNextState());
		assertNull(state.press(key('2')).getNextState());

		assertSame(answer, state.press(key('4')).getNextState().press(key('2')).getValue());
		assertNull(state.press(key('4')).getValue());
		assertNull(state.press(key('4')).getNextState().press(key('4')));
		assertNull(state.press(key('4')).getNextState().press(key('2')).getNextState());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void HashMapStateShouldResolveActionConflictsAsFirstOneWins() {
		State<Integer> s1 = new HashMapState<Integer>(asList(leafBind('a', 1), leafBind('a', 2)));
		assertEquals(1, s1.press(key('a')).getValue());
	}


	@Test
	public void SimpleKeyStrokeShouldHaveProperHashAndEqual() {
		assertEquals(key('a'), key('a'));
		assertFalse(key('a').equals(key('b')));
		assertEquals(key('a').hashCode(), key('a').hashCode());
		assertFalse(key('a').hashCode() != key('z').hashCode());
	}

	@Test
	public void unionShouldHandleCommonPrefix() {
		@SuppressWarnings("unchecked")
		State other = state(
				leafBind('3', 3),
				transitionBind('4',
						leafBind('4', 1337)));
		@SuppressWarnings("unchecked")
		State sum = union(state, other);
		assertEquals(1, sum.press(key('1')).getValue());
		assertEquals(2, sum.press(key('2')).getValue());
		assertEquals(3, sum.press(key('3')).getValue());
		assertEquals(42, sum.press(key('4')).getNextState().press(key('2')).getValue());
		assertEquals(1337, sum.press(key('4')).getNextState().press(key('4')).getValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void emptyUnionShouldBeEmpty() {
		assertNull(union().press(key('a')));
	}

	@Test
	public void emptyStateShouldSupportNoKeys() {
		assertFalse(new EmptyState<Object>().supportedKeys().iterator().hasNext());
	}

	@Test
	public void emptyStateShouldReturnOtherOneWhenDoingUnion() {
		assertSame(state, new EmptyState<Integer>().union(state));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void unionOfOneStateShouldBeThisState() {
		State<Object> s = state();
		assertSame(s, union(s));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void unionShouldResolveActionConflictsAsFirstOneWins() {
		State<Integer> s1 = state(leafBind('a', 1));
		State<Integer> s2 = state(leafBind('a', 2));
		assertEquals(1, union(s1, s2).press(key('a')).getValue());
		assertEquals(2, union(s2, s1).press(key('a')).getValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void unionShouldJoinValuesAndNextStates() {
		// rationale: it allows implementation of Vim's behavior
		// which is use timer to disambiguate;
		State<Integer> leaf = state(leafBind('a', 1));
		State<Integer> targetB = state(leafBind('b', 12));
		State<Integer> targetC = state(leafBind('c', 13));
		State<Integer> simpleB = state(transitionBind('a', targetB));
		State<Integer> simpleC = state(transitionBind('a', targetC));
		assertSame(targetB, union(leaf, simpleB).press(key('a')).getNextState());
		assertEquals(1, union(leaf, simpleB).press(key('a')).getValue());
		assertSame(targetB, union(simpleB, leaf).press(key('a')).getNextState());
		assertEquals(1, union(simpleB, leaf).press(key('a')).getValue());
		assertEquals(1, union(leaf, simpleB, simpleC).press(key('a')).getValue());
		assertEquals(12, union(leaf, simpleB, simpleC).press(key('a')).getNextState().press(key('b')).getValue());
		assertEquals(13, union(leaf, simpleB, simpleC).press(key('a')).getNextState().press(key('c')).getValue());
	}

	@Test
	public void testConvertingStateTransitions() {
		final String ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		@SuppressWarnings("unchecked")
		State<Integer> bindings = state(
				transitionBind('1', 0, state(
						leafBind('2', 11),
						leafBind('3', 12))),
				leafBind('2', 1),
				leafBind('3', 2)
				);
		final Function<Character, Integer> converter = new Function<Character, Integer>() {
			public Character call(Integer oldValue) {
				return ABC.charAt(oldValue);
			}
		};
		State<Character> abcMap = new ConvertingState<Character, Integer>(converter, bindings);
		assertEquals('A', abcMap.press(key('1')).getValue());
		assertEquals('B', abcMap.press(key('2')).getValue());
		assertEquals('C', abcMap.press(key('3')).getValue());
		assertEquals('L', abcMap.press(key('1')).getNextState().press(key('2')).getValue());
		assertEquals('M', abcMap.press(key('1')).getNextState().press(key('3')).getValue());
	}

	@Test
	public void testCountingState() {
		final StringBuilder history = new StringBuilder();
		Command command = new CountAwareCommand() {
			@Override
			public void execute(EditorAdaptor editorAdaptor, int count) {
				history.append(count);
			}
			@Override
			public CountAwareCommand repetition() {
				throw new UnsupportedOperationException("method not yet implemented");
			}
		};
		@SuppressWarnings("unchecked")
		State<Command> inner = state(leafBind('c', command));
		State<Command> outer = CountingState.wrap(inner);
		assertSame(command, outer.press(key('c')).getValue());
		State<Command> state4 = outer.press(key('4')).getNextState();
		state4.press(key('c')).getValue().execute(null);
		assertEquals("4", history.toString());
		history.setLength(0);
		State<Command> state42 = state4.press(key('2')).getNextState();
		state42.press(key('c')).getValue().execute(null);
		assertEquals("42", history.toString());
		assertNull(outer.press(key('x')));
		assertNull(outer.press(key('2')).getNextState().press(key('x')));
	}

	@Test
	public void testUnionStateLeaf() {
		@SuppressWarnings("unchecked") State<Integer> state_a1 = state(leafBind('a', 1));
		@SuppressWarnings("unchecked") State<Integer> state_a2 = state(leafBind('a', 2));
		@SuppressWarnings("unchecked") State<Integer> state_b2 = state(leafBind('b', 2));
		assertEquals(1, new UnionState<Integer>(state_a1, state_a2).press(key('a')).getValue());
		assertEquals(2, new UnionState<Integer>(state_a2, state_a1).press(key('a')).getValue());
		assertEquals(1, new UnionState<Integer>(state_a1, state_b2).press(key('a')).getValue());
		assertEquals(2, new UnionState<Integer>(state_a1, state_b2).press(key('b')).getValue());
		assertEquals(1, new UnionState<Integer>(state_b2, state_a1).press(key('a')).getValue());
		assertEquals(2, new UnionState<Integer>(state_b2, state_a1).press(key('b')).getValue());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUnionStateTransition() {
		State<Integer> leaf3 = new HashMapState<Integer>() { @Override public String toString() { return "state3"; } };
		State<Integer> leaf4 = new HashMapState<Integer>() { @Override public String toString() { return "state4"; } };
		State<Integer> state1 = state(transitionBind('a', leafBind('a', 1), transitionBind('b', leaf3)));
		State<Integer> state2 = state(transitionBind('a', leafBind('a', 2), transitionBind('c', leaf4)));
		State<Integer> union12 = new UnionState<Integer>(state1, state2);
		State<Integer> union21 = new UnionState<Integer>(state2, state1);
		assertEquals(1, union12.press(key('a')).getNextState().press(key('a')).getValue());
		assertEquals(2, union21.press(key('a')).getNextState().press(key('a')).getValue());
		assertSame(leaf3, union12.press(key('a')).getNextState().press(key('b')).getNextState());
		assertSame(leaf4, union12.press(key('a')).getNextState().press(key('c')).getNextState());
	}

	@Test
	@SuppressWarnings("unchecked")
	public void testUnionStateSupportedKeys() {
		State<Integer> state1234 = state( leafBind('1', 1), leafBind('2', 2), leafBind('3', 3), leafBind('4', 4) );
		State<Integer> state3456 = state( leafBind('3', 3), leafBind('4', 4), leafBind('5', 5), leafBind('6', 6) );
		State<Integer> state789  = state( leafBind('7', 7), leafBind('8', 8), leafBind('9', 9));

		State<Integer> unionState = new UnionState(state1234, state3456);
		Set<KeyStroke> expected = new HashSet<KeyStroke>();
		for (char chr='1'; chr<='6'; chr++) expected.add(key(chr));
		assertEquals(expected, unionState.supportedKeys());
		for (char chr='7'; chr<='9'; chr++) expected.add(key(chr));
		assertEquals(expected, unionState.union(state789).supportedKeys());
	}
}
