package net.sourceforge.vrapper.vim.commands.motions;

import net.sourceforge.vrapper.utils.Reversible;
import net.sourceforge.vrapper.vim.commands.Repeatable;

/**
 * Composite interface for those motions which are reversible. Instances of this class can be
 * assigned to the register manager for use with the keybinds like <code>;</code> and <code>,</code>
 * (done through plugs in any of the plugins).
 */
public interface NavigatingMotion extends Motion, Reversible<NavigatingMotion>, Repeatable<NavigatingMotion> {
}
