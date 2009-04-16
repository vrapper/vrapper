package net.sourceforge.vrapper.keymap.vim;

import net.sourceforge.vrapper.utils.Function;

public interface ReplicatorFactory<T> {

	Function<T, T> getReplicator(int count);

}
