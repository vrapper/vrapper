package kg.totality.core.keymap.vim;

import kg.totality.core.utils.Function;

public interface ReplicatorFactory<T> {

	Function<T, T> getReplicator(int count);

}
