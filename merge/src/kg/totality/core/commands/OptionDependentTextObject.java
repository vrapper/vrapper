package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.TotalityCorePlugin;
import kg.totality.core.commands.motions.Motion;
import kg.totality.core.utils.ContentType;
import newpackage.position.TextRange;

public class OptionDependentTextObject extends AbstractTextObject {

	private final TextObject onTrue;
	private final TextObject onFalse;
	private final String option;

	public OptionDependentTextObject(String option, Motion onTrue, Motion onFalse) {
		this.option = option;
		this.onTrue = new MotionTextObject(onTrue);
		this.onFalse = new MotionTextObject(onFalse);
	}

	public OptionDependentTextObject(String option, TextObject onTrue, TextObject onFalse) {
		this.option = option;
		this.onTrue = onTrue;
		this.onFalse = onFalse;
	}

	@Override
	public ContentType getContentType() {
		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
			return onTrue.getContentType();
		else
			return onFalse.getContentType();
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode) {
		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
			return onTrue.getRegion(editorMode);
		else
			return onFalse.getRegion(editorMode);
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode, int count) {
		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
			return onTrue.getRegion(editorMode, count);
		else
			return onFalse.getRegion(editorMode, count);
	}

}
