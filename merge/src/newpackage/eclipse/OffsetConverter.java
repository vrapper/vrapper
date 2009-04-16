package newpackage.eclipse;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension3;
import org.eclipse.jface.text.ITextViewerExtension5;

import de.jroene.vrapper.eclipse.VrapperPlugin;

@SuppressWarnings("deprecation") // ITextViewerExtension3 is used for legacy code, anyway
public class OffsetConverter {

	public static ITextViewerExtension5 create(ITextViewer textViewer) {
		if (textViewer instanceof ITextViewerExtension5)
			return (ITextViewerExtension5) textViewer;
		else if (textViewer instanceof ITextViewerExtension3)
			VrapperPlugin.error("TODO: we can implement ITextViewerExtension3 -> ITextViewerExtension5 wrapper!!!");
		return new DummyTextViewerExtension5();
	}

}
