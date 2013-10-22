package net.sourceforge.vrapper.eclipse.interceptor;

import net.sourceforge.vrapper.eclipse.activator.VrapperPlugin;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.modes.InsertMode;
import net.sourceforge.vrapper.vim.modes.NormalMode;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.swt.widgets.Display;

public class LinkedModeHandler implements IDocumentListener, ILinkedModeListener {

    private final EditorAdaptor hintReceiver;

    public LinkedModeHandler(final EditorAdaptor editorAdaptor) {
        this.hintReceiver = editorAdaptor;
    }

    public void onCheckForLinkedMode(final IDocument document) {
    	
        Display current = Display.getCurrent();
        if(current != null) {
        	// make sure to check AFTER the current UI stuff is done
        	//  (sometimes eclipse is weird)
        	current.asyncExec(new Runnable() {
        		@Override
        		public void run() {

        			checkInternal(document);
        		}
        	});
        }
    }
    
    protected void checkInternal(final IDocument document) {
        
        final LinkedModeModel model = LinkedModeModel.getModel(document, 0);
        if (model != null) {
            model.removeLinkingListener(this); // just in case, don't be a dup
            model.addLinkingListener(this);
            
            //Insert or normal mode is fine, but we don't want to get stuck in
            //visual or command line mode. In those other cases, switch to normal mode.
            String mode = hintReceiver.getCurrentModeName();
            if (VrapperPlugin.isVrapperEnabled()
                    && ! InsertMode.NAME.equals(mode)
                    && ! NormalMode.NAME.equals(mode)) {
                hintReceiver.changeModeSafely(NormalMode.NAME);
            }
        }
    }

    @Override
    public void left(final LinkedModeModel model, final int flags) {
        // left linked mode! We should now be in Normal mode,
        //  depending on the exit flag
        if (VrapperPlugin.isVrapperEnabled()
                && (flags & ILinkedModeListener.EXIT_ALL) != 0) {
            hintReceiver.changeModeSafely(NormalMode.NAME);
        }
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
    
    public static void registerListener(final IDocument document, final LinkedModeHandler handler) {
        document.addDocumentListener(handler);
    }

    public static void unregisterListener(final IDocument document, final LinkedModeHandler handler) {
        document.removeDocumentListener(handler);
    }

}
