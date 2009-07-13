package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.utils.AbstractPosition;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;

public class TextViewerPosition extends AbstractPosition {

    private final ITextViewerExtension5 converter;
    private final Space space;
    private final int offset;

    public TextViewerPosition(ITextViewer textViewer, Space space, int offset) {
        this.converter = OffsetConverter.create(textViewer);
        this.space = space;
        this.offset = offset;
    }

    public TextViewerPosition(ITextViewerExtension5 converter, Space space, int offset) {
        this.converter = converter;
        this.space = space;
        this.offset = offset;
    }

    public Position addModelOffset(int delta) {
        return new TextViewerPosition(converter, Space.MODEL, getModelOffset() + delta);
    }

    public Position addViewOffset(int delta) {
        return new TextViewerPosition(converter, Space.MODEL, getModelOffset() + delta);
    }

    public int getModelOffset() {
        switch (space) {
        case MODEL: return offset;
        case VIEW:  return converter.widgetOffset2ModelOffset(offset);
        default:
            throw new RuntimeException("bad space: " + space);
        }
    }

    public int getViewOffset() {
        switch (space) {
        case VIEW:  return offset;
        case MODEL: return converter.modelOffset2WidgetOffset(offset);
        default:
            throw new RuntimeException("bad space: " + space);
        }
    }

    public Position setModelOffset(int offset) {
        return new TextViewerPosition(converter, Space.MODEL, offset);
    }

    public Position setViewOffset(int offset) {
        return new TextViewerPosition(converter, Space.VIEW, offset);
    }

}
