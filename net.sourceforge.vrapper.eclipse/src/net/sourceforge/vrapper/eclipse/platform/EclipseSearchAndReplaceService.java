package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class EclipseSearchAndReplaceService implements SearchAndReplaceService {

    private final AbstractTextEditor editor;
    private final FindReplaceDocumentAdapter adapter;
    private Search lastHighlightedSearch;
    private List<Annotation> annotations;

    public EclipseSearchAndReplaceService(AbstractTextEditor editor, ITextViewer textViewer) {
        this.editor = editor;
        this.adapter = new FindReplaceDocumentAdapter(textViewer.getDocument());
        this.annotations = Collections.emptyList();
    }

    public SearchResult find(Search search, Position start) {
        try {
            IRegion result = find(search, start.getModelOffset());
            Position resultPosition = result != null ? resultPosition = start.setModelOffset(result.getOffset()) : null;
            return new SearchResult(resultPosition);
        } catch (BadLocationException e) {
            return new SearchResult(null);
        }
    }

    private IRegion find(Search search, int begin) throws BadLocationException {
        IRegion result = adapter.find(
                    begin, search.getKeyword(),
                    !search.isBackward(), search.isCaseSensitive(),
                    search.isWholeWord(), false);
        return result;
    }

    public void removeHighlighting() {
        lastHighlightedSearch = null;
        IAnnotationModel m = getAnnotationModel();
        if (m != null) {
            for (Annotation a : annotations) {
                m.removeAnnotation(a);
            }
        }
        annotations = Collections.emptyList();
    }


    public void highlight(Search search) {

        if (lastHighlightedSearch != null && lastHighlightedSearch.getKeyword().equals(search.getKeyword())
                && lastHighlightedSearch.isCaseSensitive() == search.isCaseSensitive()
                && lastHighlightedSearch.isWholeWord() == search.isWholeWord()) {
            return;
        }
        IAnnotationModel am = getAnnotationModel();
        if (am == null) {
            return;
        }
        removeHighlighting();
        if (search.isBackward()) {
            search = search.reverse();
        }
        IRegion result = new Region(0, 0);
        annotations = new ArrayList<Annotation>();
        lastHighlightedSearch = search;
        try {
            while ((result = find(search, result.getOffset()+result.getLength())) != null) {
                Annotation a = new Annotation("net.sourceforge.vrapper.eclipse.searchhighlight", false, "test");
                am.addAnnotation(a, new org.eclipse.jface.text.Position(result.getOffset(), result.getLength()));
                annotations.add(a);
            }
        } catch (BadLocationException e) {
            VrapperLog.error("while highlighting search", e);
        }
    }
    private IAnnotationModel getAnnotationModel() {
        IDocumentProvider doc = editor.getDocumentProvider();
        return doc != null ? doc.getAnnotationModel(editor.getEditorInput()) : null;
    }

}
