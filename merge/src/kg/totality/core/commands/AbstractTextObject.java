package kg.totality.core.commands;

public abstract class AbstractTextObject implements TextObject {
	@Override
	public TextObject withCount(int count) {
		return new MultipliedTextObject(count, this);
	}

	@Override
	public int getCount() {
		return 1;
	}
}
