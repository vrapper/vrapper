package net.sourceforge.vrapper.plugin.splitEditor.commands;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountAwareCommand;

/**
 * Implements VIM's @c :tabnext and @c :tabprev within a split.
 */
public class SwitchTabCommand extends CountAwareCommand {
    final SwitchTabDirection direction;

    public final static SwitchTabCommand NEXT = new SwitchTabCommand(SwitchTabDirection.NEXT);
    public final static SwitchTabCommand PREVIOUS = new SwitchTabCommand(SwitchTabDirection.PREVIOUS);

    private SwitchTabCommand(SwitchTabDirection direction) {
        this.direction = direction;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final int count)
            throws CommandExecutionException {
        final IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                .getActivePage().getActivePart().getSite();
        final EPartService psvc = (EPartService) site.getService(EPartService.class);
        final MPart p = (MPart) site.getService(MPart.class);
        final MElementContainer<MUIElement> editorStack = p.getParent();
        final List<MUIElement> tabs = editorStack.getChildren();
        int newIdx;
        if (count == NO_COUNT_GIVEN) {
            //
            // Just switch to the next/previous tab
            //
            final MUIElement selectedTab = editorStack.getSelectedElement();
            final int selectedIdx = tabs.indexOf(selectedTab);
            if (direction == SwitchTabDirection.NEXT) {
                newIdx = (selectedIdx + 1) % tabs.size();
            } else {
                assert direction == SwitchTabDirection.PREVIOUS;
                if (selectedIdx == 0) {
                    newIdx = tabs.size() - 1;
                } else {
                    newIdx = selectedIdx - 1;
                }
            }
        } else {
            //
            // Switch to a particular tab specified by the count
            //
            if (count < 1 || count > tabs.size()) {
                // Invalid count, ignore
                return;
            } else {
                newIdx = count - 1;
            }
        }

        final MUIElement newTab = tabs.get(newIdx);
        if (newTab instanceof MPart) {
            psvc.activate((MPart) newTab, true);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

}
