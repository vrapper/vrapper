package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.utils.SelectionArea;

public interface VisualOperatorCommand extends Command {
    public Command withPositionlessSelection(SelectionArea selection);
}
