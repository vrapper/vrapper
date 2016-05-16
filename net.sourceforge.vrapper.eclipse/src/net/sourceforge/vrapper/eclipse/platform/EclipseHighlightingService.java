package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.TextRange;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
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
    @SuppressWarnings({"rawtypes", // IAnnotationModelExtension uses raw Map
        "unchecked"}) // Converting to raw map or putting is considered unsafe
    public List<Object> highlightRegions(final String type, final String name, final List<TextRange> regions) {
        List<Object> annotations = new ArrayList<Object>();
        final IAnnotationModel am = getAnnotationModel();
        if (am instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension ame = (IAnnotationModelExtension) am;
            Map temp = new LinkedHashMap(regions.size());
            for (TextRange region : regions) {
                Annotation annotation = new Annotation(type, false, name);
                int offset = region.getLeftBound().getModelOffset();
                int length = region.getModelLength();
                temp.put(annotation, new org.eclipse.jface.text.Position(offset, length));
            }
            ame.replaceAnnotations(null, temp);
            annotations.addAll(temp.keySet());
        } else if (am != null) {
            // Slower method
            for (TextRange region : regions) {
                annotations.add(highlightRegion(type, name, region));
            }
        }
        return annotations;
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
    public void removeHighlights(List<Object> annotationHandles) {
        final IAnnotationModel am = getAnnotationModel();
        if (am instanceof IAnnotationModelExtension) {
            IAnnotationModelExtension ame = (IAnnotationModelExtension) am;
            Annotation[] temp = annotationHandles.toArray(new Annotation[annotationHandles.size()]);
            ame.replaceAnnotations(temp, null);
        } else if (am != null) {
            //Slower method
            for (Object annotationHandle : annotationHandles) {
                final Annotation annotation = (Annotation) annotationHandle;
                if (annotation != null) {
                    am.removeAnnotation(annotation);
                }
            }
        }
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
