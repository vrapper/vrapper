package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.platform.VrapperPlatformException;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.TextRange;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.custom.StyledText;

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
                throw new VrapperPlatformException("Failed to get line info for ML" + line, e);
            }

        }

        public LineInformation getLineInformationOfOffset(int offset) {
            int line;
            try {
                line = textViewer.getDocument().getLineOfOffset(offset);
            } catch (BadLocationException e) {
                throw new VrapperPlatformException("Failed to get line info for M" + offset, e);
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
                throw new VrapperPlatformException("Failed to get text M" + index
                        + " (" + length + " chars)", e);
            }
        }

        public String getText(TextRange range) {
            return getText(range.getLeftBound().getModelOffset(), range.getModelLength());
        }

        public void replace(int index, int length, String s) {
            try {
                IDocument doc = textViewer.getDocument();
                if (index > doc.getLength()) {
                    index = doc.getLength();
                }
                doc.replace(index, length, s);
            } catch (BadLocationException e) {
                throw new VrapperPlatformException("Failed to replace for M" + index
                        + " (" + length + " chars)", e);
            }
        }

        public void smartInsert(int index, String s) {
            int offset = converter.modelOffset2WidgetOffset(index);
            // View might not have index exposed (it is in a fold or far away), check and correct.
            if (offset == -1) {
                boolean exposed = converter.exposeModelRange(new Region(index, 1));
                if ( ! exposed) {
                    throw new VrapperPlatformException("Failed to expose M " + index
                            + ", cannot operate on view for this index.");
                }
                offset = converter.modelOffset2WidgetOffset(index);
            }
            viewSide.smartInsert(offset, s);
        }

        public void smartInsert(String s) {
            viewSide.smartInsert(s);
        }

        public Space getSpace() {
            return Space.MODEL;
        }

    }

    protected class ViewSideTextContent implements TextContent  {

        public LineInformation getLineInformation(int line) {
            line = converter.widgetLine2ModelLine(line);
            IRegion region;
            try {
                region = textViewer.getDocument().getLineInformation(line);
            } catch (BadLocationException e) {
                throw new VrapperPlatformException("Failed to get line info for VL" + line, e);
            }
            return new LineInformation(converter.modelLine2WidgetLine(line),
                    converter.modelOffset2WidgetOffset(region.getOffset()), region.getLength());
        }

        public LineInformation getLineInformationOfOffset(int offset) {
            int line;
            try {
                line = textViewer.getTextWidget().getLineAtOffset(offset);
            } catch (IllegalArgumentException e) {
                throw new VrapperPlatformException("Failed to get line info for V" + offset, e);
            }
            return getLineInformation(line);
        }

        public int getNumberOfLines() {
            return textViewer.getTextWidget().getLineCount();
        }

        public int getTextLength() {
            return textViewer.getTextWidget().getCharCount();
        }

        public String getText(int index, int length) {
            try {
                return textViewer.getTextWidget().getText(index, index + length - 1);
            } catch (IllegalArgumentException e) {
                throw new VrapperPlatformException("Failed to get text info for V" + index
                        + " (" + length + " chars)", e);
            }
        }

        public String getText(TextRange range) {
            return getText(range.getLeftBound().getViewOffset(), range.getViewLength());
        }

        public void replace(int index, int length, String text) {
            // XXX: it was illegal in Vrapper. Why?
            try {
                textViewer.getTextWidget().replaceTextRange(index, length, text);
            } catch (IllegalArgumentException e) {
                throw new VrapperPlatformException("Failed to replace for V" + index
                        + " (" + length + " chars)", e);
            }
        }

        public void smartInsert(int index, String s) {
            StyledText textWidget = textViewer.getTextWidget();
            int oldIndex = textWidget.getCaretOffset();
            Position oldposition = new Position(converter.widgetOffset2ModelOffset(oldIndex));
            try {
                try {
                    textViewer.getDocument().addPosition(oldposition);
                } catch (BadLocationException e) {
                    oldposition = null;
                    throw new VrapperPlatformException("Caret is at invalid pos V" + index, e);
                }
                try {
                    textWidget.setCaretOffset(index);
                } catch (IllegalArgumentException e) {
                    throw new VrapperPlatformException("Failed to move caret to V" + index, e);
                }
                textWidget.insert(s);
                try {
                    int newIndex = converter.modelOffset2WidgetOffset(oldposition.offset);
                    // Old position is automatically updated by JFace.
                    textWidget.setCaretOffset(newIndex);
                } catch (IllegalArgumentException e) {
                    throw new VrapperPlatformException("Failed to move caret to M" + oldposition.offset, e);
                }
            } finally {
                if (oldposition != null) {
                    textViewer.getDocument().removePosition(oldposition);
                }
            }
        }

        public void smartInsert(String s) {
            StyledText textWidget = textViewer.getTextWidget();
            int pos = textWidget.getCaretOffset();

            if (pos < textWidget.getCharCount()) {
                // Insert at current position and move caret after newly inserted text.
                // Doing a simple insert or replace may sometimes move the caret automatically when
                // in linked mode, at other times it might not. The other smartInsert method handles
                // both cases fine.
                smartInsert(pos, s);
            } else {
                // No need to work with position updaters, we know we can jump to the new end of the
                // file.
                try {
                    textWidget.replaceTextRange(pos, 0, s);
                    textWidget.setCaretOffset(textWidget.getCharCount());
                } catch (Exception e) {
                    throw new VrapperPlatformException("Failed to insert text at V"+pos+" (EOF)",e);
                }
            }
        }

        public Space getSpace() {
            return Space.VIEW;
        }
    }
}
