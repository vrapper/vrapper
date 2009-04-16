package kg.totality.core.commands.motions;

import kg.totality.core.EditorAdaptor;
import kg.totality.core.commands.BorderPolicy;
import kg.totality.core.commands.Counted;
import newpackage.position.Position;

public interface Motion extends Counted<Motion> {
	Position destination(EditorAdaptor editorAdaptor);
	BorderPolicy borderPolicy();
	boolean updateStickyColumn();
}
