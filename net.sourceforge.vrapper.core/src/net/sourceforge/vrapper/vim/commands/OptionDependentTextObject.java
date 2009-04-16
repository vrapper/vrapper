package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.Motion;

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
		VrapperLog.info("OptionDependentTextObject.getContentType not implemented");
		return onFalse.getContentType();
//		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
//			return onTrue.getContentType();
//		else
//			return onFalse.getContentType();
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode) {
		VrapperLog.info("OptionDependentTextObject.getRegion not implemented");
		return onFalse.getRegion(editorMode);
//		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
//			return onTrue.getRegion(editorMode);
//		else
//			return onFalse.getRegion(editorMode);
	}

	@Override
	public TextRange getRegion(EditorAdaptor editorMode, int count) {
		VrapperLog.info("OptionDependentTextObject.getRegion not implemented");
		return onFalse.getRegion(editorMode, count);
//		if (TotalityCorePlugin.getDefault().getPreferenceStore().getBoolean(option))
//			return onTrue.getRegion(editorMode, count);
//		else
//			return onFalse.getRegion(editorMode, count);
	}

}
