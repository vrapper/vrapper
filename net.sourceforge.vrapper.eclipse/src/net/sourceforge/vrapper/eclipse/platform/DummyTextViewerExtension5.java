package net.sourceforge.vrapper.eclipse.platform;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;

public class DummyTextViewerExtension5 implements ITextViewerExtension5 {

    public boolean exposeModelRange(IRegion modelRange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("method not implemented");
    }

    public IRegion[] getCoveredModelRanges(IRegion modelRange) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("method not implemented");
    }

    public IRegion getModelCoverage() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("method not implemented");
    }

    public int modelLine2WidgetLine(int modelLine) {
        return modelLine;
    }

    public int modelOffset2WidgetOffset(int modelOffset) {
        return modelOffset;
    }

    public IRegion modelRange2WidgetRange(IRegion modelRange) {
        return modelRange;
    }

    public int widgetLine2ModelLine(int widgetLine) {
        return widgetLine;
    }

    public int widgetLineOfWidgetOffset(int widgetOffset) {
        return widgetOffset;
    }

    public int widgetOffset2ModelOffset(int widgetOffset) {
        return widgetOffset;
    }

    public IRegion widgetRange2ModelRange(IRegion widgetRange) {
        return widgetRange;
    }

    public int widgetlLine2ModelLine(int widgetLine) {
        return widgetLine;
    }

}
