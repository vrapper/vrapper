package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.PositionlessSelection;

public interface VisualOperatorCommand extends Command {
    public Command withPositionlessSelection(PositionlessSelection selection);
}
