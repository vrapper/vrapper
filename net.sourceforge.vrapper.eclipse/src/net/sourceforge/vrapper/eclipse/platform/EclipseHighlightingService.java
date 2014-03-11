package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class EclipseHighlightingService implements HighlightingService {

    private final AbstractTextEditor editor;
    private final CursorService cursorService;

    EclipseHighlightingService(AbstractTextEditor editor, CursorService cursorService) {
        this.editor = editor;
        this.cursorService = cursorService;
    }

    @Override
    public Object highlightRegion(final String type, final String name, final TextRange region) {
        return this.highlightRegion(type, name, region.getLeftBound().getModelOffset(), region.getModelLength());
    }

    @Override
    public Object highlightRegion(String type, String name, int offset, int length) {
        final IAnnotationModel am = getAnnotationModel();
        if (am != null) {
            final Annotation annotation = new Annotation(type, false, name);
            am.addAnnotation(annotation, new org.eclipse.jface.text.Position(offset, length));
            return annotation;
        }
        return null;
    }

    @Override
    public TextRange getHighlightedRegion(Object annotationHandle) {
        final Annotation annotation = (Annotation) annotationHandle;
        final IAnnotationModel am = getAnnotationModel();
        if (am != null && annotation != null) {
            final org.eclipse.jface.text.Position position = am.getPosition(annotation);
            if (position != null) {
                return new StartEndTextRange(
                        cursorService.newPositionForModelOffset(position.getOffset()),
                        cursorService.newPositionForModelOffset(position.getOffset() + position.getLength()));
            }
        }
        return null;
    }

    @Override
    public void removeHighlighting(Object annotationHandle) {
        final Annotation annotation = (Annotation) annotationHandle;
        final IAnnotationModel am = getAnnotationModel();
        if (am != null && annotation != null) {
            am.removeAnnotation(annotation);
        }
    }

    private IAnnotationModel getAnnotationModel() {
        IDocumentProvider doc = editor.getDocumentProvider();
        return doc != null ? doc.getAnnotationModel(editor.getEditorInput()) : null;
    }

}
