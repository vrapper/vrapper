package net.sourceforge.vrapper.vim;

import static net.sourceforge.vrapper.platform.Configuration.Option.bool;
import static net.sourceforge.vrapper.utils.VimUtils.set;

import java.util.Set;

import net.sourceforge.vrapper.platform.Configuration.Option;

public interface Options {

    public static final Option<Boolean> SMART_INDENT      = bool("smartindent",     true);
    public static final Option<Boolean> AUTO_INDENT       = bool("autoindent",      false);
    public static final Option<Boolean> ATOMIC_INSERT     = bool("atomicinsert",    true,  "ati");
    public static final Option<Boolean> IGNORE_CASE       = bool("ignorecase",      false);
    public static final Option<Boolean> SMART_CASE        = bool("smartcase",       false);
    @SuppressWarnings("unchecked")
    public static final Set<Option<Boolean>> BOOLEAN_OPTIONS = set(SMART_INDENT, AUTO_INDENT, ATOMIC_INSERT, IGNORE_CASE, SMART_CASE);

}
