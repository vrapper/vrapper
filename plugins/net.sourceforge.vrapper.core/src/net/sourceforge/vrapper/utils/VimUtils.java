package net.sourceforge.vrapper.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.keymap.KeyStroke;
import net.sourceforge.vrapper.keymap.KeyStroke.Modifier;
import net.sourceforge.vrapper.keymap.vim.SimpleKeyStroke;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.Options;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.Utils;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;


/**
 * Commonly used methods.
 *
 * @author Matthias Radig
 */
// FIXME: this is just dumb port; some of those utils may be not needed any more, we may move some others elsewhere, etc.
public class VimUtils {

    public static final Pattern COMPILED_PATTERN_DELIM_PATTERN = Pattern.compile(VimConstants.PATTERN_DELIM_PATTERN);

    /**
     * This static variable shouldn't be permanently used, it's only here to
     * have a trigger value for conditional breakpoints.
     * Make sure to remove any references from testcases before committing!
     * 
     * <p>To use this, place a conditional breakpoint on the relevant code. Let
     * it fire "On value change" and use "VimUtils.BREAKPOINT_TRIGGER" as
     * expression. The breakpoint will always fire once on startup (if you want
     * to have more control, change it into a value test).
     * 
     * <p>Triggering your breakpoint another time should then be easy, just
     * put the following code in your test case:
     * <pre>VimUtils.BREAKPOINT_TRIGGER++;</pre>
     */
    public static int BREAKPOINT_TRIGGER = 0;

    private VimUtils() {
        // no instance
    }

    /**
     * @param s
     *            a string (of length 1).
     * @return whether s contains a single whitespace character.
     */
    public static boolean isWhiteSpace(final String s) {
        return VimConstants.WHITESPACE.contains(s);
    }

    /**
     * @param line
     *            a line in the text.
     * @return the offset where the first non-whitespace character occurs in the given line.
     */
    public static int getFirstNonWhiteSpaceOffset(final TextContent content, final LineInformation line) {
        int index = line.getBeginOffset();
        final int end = line.getEndOffset();
        while (index < end) {
            final String s = content.getText(index, 1);
            if (!isWhiteSpace(s)) {
                break;
            }
            index += 1;
        }
        return index;
    }

    /**
     * @param line
     *            a line in the text.
     * @return the offset of the last non-whitespace character.
     */
    public static int getLastNonWhiteSpaceOffset(final TextContent content, final LineInformation line) {
        if (line.getLength() == 0) {
            return line.getBeginOffset();
        }
        int index = line.getEndOffset() - 1;
        final int begin = line.getBeginOffset();
        while (index > begin) {
            final String s = content.getText(index, 1);
            if (!isWhiteSpace(s)) {
                break;
            }
            index--;
        }
        return index;
    }

    /**
     * @param vim
     *            the vim emulator.
     * @param line
     *            a line in the text.
     * @return the whitespace at the begin of the given line.
     */
    public static String getIndent(final TextContent content, final LineInformation line) {
        final int offset = getFirstNonWhiteSpaceOffset(content, line);
        return content.getText(line.getBeginOffset(), offset - line.getBeginOffset());
    }

    /**
     * @param content textContent
     * @param line
     *            a line in the text.
     * @return the content of the given line, without preceeding whitespace.
     */
    public static String getWithoutIndent(final TextContent content, final LineInformation info) {
        final int offset = getFirstNonWhiteSpaceOffset(content, info);
        return content.getText(offset, info.getEndOffset() - offset);
    }
    
    /**
     * Grab the word currently under the cursor.
     * If wholeWord, end at nearest whitespace.
     * Otherwise, use Options.KEYWORDS values.
     */
    public static String getWordUnderCursor(final EditorAdaptor editorAdaptor, final boolean wholeWord) {
        String word = "";
        TextContent p = editorAdaptor.getViewContent();
        int index = editorAdaptor.getCursorService().getPosition().getViewOffset();
        LineInformation line = p.getLineInformationOfOffset(index);
        int min = line.getBeginOffset();
        int max = line.getEndOffset();
        int first = -1;
        int last = -1;
        String s;
        boolean found = false;
        String keywords = wholeWord ? "\\S" : editorAdaptor.getConfiguration().get(Options.KEYWORDS);

        if (index < max) {
            s = p.getText(index, 1);
            if (Utils.characterType(s.charAt(0), keywords) == Utils.WORD) {
                found = true;
                first = index;
                last = index;
            }
        }
        while (index < max-1) {
            index += 1;
            s = p.getText(index, 1);
            if(Utils.characterType(s.charAt(0), keywords) == Utils.WORD) {
                last = index;
                if(!found) {
                    first = index;
                    found = true;
                }
            } else if(found) {
                break;
            }
        }
        if (found) {
            index = first;
            while (index > min) {
                index -= 1;
                s = p.getText(index, 1);
                if(Utils.characterType(s.charAt(0), keywords) == Utils.WORD) {
                    first = index;
                } else {
                    break;
                }
            }
            word = p.getText(first, last-first+1);
        }
        return word;
    }
    
    public static boolean containsNewLine(final String s) {
    	for(final String newline : VimConstants.NEWLINE) {
    		if(s.contains(newline)) {
    			return true;
    		}
    	}
    	return false;
    }

    public static boolean isNewLine(final String s) {
        return VimConstants.NEWLINE.contains(s);
    }
    
    public static int startsWithNewLine(String s) {
        int nlLen = 0;
        // Find the longest new line prefix.
    	for(String newline : VimConstants.NEWLINE) {
    		if(s.startsWith(newline)) {
    			nlLen = Math.max(nlLen, newline.length());
    		}
    	}
    	return nlLen;
    }
    
    public static int endsWithNewLine(String s) {
        int nlLen = 0;
        // Find the longest new line suffix.
    	for(String newline : VimConstants.NEWLINE) {
    		if(s.endsWith(newline)) {
    			nlLen = Math.max(nlLen, newline.length());
    		}
    	}
    	return nlLen;
    }

    /**
     * Replaces all combinations of newline characters in a string with another string.
     */
    public static String replaceNewLines(String inputString, String replacement) {
        char[] input = inputString.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            if (input[i] == '\r' && i + 1 < input.length && input[i+1] == '\n') {
                sb.append(replacement);
                i++; // skip \n
            } else if (input[i] == '\r') {
                sb.append(replacement);
            } else if (input[i] == '\n') {
                sb.append(replacement);
            } else {
                sb.append(input[i]);
            }
        }
        return sb.toString();
    }

    public static boolean isPatternDelimiter(final String s) {
        return VimUtils.COMPILED_PATTERN_DELIM_PATTERN.matcher(s).find();
    }

    public static boolean isBlank(final String s) {
        return s == null || s.trim().equals("");
    }
    
    /**
     * @return true, if line contains only whitespace characters
     */
    public static boolean isLineBlank(final TextContent content, final int lineNo) {
        final LineInformation line = content.getLineInformation(lineNo);
        return VimUtils.isBlank(content.getText(line.getBeginOffset(), line.getLength()));
    }
    
    /**
     * @return true, if the last character in the text buffer is newline
     */
    public static boolean endsWithEOL(final EditorAdaptor editor) {
        final TextContent content = editor.getModelContent();
        final LineInformation line = content.getLineInformation(content.getNumberOfLines() - 1);
        return line.getNumber() > 0 && line.getLength() == 0;
    }
    
    public static int calculateColForPosition(final TextContent p, final Position position) {
        return calculateColForOffset(p, position.getModelOffset());
    }
    
    public static int calculateColForOffset(final TextContent p, final int modelOffset) {
        final LineInformation line = p.getLineInformationOfOffset(modelOffset);
        return modelOffset - line.getBeginOffset();
    }
    
    public static int calculateLine(final TextContent text, final int offset) {
        final LineInformation line = text.getLineInformationOfOffset(offset);
        return line.getNumber();
    }
    
    public static int calculateLine(final TextContent text, final Position position) {
        return calculateLine(text, position.getModelOffset());
    }


    /**
     * Calculates an offset position. Line breaks are not counted.
     * @param position TODO
     */
    public static int calculatePositionForOffset(final TextContent p, int position, final int offset) {
        LineInformation line = p.getLineInformationOfOffset(position);
        if (offset < 0) {
            int i = -offset;
            while (i > 0) {
                if(position > line.getBeginOffset()) {
                    position -=1;
                } else {
                    final int nextLine = line.getNumber()-1;
                    if (nextLine < 0) {
                        break;
                    }
                    line = p.getLineInformation(nextLine);
                    position = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
                }
                i -= 1;
            }
        } else if (offset > 0) {
            int i = offset;
            int end = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
            while (i > 0) {
                if(position < end) {
                    position +=1;
                } else {
                    final int nextLine = line.getNumber()+1;
                    if (nextLine > p.getNumberOfLines()-1) {
                        break;
                    }
                    line = p.getLineInformation(nextLine);
                    end = Math.max(line.getBeginOffset(), line.getEndOffset()-1);
                    position = line.getBeginOffset();
                }
                i -= 1;
            }

        }
        return position;
    }

    public static String stripLastNewline(final String text) {
        if (text.endsWith(NewLine.WINDOWS.nl)) {
            return text.substring(0, text.length()-2);
        }
        if (text.endsWith(NewLine.UNIX.nl) || text.endsWith(NewLine.MAC.nl)) {
            return text.substring(0, text.length()-1);
        }
        return text;
    }

    @SuppressWarnings("unchecked")
	public static final <T> Set<T> set(final T... content) {
        return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(content)));
    }
    
    @SuppressWarnings("unchecked")
    public static final <T> List<T> list(final T... content) {
    	return Collections.unmodifiableList(new ArrayList<T>(Arrays.asList(content)));
    }

    public static SearchResult wrapAroundSearch(final EditorAdaptor vim, final Search search,
            final Position position) {
        final SearchAndReplaceService searcher = vim.getSearchAndReplaceService();
        SearchResult result = searcher.find(search, position);
        if (!result.isFound() && vim.getConfiguration().get(Options.WRAP_SCAN)) {
            // redo search from beginning / end of document
            final TextContent p = vim.getModelContent();
            final int index = search.isBackward() ? p.getLineInformation(p.getNumberOfLines()-1).getEndOffset()-1 : 0;
            result = searcher.find(search, position.setModelOffset(index));
        }
        return result;
    }

    /**
     * Similar to wrapAroundSearch but only searches within last selection
     */
    public static SearchResult wrapSelectionSearch(final EditorAdaptor vim, final Search search,
            Position position) {
        Selection selection = vim.getLastActiveSelection();
                
        //if the cursor is inside the selection, use it as our starting point
        //otherwise, use selection boundaries
        //(this is to ensure 'n' will actually go to the next match
        // rather than constantly starting at the same position)
        if(search.isBackward()) {
            position = selection.getRightBound().compareTo(position) > 0 ? position : selection.getRightBound();
        }
        else {
            position = selection.getLeftBound().compareTo(position) < 0 ? position : selection.getLeftBound();
        }
        
        SearchAndReplaceService searcher = vim.getSearchAndReplaceService();
        SearchResult result = searcher.find(search, position);
        if (result.isFound()) {
            if(result.getLeftBound().compareTo(selection.getLeftBound()) < 0 ||
                    result.getRightBound().compareTo(selection.getRightBound()) > 0) {
                //a match was found outside the selection, force a 'not found'
                result = new SearchResult(null, null);
            }
        }

        if (!result.isFound() && vim.getConfiguration().get(Options.WRAP_SCAN)) {
            //wrap to top/bottom of selection and try again
            Position newStart = search.isBackward() ? selection.getRightBound() : selection.getLeftBound();
            result = searcher.find(search, newStart);

            if (result.isFound()) {
                if(result.getLeftBound().compareTo(selection.getLeftBound()) < 0 ||
                        result.getRightBound().compareTo(selection.getRightBound()) > 0) {
                    //a match was found outside the selection, force a 'not found'
                    result = new SearchResult(null, null);
                }
            }
        }
        return result;
    }
    
    /**
     * Vim doesn't start a delimited range on a newline or end a range on an
     * empty line (try 'vi{' while within a function for proof).
     */
    public static Position fixLeftDelimiter(TextContent model, CursorService cursor, Position delim) {
        //check if the character after delimiter is a newline
        if(isNewLine(model.getText(delim.getModelOffset() + 1, 1))) {
            //start after newline
            LineInformation line = model.getLineInformationOfOffset(delim.getModelOffset());
            LineInformation nextLine = model.getLineInformation(line.getNumber() + 1);
            delim = cursor.newPositionForModelOffset(nextLine.getBeginOffset());
        }
        else {
            delim = delim.addModelOffset(1);
        }
        return delim;
    }
    public static Position fixRightDelimiter(TextContent model, CursorService cursor, Position delim) {
        int delimIndex = delim.getModelOffset();
        LineInformation line = model.getLineInformationOfOffset(delimIndex);
        int lineStart = line.getBeginOffset();
        
        if(delimIndex > lineStart) {
            //is everything before the delimiter just whitespace?
            String text = model.getText(lineStart, delimIndex - lineStart);
            if(VimUtils.isBlank(text)) {
                //end on previous line
                LineInformation previousLine = model.getLineInformation(line.getNumber() -1);
                delimIndex = previousLine.getEndOffset();
                delim = cursor.newPositionForModelOffset(delimIndex);
            }
        }
        return delim;
    }

    /**
     * Tries to work around AltGr madness if we detect that AltGr was pressed.
     * If you did press Ctrl + Alt or Alt and hit this function, then check the state provider.
     * This function should be called as a fallback when remaps and default bindings failed to find
     * a match.
     * <p>
     * Examples for different Operating Systems when AltGr + Q is pressed (@ key on German keybord):
     * <ul>
     * <li>Windows SWT sends Ctrl + Alt + @.</li>
     * <li>Mac OSX SWT sends Alt + @.</li>
     * <li>Linux SWT passes just @.</li>
     * </ul>
     * @return Either a {@link KeyStroke} without modifiers or <tt>null</tt> if not applicable.
     */
    public static KeyStroke fixAltGrKey(KeyStroke key) {
        //Most-common case, bail as fast as possible
        if( ! key.withAltKey()) {
            return null;
        }

        EnumSet<Modifier> modifiers = EnumSet.copyOf(key.getModifiers());
        modifiers.remove(Modifier.ALT);
        modifiers.remove(Modifier.CONTROL);

        // Turn off control and alt key bits.
        if (key.getSpecialKey() == null) {
            return new SimpleKeyStroke(key.getCharacter(), modifiers);
        } else {
            return new SimpleKeyStroke(key.getSpecialKey(), modifiers);
        }
    }
    
    /**
     * Adds a (possibly negative) offset to a Position, then checks that the position didn't run
     * into newline sequence or is past the last character.
     * Windows endlines (CR LF) are notorious as they will make Eclipse puke.
     * 
     * This function will also account for the end of the text and the start of the text.
     * 
     * @param adaptor {@link EditorAdaptor}
     * @param original {@link Position} starting position
     * @param delta {@link delta} how many characters to move. A CR LF newline will always count for
     *     two, whereas a CR newline only counts for one. The only special case is when the given
     *     delta makes us move into a CR LF sequence, in that case this function will move +/- 1
     *     character.
     * @param allowPastLastChar whether the newly returned position can be past the last character.
     *     All motions want to pass <tt>true</tt> so that a selection includes the last character,
     *     while low-level positioning logic can pass <tt>false</tt> to keep the cursor on the last
     *     character. If the line is empty, the first position of the line is returned.
     *
     */
    public static Position safeAddModelOffset(EditorAdaptor adaptor, Position original, int delta,
            boolean allowPastLastChar) {
        CursorService cursorService = adaptor.getCursorService();
        int originalPos = original.getModelOffset();
        return cursorService.shiftPositionForModelOffset(originalPos, delta, allowPastLastChar);
    }

    /**
     * Find a mode switch hint of a specific type. Note that it is more efficient to simply loop
     * over the arguments and write a tree of nested <code>if (x instanceof Y)</code> blocks when
     * one wants to find multiple types of hints.
     * @return the mode hint or <code>null</code>.
     */
    @SuppressWarnings("unchecked")
    public static <T> T findModeHint(Class<T> typeToFind, ModeSwitchHint...args) {
        T result = null;
        if (args.length >= 1 && typeToFind.isInstance(args[0])) {
            result = (T) args[0];
        }
        int i = 1;
        while (i < args.length && result == null) {
            if (typeToFind.isInstance(args[i])) {
                result = (T) args[i];
            }
            i++;
        }
        return result;
    }
}
