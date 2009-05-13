package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Search;
import net.sourceforge.vrapper.utils.SearchResult;
import net.sourceforge.vrapper.utils.Space;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;

@SuppressWarnings("nls")
public class EclipseTextContent {

    protected final ITextViewer textViewer;
    protected ITextViewerExtension5 converter;
    protected TextContent modelSide;
    protected TextContent viewSide;
    protected int horizontalPosition;
    protected boolean lineWiseMouseSelection;

    public EclipseTextContent(ITextViewer textViewer) {
        this.textViewer = textViewer;
        this.converter = OffsetConverter.create(textViewer);
        modelSide = new ModelSideTextContent();
        viewSide = new ViewSideTextContent();
    }

    public TextContent getModelContent() {
        return modelSide;
    }

    public TextContent getViewContent() {
        return viewSide;
    }

    protected class ModelSideTextContent implements TextContent {

        public LineInformation getLineInformation(int line) {
            try {
                IRegion region = textViewer.getDocument().getLineInformation(
                        line);
                return new LineInformation(line, region.getOffset(), region
                        .getLength());
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }

        }

        public LineInformation getLineInformationOfOffset(int offset) {
            int line;
            try {
                line = textViewer.getDocument().getLineOfOffset(offset);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            return getLineInformation(line);
        }

        public int getNumberOfLines() {
            return textViewer.getDocument().getNumberOfLines();
        }

        public int getTextLength() {
            return textViewer.getDocument().getLength();
        }

        public String getText(int index, int length) {
            try {
                return textViewer.getDocument().get(index, length);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        public void replace(int index, int length, String s) {
            try {
                IDocument doc = textViewer.getDocument();
                if (index > doc.getLength()) {
                    index = doc.getLength();
                }
                doc.replace(index, length, s);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
        }

        public Space getSpace() {
            return Space.MODEL;
        }

        public SearchResult find(Search search, Position start) {
            try {
                // TODO: find non-deprecated API
                int result = textViewer.getDocument().search(
                            start.getModelOffset(), search.getKeyword(),
                            !search.isBackward(), true, search.isWholeWord());
                Position resultPosition = result >= 0 ? start.setModelOffset(result) : null;
                return new SearchResult(resultPosition);
            } catch (BadLocationException e) {
                return new SearchResult(null);
            }
        }

    }

    protected class ViewSideTextContent implements TextContent  {

        public LineInformation getLineInformation(int line) {
            line = converter.widgetLine2ModelLine(line);
            IRegion region;
            try {
                region = textViewer.getDocument().getLineInformation(line);
            } catch (BadLocationException e) {
                throw new RuntimeException(e);
            }
            return new LineInformation(converter.modelLine2WidgetLine(line),
                    converter.modelOffset2WidgetOffset(region.getOffset()), region.getLength());
        }

        public LineInformation getLineInformationOfOffset(int offset) {
            int line = textViewer.getTextWidget().getLineAtOffset(offset);
            return getLineInformation(line);
        }

        public int getNumberOfLines() {
            return textViewer.getTextWidget().getLineCount();
        }

        public int getTextLength() {
            return textViewer.getTextWidget().getCharCount();
        }

        public String getText(int index, int length) {
            return textViewer.getTextWidget().getText(index, index + length - 1);
        }

        public void replace(int index, int length, String text) {
            // XXX: it was illegal in Vrapper. Why?
            textViewer.getTextWidget().replaceTextRange(index, length, text);
        }

        public Space getSpace() {
            return Space.VIEW;
        }

        public SearchResult find(Search search, Position start) {
            return modelSide.find(search, start);
        }
    }
}
