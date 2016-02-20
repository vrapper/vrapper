package net.sourceforge.vrapper.vim;

import static net.sourceforge.vrapper.platform.Configuration.Option.*;
import static net.sourceforge.vrapper.utils.VimUtils.set;

import java.util.Set;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.modes.commandline.HighlightSearch;
import net.sourceforge.vrapper.vim.register.RegisterManager;

public interface Options {
    // Boolean options:
    public static final Option<Boolean> SMART_INDENT          = bool("smartindent",    true,  "si");
    public static final Option<Boolean> AUTO_INDENT           = bool("autoindent",     false, "ai");
    public static final Option<Boolean> ATOMIC_INSERT         = bool("atomicinsert",   true,  "ati");
    public static final Option<Boolean> IGNORE_CASE           = bool("ignorecase",     false, "ic");
    public static final Option<Boolean> FILE_IGNORE_CASE      = bool("fileignorecase", false, "fic");
    public static final Option<Boolean> SMART_CASE            = bool("smartcase",      false, "scs");
    public static final Option<Boolean> SEARCH_HIGHLIGHT= globalBool("hlsearch",       false, "hls");
    public static final Option<Boolean> SEARCH_REGEX          = bool("regexsearch",    true,  "rxs");
    public static final Option<Boolean> INCREMENTAL_SEARCH    = bool("incsearch",      false, "is");
    public static final Option<Boolean> IM_DISABLE            = bool("imdisable",      false, "imd");
    public static final Option<Boolean> VISUAL_MOUSE          = bool("visualmouse",    true,  "vm");
    public static final Option<Boolean> VISUAL_OTHER          = bool("visualother",  false, "vo");
    public static final Option<Boolean> EXIT_LINK_MODE        = bool("exitlinkmode",   true,  "elm");
    public static final Option<Boolean> CLEAN_INDENT          = bool("cleanindent",    true);
    public static final Option<Boolean> AUTO_CHDIR            = bool("autochdir",      false, "acd");
    // TODO: This is an Eclipse setting under Window->Preferences->Editors->Text Editors->"Insert spaces for tabs"
    //       Changing this value should change the Eclipse configuration too. -- BRD
    public static final Option<Boolean> EXPAND_TAB            = bool("expandtab",    true,  "et");
    public static final Option<Boolean> SHIFT_ROUND           = bool("shiftround",   false, "sr");
    public static final Option<Boolean> CONTENT_ASSIST_MODE   = bool("contentassistmode", false, "cam");
    public static final Option<Boolean> START_NORMAL_MODE     = bool("startnormalmode",   false, "snm");
    public static final Option<Boolean> UNDO_MOVES_CURSOR     = bool("undomovescursor",  true,  "umvc");
    public static final Option<Boolean> WRAP_SCAN             = bool("wrapscan",     true,  "ws");
    public static final Option<Boolean> DEBUGLOG        = globalBool("debuglog",     false);
    public static final Option<Boolean> LINE_NUMBERS    = globalBool("number",       false, "nu");
    public static final Option<Boolean> SHOW_WHITESPACE = globalBool("list",         false, "l");
    public static final Option<Boolean> SPELL           = globalBool("spell",        true);
    public static final Option<Boolean> HIGHLIGHT_CURSOR_LINE = globalBool("cursorline",   false, "cul");

    public static final Option<Boolean> MODIFIABLE       = localBool("modifiable", true, "ma");
    public static final Option<Boolean> GLOBAL_REGISTERS = localBool("globalregisters", true);

    @SuppressWarnings("unchecked")
    public static final Set<Option<Boolean>> BOOLEAN_OPTIONS = set(
            EXPAND_TAB, SHIFT_ROUND, SMART_INDENT, AUTO_INDENT, ATOMIC_INSERT, IGNORE_CASE,
            FILE_IGNORE_CASE, SMART_CASE, SEARCH_HIGHLIGHT, SEARCH_REGEX,
            INCREMENTAL_SEARCH, LINE_NUMBERS, SHOW_WHITESPACE, IM_DISABLE,
            VISUAL_MOUSE, VISUAL_OTHER, EXIT_LINK_MODE, CLEAN_INDENT, AUTO_CHDIR, HIGHLIGHT_CURSOR_LINE,
            CONTENT_ASSIST_MODE, START_NORMAL_MODE, UNDO_MOVES_CURSOR, DEBUGLOG, MODIFIABLE,
            GLOBAL_REGISTERS, WRAP_SCAN, SPELL);

    // String options:
    public static final Option<String> SYNC_MODIFIABLE = globalString("syncmodifiable", "nosync", "nosync, matchreadonly", "syncma");
    public static final Option<String> SEARCH_HL_SCOPE = globalString("hlscope", HighlightSearch.SEARCH_HL_SCOPE_CLEAR, HighlightSearch.SEARCH_HL_OPTIONS);
    public static final Option<String> SELECTION =             string("selection", Selection.INCLUSIVE, Selection.SELECTION_OPTIONS, "sel");
    public static final Option<String> PATH      = stringNoConstraint("path", ".", "pa");
    public static final Option<String> GVIM_PATH = stringNoConstraint("gvimpath", "/usr/bin/gvim", "gvp");
    public static final Option<String> GVIM_ARGS = stringNoConstraint("gvimargs", "");
    public static final Option<String> KEYWORDS  = stringNoConstraint("iskeyword", "a-zA-Z0-9_\u00C0-\u017F", "isk");

    // [NOTE] This is not added to the list of string options because it can't be changed using :set
    public static final Option<String> MAPLEADER = globalStringNoConstraint("mapleader", "\\");

    @SuppressWarnings("unchecked")
    public static final Set<Option<String>> STRING_OPTIONS = set(SELECTION, SEARCH_HL_SCOPE, PATH,
            GVIM_PATH, GVIM_ARGS, KEYWORDS, SYNC_MODIFIABLE);

    // String-set options:
    public static final Option<Set<String>> CLIPBOARD = globalStringSet("clipboard", "",
            RegisterManager.CLIPBOARD_VALUE_UNNAMED + ", "
                + RegisterManager.CLIPBOARD_VALUE_UNNAMEDPLUS + ", "
                + RegisterManager.CLIPBOARD_VALUE_AUTOSELECT + ", "
                + RegisterManager.CLIPBOARD_VALUE_AUTOSELECTPLUS + ", "
                + "exclude:", // Used by default in Vim on Linux, ignored by Vrapper.
            "cb");
    public static final Option<Set<String>> MATCHPAIRS = globalStringSet("matchpairs", "(:),{:},[:]", Pattern.compile(".:."), "mps");
    @SuppressWarnings("unchecked")
    public static final Set<Option<Set<String>>> STRINGSET_OPTIONS = set(CLIPBOARD, MATCHPAIRS);

    // Int options:
    public static final Option<Integer> SCROLL_OFFSET = integer("scrolloff",   0, "so");
    public static final Option<Integer> SCROLL        = integer("scroll",      0, "scr");
    public static final Option<Integer> SCROLL_JUMP   = integer("scrolljump",  1, "sj");
    public static final Option<Integer> TEXT_WIDTH    = integer("textwidth",  80, "tw");
    public static final Option<Integer> SOFT_TAB      = integer("softtabstop", 0, "sts");
    // TODO: This is an Eclipse setting under Window->Preferences->Editors->Text Editors->"Displayed tab width"
    //       Changing this value should change the Eclipse configuration too. -- BRD
    public static final Option<Integer> TAB_STOP      = integer("tabstop",     8, "ts");
    public static final Option<Integer> SHIFT_WIDTH   = integer("shiftwidth",  8, "sw");

    @SuppressWarnings("unchecked")
    public static final Set<Option<Integer>> INT_OPTIONS = set(SCROLL_JUMP, SCROLL, SCROLL_OFFSET, TEXT_WIDTH, SOFT_TAB, TAB_STOP, SHIFT_WIDTH);
}
