package net.sourceforge.vrapper.vim;

import static net.sourceforge.vrapper.platform.Configuration.Option.bool;
import static net.sourceforge.vrapper.platform.Configuration.Option.string;
import static net.sourceforge.vrapper.utils.VimUtils.set;

import java.util.Set;

import net.sourceforge.vrapper.platform.Configuration.Option;

public interface Options {
    // Boolean options:
    public static final Option<Boolean> SMART_INDENT      = bool("smartindent",     true,  "si");
    public static final Option<Boolean> AUTO_INDENT       = bool("autoindent",      false, "ai");
    public static final Option<Boolean> ATOMIC_INSERT     = bool("atomicinsert",    true,  "ati");
    public static final Option<Boolean> IGNORE_CASE       = bool("ignorecase",      false, "ic");
    public static final Option<Boolean> SMART_CASE        = bool("smartcase",       false, "scs");
    public static final Option<Boolean> MOVE_ON_YANK      = bool("moveonyank",      true);
    public static final Option<Boolean> SANE_CW           = bool("sanecw",          false);
    public static final Option<Boolean> SANE_Y            = bool("saney",           false);
    public static final Option<Boolean> SEARCH_HIGHLIGHT  = bool("hlsearch",        true,  "hls");
    public static final Option<Boolean> SEARCH_REGEX      = bool("regexsearch",     false, "rxs");
    public static final Option<Boolean> INCREMENTAL_SEARCH = bool("incsearch",      false, "is");

    @SuppressWarnings("unchecked")
    public static final Set<Option<Boolean>> BOOLEAN_OPTIONS = set(
            SMART_INDENT, AUTO_INDENT, ATOMIC_INSERT, IGNORE_CASE, SMART_CASE,
            MOVE_ON_YANK, SANE_CW, SANE_Y, SEARCH_HIGHLIGHT, SEARCH_REGEX,
            INCREMENTAL_SEARCH);

    // String options:
    public static final Option<String> SELECTION = string("selection", "inclusive", "old, inclusive, exclusive", "sel");
    @SuppressWarnings("unchecked")
    public static final Set<Option<String>> STRING_OPTIONS = set(SELECTION);
}
