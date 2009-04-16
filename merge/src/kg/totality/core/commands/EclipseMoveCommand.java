package kg.totality.core.commands;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.motions.CountAwareMotion;
import newpackage.position.Position;

public class EclipseMoveCommand extends CountAwareMotion {

	private final String motionName;
	private final BorderPolicy borderPolicy;

	public EclipseMoveCommand(String motionName, BorderPolicy borderPolicy) {
		this.motionName = motionName;
		this.borderPolicy = borderPolicy;
	}

	@Override
	public BorderPolicy borderPolicy() {
		return borderPolicy;
	}

	@Override
	public Position destination(EditorAdaptor editorAdaptor) {
		return destination(editorAdaptor, 1);
	}

	@Override
	public Position destination(EditorAdaptor editorAdaptor, int count) {
		Position oldCarretOffset = editorAdaptor.getPosition();
		EclipseCommand.doIt(count, motionName, editorAdaptor);
		Position newCarretOffset = editorAdaptor.getPosition();
		editorAdaptor.setPosition(oldCarretOffset, true);
		return newCarretOffset;
	}

	@Override
	public boolean updateStickyColumn() {
		return true;
	}

	public Command command() {
		return new EclipseCommand(motionName);
	}
}
