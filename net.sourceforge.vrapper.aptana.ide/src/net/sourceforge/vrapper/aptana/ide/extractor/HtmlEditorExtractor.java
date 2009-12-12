package net.sourceforge.vrapper.aptana.ide.extractor;

import java.util.Collection;
import java.util.Collections;

import net.sourceforge.vrapper.eclipse.extractor.EditorExtractor;
import net.sourceforge.vrapper.log.VrapperLog;

import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import com.aptana.ide.editor.html.HTMLEditor;

public class HtmlEditorExtractor implements EditorExtractor {

    public Collection<AbstractTextEditor> extractATEs(IWorkbenchPart part) {
        if (part instanceof HTMLEditor) {
            HTMLEditor htmlEditor = (HTMLEditor) part;
            if (htmlEditor.getEditor() != null)
                return Collections.singleton((AbstractTextEditor) htmlEditor.getEditor());
            else
                VrapperLog.error("WTF?!?!");
        }
        return Collections.emptySet();
    }

}
