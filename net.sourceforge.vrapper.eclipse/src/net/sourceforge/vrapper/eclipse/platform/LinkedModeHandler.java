package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.vim.ModeChangeHintReceiver;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;

public class LinkedModeHandler implements IDocumentListener, ILinkedModeListener {

    private final ModeChangeHintReceiver hintReceiver;

    public LinkedModeHandler(final ModeChangeHintReceiver hintReceiver) {
        this.hintReceiver = hintReceiver;
    }

    public void onCheckForLinkedMode(final IDocument document) {
        new Thread() {
            @Override
            public void run() {
                
                // make sure to check AFTER the
                //  current UI stuff is done
                //  (sometimes eclipse is weird)
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException e) { }
                
                checkInternal(document);
            }
        }.start();
    }
    
    protected void checkInternal(final IDocument document) {
        
        final LinkedModeModel model = LinkedModeModel.getModel(document, 0);
        if (model != null) {
            model.removeLinkingListener(LinkedModeHandler.this); // just in case, don't be a dup
            model.addLinkingListener(LinkedModeHandler.this);
        }
    }

    @Override
    public void left(final LinkedModeModel model, final int flags) {
        // left linked mode! We should now be in Normal mode
        hintReceiver.changeModeSafely(NormalMode.NAME);
    }

    @Override
    public void suspend(final LinkedModeModel model) {
    }

    @Override
    public void resume(final LinkedModeModel model, final int flags) {
    }

    @Override
    public void documentAboutToBeChanged(final DocumentEvent event) {
    }

    @Override
    public void documentChanged(final DocumentEvent event) {
        onCheckForLinkedMode(event.fDocument);
    }
    
    public void registerListener(final IDocument document) {
        document.addDocumentListener(this);
    }

    public void unregisterListener(final IDocument document) {
        document.removeDocumentListener(this);
    }

}
