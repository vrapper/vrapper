package net.sourceforge.vrapper.eclipse.platform;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.UnderlyingEditorSettings;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.DefaultConfigProvider;
import net.sourceforge.vrapper.vim.Options;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;

/**
 * This class exposes a number of Eclipse editor functionalities to Vrapper. The operations for Core
 * are defined in UnderlyingEditorSettings whereas some configuration-related stuff is only defined
 * here.
 */
public class AbstractTextEditorSettings implements UnderlyingEditorSettings, DefaultConfigProvider {

    private final AbstractTextEditor abstractTextEditor;
    private ISourceViewer sourceViewer;

    public AbstractTextEditorSettings(AbstractTextEditor abstractTextEditor, ISourceViewer sourceViewer) {
        this.abstractTextEditor = abstractTextEditor;
        this.sourceViewer = sourceViewer;
    }

    public void setReplaceMode(boolean replace) {
        try {
            // AbstractTextEditor.enableOverwriteMode is broken - it works only for disabling overwrite mode %-/
            Method isInsertingMethod = AbstractTextEditor.class.getDeclaredMethod("isInInsertMode");
            isInsertingMethod.setAccessible(true);
            boolean isInserting = (Boolean) isInsertingMethod.invoke(abstractTextEditor);
            if (isInserting == replace) {
                Method toggleMethod = AbstractTextEditor.class.getDeclaredMethod("toggleOverwriteMode");
                toggleMethod.setAccessible(true);
                toggleMethod.invoke(abstractTextEditor);
            }
        } catch (Exception exception) {
            VrapperLog.error("error when enabling replace mode", exception);
        }
    }

    public void disableInputMethod() {
        //Reset IME (Input Method editor) so Japanese keyboards can use normal-mode's key-bindings
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        shell.setImeInputMode(SWT.NONE);
    }
    
    protected SourceViewerConfiguration getSourceViewerConfig() throws IOException {
        // org.eclipse.ui.texteditor.AbstractTextEditor.getSourceViewerConfiguration()
        try {
            Method me = AbstractTextEditor.class.getDeclaredMethod("getSourceViewerConfiguration");
            me.setAccessible(true);
            Object sourceConfig = me.invoke(abstractTextEditor);
            if (sourceConfig == null) {
                throw new IOException("getSourceViewerConfiguration returned null!");
            }
            return (SourceViewerConfiguration) sourceConfig;
        } catch (ClassCastException e) {
            throw new IOException("Failed to grab sourceviewer");
        } catch (SecurityException e) {
            throw new IOException("Failed to grab sourceviewer");
        } catch (NoSuchMethodException e) {
            throw new IOException("Failed to grab sourceviewer");
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to grab sourceviewer");
        } catch (IllegalAccessException e) {
            throw new IOException("Failed to grab sourceviewer");
        } catch (InvocationTargetException e) {
            throw new IOException("Failed to grab sourceviewer");
        }
    }

    @Override
    @SuppressWarnings("unchecked") // we're doing casting for the convenience of the caller.
    public <T> T getDefault(Option<T> option) {
        T result = null;
        try {
            if (Options.TAB_STOP.equals(option)) {
                SourceViewerConfiguration config = getSourceViewerConfig();
                Integer tabWidth = config.getTabWidth(sourceViewer);
                return (T) tabWidth;

            } else if (Options.SHIFT_WIDTH.equals(option)) {
                SourceViewerConfiguration config = getSourceViewerConfig();
                String[] indents = config.getIndentPrefixes(sourceViewer, IDocument.DEFAULT_CONTENT_TYPE);
                if (indents.length < 1) {
                    return null;
                }
                String indent = indents[0];
                int tabWidth = config.getTabWidth(sourceViewer);
                int[] offsets = StringUtils.calculateVisualOffsets(indent, indent.length(), tabWidth);
                Integer shiftWidth = offsets[indent.length()]; // offsets array size is +1 longer
                return (T) shiftWidth;

            } else if (Options.EXPAND_TAB.equals(option)) {
                SourceViewerConfiguration config = getSourceViewerConfig();
                String[] indents = config.getIndentPrefixes(sourceViewer, IDocument.DEFAULT_CONTENT_TYPE);
                if (indents.length < 1) {
                    return null;
                }
                String indent = indents[0];
                Boolean expandTab = ! indent.contains("\t");
                return (T) expandTab;
            }
        } catch (IOException e) {
            VrapperLog.debug("Failed to get source viewer: " + e);
        }
        return result;
    }
}
