package net.sourceforge.vrapper.plugin.exchange.commands;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.vrapper.platform.HighlightingService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SimpleTextOperation;
import net.sourceforge.vrapper.vim.commands.TextOperation;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Exchanges two text objects. First invocation highlights a text object to
 * be exchanged with a text object used for the second invocation.
 */
public class ExchangeOperation extends SimpleTextOperation {

    private static final String ANNOTATION_TYPE = "net.sourceforge.vrapper.eclipse.exchangeregion";

    public static final ExchangeOperation INSTANCE = new ExchangeOperation();

    // Highlighted exchange region for each editor.
    private final Map<EditorAdaptor, Object> annotations = new HashMap<EditorAdaptor, Object>();

    public void clear(EditorAdaptor editorAdaptor) {
        if (annotations.containsKey(editorAdaptor))  {
            final Object annotation = annotations.get(editorAdaptor);
            assert annotation != null;
            editorAdaptor.getHighlightingService().removeHighlighting(annotation);
            annotations.remove(editorAdaptor);
        }
    }

    public TextOperation repetition() {
        return this;
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor, final TextRange region,
            final ContentType contentType) throws CommandExecutionException {
        final HighlightingService highlightingService = editorAdaptor.getHighlightingService();
        // Check if the editor has an exchange region highlighted.
        if (annotations.containsKey(editorAdaptor))  {
            final TextRange highlightedRegion = highlightingService
                    .getHighlightedRegion(annotations.get(editorAdaptor));
            clear(editorAdaptor);
            if (highlightedRegion == null || highlightedRegion.getModelLength() == 0) {
                // Exchange region was deleted before the exchange completed,
                // ignore previous exchange highlighting.
                highlight(editorAdaptor, region);
            } else {
                editorAdaptor.getHistory().beginCompoundChange();
                try {
                    editorAdaptor.setPosition(region.getLeftBound(), StickyColumnPolicy.ON_CHANGE);
                    exchange(editorAdaptor.getModelContent(), region, highlightedRegion);
                } finally {
                    editorAdaptor.getHistory().endCompoundChange();
                }
            }
        } else {
            highlight(editorAdaptor, region);
        }
    }

    private void exchange(final TextContent content, TextRange a, TextRange b) throws CommandExecutionException {
        // Make sure region a comes before region b.
        if (b.getLeftBound().getModelOffset() < a.getLeftBound().getModelOffset()) {
            final TextRange t = a; a = b; b = t; // swap a <-> b;
        }
        // Check region intersection
        if (a.getRightBound().getModelOffset() > b.getLeftBound().getModelOffset()) {
            throw new CommandExecutionException("Exchange aborted: overlapping text");
        }
        final String textA = content.getText(a);
        final String textB = content.getText(b);
        // Replace b with a first to ensure a's offsets don't change
        content.replace(b.getLeftBound().getModelOffset(), b.getModelLength(), textA);
        content.replace(a.getLeftBound().getModelOffset(), a.getModelLength(), textB);
    }

    private void highlight(final EditorAdaptor editorAdaptor, final TextRange region) {
        final HighlightingService highlightingService = editorAdaptor.getHighlightingService();
        final Object annotation = highlightingService.highlightRegion(ANNOTATION_TYPE,
                "Vrapper Exchange Region", region);
        if (annotation != null) {
            annotations.put(editorAdaptor, annotation);
        }
    }
}