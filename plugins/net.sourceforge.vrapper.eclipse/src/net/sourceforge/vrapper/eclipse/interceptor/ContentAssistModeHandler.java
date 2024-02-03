package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.ContentAssistMode;
import net.sourceforge.vrapper.vim.modes.InsertMode;

import org.eclipse.jface.text.contentassist.ContentAssistEvent;
import org.eclipse.jface.text.contentassist.ICompletionListener;
import org.eclipse.jface.text.contentassist.ICompletionProposal;

public class ContentAssistModeHandler implements ICompletionListener {

    private final EditorAdaptor editorAdaptor;

    public ContentAssistModeHandler(final EditorAdaptor editorAdaptor) {
        this.editorAdaptor = editorAdaptor;
    }

    @Override
    public void assistSessionStarted(ContentAssistEvent event) {
        final String mode = editorAdaptor.getCurrentModeName();
        //
        // Only switch from INSERT mode.
        //
        if (VrapperPlugin.isVrapperEnabled() && InsertMode.NAME.equals(mode)) {
            editorAdaptor.changeModeSafely(ContentAssistMode.NAME,
            		InsertMode.DONT_MOVE_CURSOR, InsertMode.RESUME_ON_MODE_ENTER);
        }
    }

    @Override
    public void assistSessionEnded(ContentAssistEvent event) {
        final String mode = editorAdaptor.getCurrentModeName();
        if (VrapperPlugin.isVrapperEnabled() && ContentAssistMode.NAME.equals(mode)) {
            editorAdaptor.changeModeSafely(InsertMode.NAME,
            		InsertMode.DONT_MOVE_CURSOR, InsertMode.RESUME_ON_MODE_ENTER);
        }
    }

    @Override
    public void selectionChanged(ICompletionProposal proposal,
            boolean smartToggle) {
    }

}
