package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;

public class MovementTest extends VimTestCase {

	public void testLeftRight() {
		platform.setBuffer("aa\naaaaa\noeuueu");
		assertMovement("l", 1);
		assertMovement("l", 1);
		assertMovement("h", 0);
		assertMovement("h", 0);
		platform.setPosition(3);
		assertMovement("h", 3);
		assertMovement("2l", 5);
		assertMovement("22l", 7);
		platform.setPosition(9);
		assertMovement("22l", 14);
		assertMovement("4h", 10);
		assertMovement("24h", 9);
	}

	public void testUpDown() {
		platform.setBuffer("aa\naaaaa\noeuueu");
		assertMovement("k", 0);
		assertMovement("j", 3);
		assertMovement("j", 9);
		assertMovement("j", 9);
		assertMovement("4k", 0);
		assertMovement("2j", 9);
	}
}
