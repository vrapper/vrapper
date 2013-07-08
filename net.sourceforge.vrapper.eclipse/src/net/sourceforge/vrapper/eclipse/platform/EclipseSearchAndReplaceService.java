package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.SearchAndReplaceService;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.Options;

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

    private static final String ANNOTATION_TYPE = "net.sourceforge.vrapper.eclipse.searchhighlight";
    private final AbstractTextEditor editor;
    private final FindReplaceDocumentAdapter adapter;
    final Configuration sharedConfiguration;
    private Search lastHighlightedSearch;
    private List<Annotation> annotations;
    private Annotation incSearchAnnotation;

    public EclipseSearchAndReplaceService(AbstractTextEditor editor, ITextViewer textViewer, final Configuration sharedConfiguration) {
        this.editor = editor;
        this.adapter = new FindReplaceDocumentAdapter(textViewer.getDocument());
        this.sharedConfiguration = sharedConfiguration;
        this.annotations = Collections.emptyList();
    }

    public SearchResult find(Search search, Position start) {
        try {
            IRegion result = find(search, start.getModelOffset());
            Position resultPosition = result != null ? start.setModelOffset(result.getOffset()) : null;
            Position endPosition = result != null ? start.setModelOffset(result.getOffset()+result.getLength()) : null;
            return new SearchResult(resultPosition, endPosition);
        } catch (BadLocationException e) {
            return new SearchResult(null, null);
        }
    }
    
    public int replace(LineInformation line, String toFind, String replace, String flags) {
    	int start = line.getBeginOffset();
    	int end = line.getEndOffset();
    	final boolean replaceAll = flags.contains("g");
        boolean caseSensitive = isCaseSensitive(toFind, flags);

    	//each time we perform a replace,
    	//how many characters will be added/removed?
    	int lengthDiff = replace.length() - toFind.length();
    	int match;
    	int numReplaces = 0;
    	try {
    		while(start < end) {
    			IRegion result = adapter.find(start, toFind, true, caseSensitive, false, true);
    			if(result != null && result.getOffset() < end) {
    				match = result.getOffset();
    				numReplaces++;
    				adapter.replace(replace, true);
    				if(replaceAll) {
    					//don't match on the replacement string
    					//when we come around again
    					// (s/foo/barfoo/g)
    					start = match + replace.length();
    					//the offset for the end of this line has changed
    					end += lengthDiff;
    				}
    				else {
    					//if not global, we've performed our one replace
    					break;
    				}
    			}
    			else {
    				//no match found
    				break;
    			}
    		}
        } catch (BadLocationException e) {
        	//should we log something?
        }
    	
    	return numReplaces;
    }

	private boolean isCaseSensitive(String toFind, String flags) {
		boolean caseSensitive = !sharedConfiguration.get(Options.IGNORE_CASE)
            || (sharedConfiguration.get(Options.SMART_CASE)
                && StringUtils.containsUppercase(toFind));
        if (flags.contains("i"))
            caseSensitive = false;
        if (flags.contains("I"))
            caseSensitive = true;
		return caseSensitive;
	}
    
    public boolean substitute(int start, String toFind, String flags, String toReplace) {
    	boolean success = false;
    	try {
			IRegion result = adapter.find(start, toFind, true, isCaseSensitive(toFind, flags), false, true);
			if(result != null) {
				adapter.replace(toReplace, true);
				success = true;
			}
		} catch (BadLocationException e) {
		}
    	return success;
    }

    private IRegion find(Search search, int begin) throws BadLocationException {
        IRegion result = adapter.find(
                    begin, search.getKeyword(),
                    !search.isBackward(), search.isCaseSensitive(),
                    search.isWholeWord(), search.isRegExSearch());
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
                Annotation a = new Annotation(ANNOTATION_TYPE, false, "Vrapper Search");
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

    public void incSearchhighlight(Position start, int length) {
        IAnnotationModel am = getAnnotationModel();
        if (am != null) {
            removeIncSearchHighlighting();
            incSearchAnnotation = new Annotation(ANNOTATION_TYPE, false, "Incremental Search");
            am.addAnnotation(incSearchAnnotation, new org.eclipse.jface.text.Position(start.getModelOffset(), length));
        }
    }

    public void removeIncSearchHighlighting() {
        IAnnotationModel am = getAnnotationModel();
        if (am != null && incSearchAnnotation != null) {
            am.removeAnnotation(incSearchAnnotation);
        }
    }

}
