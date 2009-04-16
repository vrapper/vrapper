package kg.totality.core.modes;

import org.eclipse.swt.custom.VerifyKeyListener;

public interface EditorMode {
	String getName();
	void enterMode();
	void leaveMode();
	VerifyKeyListener getKeyListener();
}
