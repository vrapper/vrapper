package kg.totality.core.modes;

import org.eclipse.swt.custom.VerifyKeyListener;

public class CommandLineMode implements EditorMode {


	public static final String NAME = "command mode";

	@Override
	public void enterMode() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public VerifyKeyListener getKeyListener() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void leaveMode() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("method not implemented");
	}

}
