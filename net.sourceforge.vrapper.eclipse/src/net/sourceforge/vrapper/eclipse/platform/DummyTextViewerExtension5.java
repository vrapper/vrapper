package net.sourceforge.vrapper.eclipse.platform;

import net.sourceforge.vrapper.log.VrapperLog;

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
        VrapperLog.error("dummy line conversion");
        return modelLine;
    }

    public int modelOffset2WidgetOffset(int modelOffset) {
        VrapperLog.error("dummy offset conversion");
        return modelOffset;
    }

    public IRegion modelRange2WidgetRange(IRegion modelRange) {
        VrapperLog.error("dummy range conversion");
        return modelRange;
    }

    public int widgetLine2ModelLine(int widgetLine) {
        VrapperLog.error("dummy line conversion");
        return widgetLine;
    }

    public int widgetLineOfWidgetOffset(int widgetOffset) {
        VrapperLog.error("dummy offset conversion");
        return widgetOffset;
    }

    public int widgetOffset2ModelOffset(int widgetOffset) {
        VrapperLog.error("dummy offset conversion");
        return widgetOffset;
    }

    public IRegion widgetRange2ModelRange(IRegion widgetRange) {
        VrapperLog.error("dummy region conversion");
        return widgetRange;
    }

    public int widgetlLine2ModelLine(int widgetLine) {
        VrapperLog.error("dummy line conversion");
        return widgetLine;
    }

}
