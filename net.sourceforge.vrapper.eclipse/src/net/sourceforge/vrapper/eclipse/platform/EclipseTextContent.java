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
import org.eclipse.swt.custom.StyledText;

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
            viewSide.smartInsert(converter.modelOffset2WidgetOffset(index), s);
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
            try {
                textWidget.setCaretOffset(index);
            } catch (IllegalArgumentException e) {
                throw new VrapperPlatformException("Failed to move caret to V" + index, e);
            }
            textWidget.insert(s);
            try {
                if (oldIndex > index) {
                    // Position got shifted due to our insert.
                    oldIndex = oldIndex + s.length();
                }
                textWidget.setCaretOffset(oldIndex);
            } catch (IllegalArgumentException e) {
                throw new VrapperPlatformException("Failed to move caret to V" + oldIndex, e);
            }
        }

        public void smartInsert(String s) {
            StyledText textWidget = textViewer.getTextWidget();
            int pos = textWidget.getCaretOffset();
            // move caret after insertion to preserve position
            if (pos < textWidget.getCharCount()) {
                // check that we're not at the end of the line, in which case we might run into \r\n
                int mOffset = converter.widgetOffset2ModelOffset(pos);
                int nextLineStart, lineStart, lineEnd;
                try {
                    int mLine = textViewer.getDocument().getLineOfOffset(mOffset);
                    int lineLenDelim = textViewer.getDocument().getLineLength(mLine);
                    IRegion lineInfo = textViewer.getDocument().getLineInformation(mLine);
                    lineStart = lineInfo.getOffset();
                    lineEnd = lineStart + lineInfo.getLength();
                    nextLineStart = lineStart + lineLenDelim;
                } catch (BadLocationException e) {
                    throw new VrapperPlatformException("Failed to get line info for M" + mOffset
                            + "/V" + pos, e);
                }
                try {
                    int jump = 1;
                    if (mOffset + jump > lineEnd) {
                        // park caret on next line so we don't jump into the middle of \r\n
                        jump = nextLineStart - mOffset;
                    }
                    textWidget.setCaretOffset(pos + jump);
                    textWidget.replaceTextRange(pos, 0, s);
                    textWidget.setCaretOffset(textWidget.getCaretOffset() - jump);
                } catch (IllegalArgumentException e) {
                    throw new VrapperPlatformException("Failed to insert text"
                            + " at M" + mOffset + "/V" + pos, e);
                }
            } else {
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
