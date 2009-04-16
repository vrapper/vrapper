package newpackage.eclipse;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewerExtension5;

import de.jroene.vrapper.eclipse.VrapperPlugin;

public class DummyTextViewerExtension5 implements ITextViewerExtension5 {

	@Override
	public boolean exposeModelRange(IRegion modelRange) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public IRegion[] getCoveredModelRanges(IRegion modelRange) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public IRegion getModelCoverage() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public int modelLine2WidgetLine(int modelLine) {
		VrapperPlugin.error("dummy line conversion");
		return modelLine;
	}

	@Override
	public int modelOffset2WidgetOffset(int modelOffset) {
		VrapperPlugin.error("dummy offset conversion");
		return modelOffset;
	}

	@Override
	public IRegion modelRange2WidgetRange(IRegion modelRange) {
		VrapperPlugin.error("dummy range conversion");
		return modelRange;
	}

	@Override
	public int widgetLine2ModelLine(int widgetLine) {
		VrapperPlugin.error("dummy line conversion");
		return widgetLine;
	}

	@Override
	public int widgetLineOfWidgetOffset(int widgetOffset) {
		VrapperPlugin.error("dummy offset conversion");
		return widgetOffset;
	}

	@Override
	public int widgetOffset2ModelOffset(int widgetOffset) {
		VrapperPlugin.error("dummy offset conversion");
		return widgetOffset;
	}

	@Override
	public IRegion widgetRange2ModelRange(IRegion widgetRange) {
		VrapperPlugin.error("dummy region conversion");
		return widgetRange;
	}

	@Override
	public int widgetlLine2ModelLine(int widgetLine) {
		VrapperPlugin.error("dummy line conversion");
		return widgetLine;
	}

}
