package net.sourceforge.vrapper.core.tests.cases;

import static java.util.Arrays.asList;
import static net.sourceforge.vrapper.keymap.StateUtils.union;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.key;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.leafBind;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.parseKeyStrokes;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.state;
import static net.sourceforge.vrapper.keymap.vim.ConstructorWrappers.transitionBind;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import net.sourceforge.vrapper.keymap.ConvertingState;
import net.sourceforge.vrapper.keymap.EmptyState;
import net.sourceforge.vrapper.keymap.HashMapState;
import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.State;
import net.sourceforge.vrapper.keymap.Transition;
import net.sourceforge.vrapper.keymap.UnionState;
import net.sourceforge.vrapper.keymap.WrappingState;
import net.sourceforge.vrapper.keymap.vim.CountingState;
import net.sourceforge.vrapper.utils.Function;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;

import org.junit.Test;

public class StateAndTransitionTests {

    private final Integer obj1 = 1;
    private final Integer obj2 = 2;
    private final Integer answer = 42;

    @SuppressWarnings("unchecked")
    private final State<Integer> state = state(leafBind('1', obj1), leafBind('2',
            obj2), transitionBind('4', leafBind('2', answer)));

    @Test
    public void testHashMapStateSimpleBehaviour() {
        assertSame(obj1, state.press(key('1')).getValue());
        assertSame(obj2, state.press(key('2')).getValue());
        assertNull(state.press(key('3')));

        assertNull(state.press(key('1')).getNextState());
        assertNull(state.press(key('2')).getNextState());

        assertSame(answer, state.press(key('4')).getNextState().press(key('2'))
                .getValue());
        assertNull(state.press(key('4')).getValue());
        assertNull(state.press(key('4')).getNextState().press(key('4')));
        assertNull(state.press(key('4')).getNextState().press(key('2'))
                .getNextState());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkThatHashMapStateShouldResolveActionConflictsAsFirstOneWins() {
        State<Integer> s1 = new HashMapState<Integer>(asList(leafBind('a', 1),
                leafBind('a', 2)));
        assertEquals((Integer) 1, s1.press(key('a')).getValue());
    }

    @Test
    public void unionShouldHandleCommonPrefix() {
        @SuppressWarnings("unchecked")
        State other = state(leafBind('3', 3), transitionBind('4', leafBind('4',
                1337)));
        @SuppressWarnings("unchecked")
        State sum = union(state, other);
        assertEquals(1, sum.press(key('1')).getValue());
        assertEquals(2, sum.press(key('2')).getValue());
        assertEquals(3, sum.press(key('3')).getValue());
        assertEquals(42, sum.press(key('4')).getNextState().press(key('2'))
                .getValue());
        assertEquals(1337, sum.press(key('4')).getNextState().press(key('4'))
                .getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void emptyUnionShouldBeEmpty() {
        assertNull(union().press(key('a')));
    }

    @Test
    public void emptyStateShouldReturnOtherOneWhenDoingUnion() {
        State<Integer> emptyState = EmptyState.getInstance();
        assertSame(state, emptyState.union(state));
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
        assertEquals((Integer) 1, union(s1, s2).press(key('a')).getValue());
        assertEquals((Integer) 2, union(s2, s1).press(key('a')).getValue());
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
        assertEquals((Integer) 1, union(leaf, simpleB).press(key('a')).getValue());
        assertSame(targetB, union(simpleB, leaf).press(key('a')).getNextState());
        assertEquals((Integer) 1, union(simpleB, leaf).press(key('a')).getValue());
        assertEquals((Integer) 1, union(leaf, simpleB, simpleC).press(key('a'))
                .getValue());
        assertEquals((Integer) 12, union(leaf, simpleB, simpleC).press(key('a'))
                .getNextState().press(key('b')).getValue());
        assertEquals((Integer) 13, union(leaf, simpleB, simpleC).press(key('a'))
                .getNextState().press(key('c')).getValue());
    }

    @Test
    public void testConvertingStateTransitions() {
        final String ABC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        @SuppressWarnings("unchecked")
        State<Integer> bindings = state(transitionBind('1', 0, state(leafBind(
                '2', 11), leafBind('3', 12))), leafBind('2', 1), leafBind('3',
                2));
        final Function<Character, Integer> converter = new Function<Character, Integer>() {
            public Character call(Integer oldValue) {
                return ABC.charAt(oldValue);
            }
        };
        State<Character> abcMap = new ConvertingState<Character, Integer>(
                converter, bindings);
        assertEquals((Character) 'A', abcMap.press(key('1')).getValue());
        assertEquals((Character) 'B', abcMap.press(key('2')).getValue());
        assertEquals((Character) 'C', abcMap.press(key('3')).getValue());
        assertEquals((Character) 'L', abcMap.press(key('1')).getNextState().press(key('2'))
                .getValue());
        assertEquals((Character) 'M', abcMap.press(key('1')).getNextState().press(key('3'))
                .getValue());
    }

    @Test
    public void testCountingState() throws CommandExecutionException {
        final StringBuilder history = new StringBuilder();
        Command command = new CountAwareCommand() {
            @Override
            public void execute(EditorAdaptor editorAdaptor, int count) {
                history.append(count);
            }

            @Override
            public CountAwareCommand repetition() {
                throw new UnsupportedOperationException();
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
    @SuppressWarnings("unchecked")
    public void testUnionStateLeaf() {
        State<Integer> state_a1 = state(leafBind('a', 1));
        State<Integer> state_a2 = state(leafBind('a', 2));
        State<Integer> state_b2 = state(leafBind('b', 2));
        assertEquals((Integer) 1, new UnionState<Integer>(state_a1, state_a2).press(
                key('a')).getValue());
        assertEquals((Integer) 2, new UnionState<Integer>(state_a2, state_a1).press(
                key('a')).getValue());
        assertEquals((Integer) 1, new UnionState<Integer>(state_a1, state_b2).press(
                key('a')).getValue());
        assertEquals((Integer) 2, new UnionState<Integer>(state_a1, state_b2).press(
                key('b')).getValue());
        assertEquals((Integer) 1, new UnionState<Integer>(state_b2, state_a1).press(
                key('a')).getValue());
        assertEquals((Integer) 2, new UnionState<Integer>(state_b2, state_a1).press(
                key('b')).getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUnionStateTransition() {
        State<Integer> leaf3 = new HashMapState<Integer>() {
            @Override
            public String toString() {
                return "state3";
            }
        };
        State<Integer> leaf4 = new HashMapState<Integer>() {
            @Override
            public String toString() {
                return "state4";
            }
        };
        State<Integer> state1 = state(transitionBind('a', leafBind('a', 1),
                transitionBind('b', leaf3)));
        State<Integer> state2 = state(transitionBind('a', leafBind('a', 2),
                transitionBind('c', leaf4)));
        
        State<Integer> union12 = new UnionState<Integer>(state1, state2);
        State<Integer> union21 = new UnionState<Integer>(state2, state1);
        
        assertEquals((Integer) 1, union12.press(key('a')).getNextState().press(key('a'))
                .getValue());
        assertEquals((Integer) 2, union21.press(key('a')).getNextState().press(key('a'))
                .getValue());
        assertSame(leaf3, union12.press(key('a')).getNextState()
                .press(key('b')).getNextState());
        assertSame(leaf4, union12.press(key('a')).getNextState()
                .press(key('c')).getNextState());
    }

    @Test
    public void testParsingKeyStrokes() {
        assertEquals(asList(key('a')), parseKeyStrokes("a"));
        assertEquals(asList(key('a'), key('b')), parseKeyStrokes("ab"));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void testWrapperState() {
        Function<Integer, Integer> subTwo = new Function<Integer, Integer>() {
            public Integer call(Integer arg) { return arg - 2; }
            @Override public String toString() { return "subTwo()"; }
        };
        Function<Integer, Integer> addTwo = new Function<Integer, Integer>() {
            public Integer call(Integer arg) { return arg + 2; }
            @Override public String toString() { return "addTwo()"; }
        };
        State<Integer> state42 = state(transitionBind('4', 4, state(leafBind('2', 42))));
        State<Integer> state9 = state(leafBind('9', 9));
        State<Function<Integer, Integer>> stateOperators = state(
                leafBind('+', addTwo),
                transitionBind('-', subTwo, state(leafBind('-', addTwo))));
        State<Integer> wrapped42 = new WrappingState<Integer>(stateOperators, state42);
        State<Integer> wrapped9 = new WrappingState<Integer>(stateOperators, state9);
        State<Integer> unionState = union(wrapped42, wrapped9);
        
        assertEquals((Integer) 4, getValue(wrapped42, "4"));
        assertEquals((Integer) 6, getValue(wrapped42, "+4"));
        assertEquals((Integer) 2, getValue(wrapped42, "-4"));
        assertEquals((Integer) 6, getValue(wrapped42, "--4"));
        
        assertEquals((Integer) 42, getValue(wrapped42, "42"));
        assertEquals((Integer) 44, getValue(wrapped42, "+42"));
        assertEquals((Integer) 40, getValue(wrapped42, "-42"));
        assertEquals((Integer) 44, getValue(wrapped42, "--42"));
        
        assertEquals((Integer) 11, getValue(unionState, "--9"));
        
        assertNull(wrapped42.press(key('5')));
        assertNull(wrapped42.press(key('+')).getNextState().press(key('5')));
        assertNull(wrapped42.press(key('-')).getNextState().press(key('5')));
        assertNull(wrapped42.press(key('-')).getNextState().press(key('-')).getNextState().press(key('5')));
    }
    
    static<T> T getValue(State<T> state, String keys) {
        return goThrough(state, keys).getValue();
    }
    
    static<T> Transition<T> goThrough(State<T> state, String keys) {
        return goThrough(state, parseKeyStrokes(keys));
    }
    
    static<T> Transition<T> goThrough(State<T> state, Iterable<KeyStroke> keys) {
        Transition<T> transition = null;
        for (KeyStroke key: keys) {
//            System.out.println(key);
            transition = state.press(key);
//            System.out.println(transition);
            state = transition.getNextState();
//            System.out.println(state);
//            System.out.println();
        }
        return transition;
    }

//    private static<T> void assertReturnsValue(T expected, State<T> state, String keys) {
//        int last = keys.length() - 1;
//        for (int i = 0; i < last; i++) {
//            state = state.press(key(keys.charAt(i))).getNextState();
//        }
//        assertEquals(expected, state.press(key(keys.charAt(last))).getValue());
//    }

}
