package de.jroene.vrapper.vim.token;

import de.jroene.vrapper.test.VimTestCase;

public class DeleteTest extends VimTestCase {

	public void testDelete() {
		platform.setBuffer("aaaabbbbccccdddd");
		assertEdit("dl", 0, "aaabbbbccccdddd");
		assertEdit("3dl", 0, "bbbbccccdddd");
		assertEdit("4dl", 0, "ccccdddd");
		assertEdit("d4l", 0, "dddd");
		assertEdit("2d2l", 0, "");

		platform.setBuffer("aaaabbbb");
		assertEdit("dh", 0, "aaaabbbb");
		assertEdit("4dh", 0, "aaaabbbb");
		platform.setBuffer("aaaabbbb");
		platform.setPosition(7);
		assertEdit("dh", 6, "aaaabbb");
		assertEdit("4dh", 2, "aab");
	}

	public void testLineDelete() {
		platform.setBuffer("aaaa\nbbbb\ncccc\ndddd\n");
		platform.setPosition(2);
		assertEdit("dd", 0, "bbbb\ncccc\ndddd\n");
		assertEdit("2dd", 0, "dddd\n");
		platform.setBuffer("bbbb\ncccc\ndddd\n");
		platform.setPosition(2);
		assertEdit("dj", 0, "dddd\n");
		platform.setBuffer("bbbb\ncccc\ndddd\n");
		platform.setPosition(2);
		assertEdit("2dj", 0, "");
		platform.setBuffer("aaaa\nbbbb\ncccc\ndddd\n");
		platform.setPosition(17);
		assertEdit("2dk", 5, "aaaa\n");
	}
}
