package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;

public class ParenthesesTest extends VimTestCase {

	public void testMove() {
		String s = "aoe(huet)ueou\n( hout{ outheu\n  { toueh }}";
		platform.setBuffer(s);
		assertMovement("%", 8);
		assertMovement("%", 3);
		assertMovement("l%", 3);
		platform.setPosition(15);
		assertMovement("%", s.length()-1);
		assertMovement("h%", 31);
	}

	public void testDelete() {
		String s = "aoe(huet)ueou\n( hout{ outheu\n  { toueh }}";
		platform.setBuffer(s);
		assertEdit("d%", 0, "ueou\n( hout{ outheu\n  { toueh }}");
		platform.setPosition(31);
		assertEdit("d%", 10, "ueou\n( hout");
	}
}
