package net.sourceforge.vrapper.core.tests.cases;

import static org.mockito.Mockito.when;
import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;
import net.sourceforge.vrapper.vim.register.RegisterManager;

import org.junit.Test;

public class MotionTests extends CommandTestCase {
	private final String longWord;

	public MotionTests() {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<100; i++)
			builder.append("VeryLongWord");
		longWord = builder.toString();
	}

	@Test
	public void testMoveRight() {
		Motion moveRight = new MoveRight();
		checkMotion(moveRight,
				"",'A',"la ma kota",
				"A",'l',"a ma kota");
		checkMotion(moveRight,
				"A",'l',"a ma kota",
				"Al",'a'," ma kota");
		checkMotion(moveRight,
				"Ala ma kot",'a', "",
				"Ala ma kota",EOF,"");
		checkMotion(moveRight,
				"Ala ma kota",EOF,"",
				"Ala ma kota",EOF,"");
		checkMotion(moveRight,
				"Ala ma kota",'\n',"Lata osa koło nosa.",
				"Ala ma kota",'\n',"Lata osa koło nosa.");
	}

	@Test
	public void testMoveLeft() {
		Motion moveLeft = new MoveLeft();
		checkMotion(moveLeft,
				"",'A',"la ma kota",
				"",'A',"la ma kota");
		checkMotion(moveLeft,
				"Al",'a'," ma kota",
				"A",'l',"a ma kota");
		checkMotion(moveLeft,
				"Ala ma kota",EOF,"",
				"Ala ma kot",'a', "");
		checkMotion(moveLeft,
				"Ala ma kota\n",'L',"ata osa koło nosa.",
				"Ala ma kota\n",'L',"ata osa koło nosa.");
	}

	public void testCommonMoveWordWORDRight(Motion wordMotion) {
		// simple stuff
		checkMotion(wordMotion,
				"",'A',"la ma kota",
				"Ala ",'m',"a kota");
		checkMotion(wordMotion,
				"",'A',"LA_MA kota",
				"ALA_MA ",'k',"ota");
		checkMotion(wordMotion,
				"Al",'a'," ma kota",
				"Ala ",'m',"a kota");
		checkMotion(wordMotion,
				"Ala m",'a'," kota",
				"Ala ma ",'k',"ota");
		checkMotion(wordMotion,
				"Ala",' ',"ma kota",
				"Ala ",'m',"a kota");
		checkMotion(wordMotion,
				"Ala ",'i'," kot",
				"Ala i ",'k',"ot");
		checkMotion(wordMotion,
				"Ala",' ',"i kot",
				"Ala ",'i'," kot");
		// end of buffer
		checkMotion(wordMotion,
				"Ala ma kot",'a', "",
				"Ala ma kota",EOF,"");
		checkMotion(wordMotion,
				"Ala ma kota",EOF,"",
				"Ala ma kota",EOF,"");
		// jump over newlines
		checkMotion(wordMotion,
				"Ala ma ko",'t',"a\nLata osa koło nosa.",
				"Ala ma kota\n",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kot",'a',"\nLata osa koło nosa.",
				"Ala ma kota\n",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"Lata osa koło nosa.",
				"Ala ma kota\n",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma ko",'t',"a  \n   Lata osa koło nosa.",
				"Ala ma kota  \n   ",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota ",' ',"\n   Lata osa koło nosa.",
				"Ala ma kota  \n   ",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  ",'\n',"   Lata osa koło nosa.",
				"Ala ma kota  \n   ",'L',"ata osa koło nosa.");
		// DO stop at empty lines
		checkMotion(wordMotion,
				"Ala ma ko",'t',"a\n\nLata osa koło nosa.",
				"Ala ma kota\n",'\n',"Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"\nLata osa koło nosa.",
				"Ala ma kota\n",'\n',"Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota\n",'\n',"Lata osa koło nosa.",
				"Ala ma kota\n\n",'L',"ata osa koło nosa.");
		// don't stop at whitespace lines
		checkMotion(wordMotion,
				"Ala ma kot",'a',"\n  \nLata osa koło nosa.",
				"Ala ma kota\n  \n",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"  \nLata osa koło nosa.",
				"Ala ma kota\n  \n",'L',"ata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota\n",' '," \nLata osa koło nosa.",
				"Ala ma kota\n  \n",'L',"ata osa koło nosa.");
		// long word tests
		checkMotion(wordMotion,
				"checkthisou", 't', "cause" + longWord + " isSo" + longWord,
				"checkthisoutcause" + longWord + " ", 'i', "sSo" + longWord);
		checkMotion(wordMotion,
				longWord + "isn", 't', "it",
				longWord + "isntit", EOF, "");
		checkMotion(wordMotion,
				longWord + "isnti", 't', "",
				longWord + "isntit", EOF, "");
		checkMotion(wordMotion,
				longWord + "isntit", EOF, "",
				longWord + "isntit", EOF, "");
	}

	public void testCommonMoveWordEndWORDEndRight(Motion wordMotion) {
		// simple stuff
		checkMotion(wordMotion,
				"",'A',"la ma kota",
				"Al",'a'," ma kota");
		checkMotion(wordMotion,
				"Al",'a'," ma kota",
				"Ala m",'a'," kota");
		checkMotion(wordMotion,
				"Ala ",'m',"a kota",
				"Ala m",'a'," kota");
		checkMotion(wordMotion,
				"Ala",' ',"ma kota",
				"Ala m",'a'," kota");
		checkMotion(wordMotion,
				"Ala",' ',"i kot",
				"Ala ",'i'," kot");
		checkMotion(wordMotion,
				"Ala ma ko",'t',"a\nLata osa koło nosa.",
				"Ala ma kot",'a',"\nLata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma ko",'t',"a  \n   Lata osa koło nosa.",
				"Ala ma kot",'a',"  \n   Lata osa koło nosa.");
		checkMotion(wordMotion,
				"",'A',"LA_MA kota",
				"ALA_M",'A'," kota");
		// end of buffer
		checkMotion(wordMotion,
				"Ala ma kot",'a', "",
				"Ala ma kota",EOF,"");
		checkMotion(wordMotion,
				"Ala ma kota",EOF,"",
				"Ala ma kota",EOF,"");
		// jump over newlines
		checkMotion(wordMotion,
				"Ala ma kot",'a',"\nLata osa koło nosa.",
				"Ala ma kota\nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"Lata osa koło nosa.",
				"Ala ma kota\nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota ",' ',"\n   Lata osa koło nosa.",
				"Ala ma kota  \n   Lat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  ",'\n',"   Lata osa koło nosa.",
				"Ala ma kota  \n   Lat",'a'," osa koło nosa.");
		// DON'T stop at empty lines
		checkMotion(wordMotion,
				"Ala ma kot",'a',"\n\nLata osa koło nosa.",
				"Ala ma kota\n\nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"\nLata osa koło nosa.",
				"Ala ma kota\n\nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota\n",'\n',"Lata osa koło nosa.",
				"Ala ma kota\n\nLat",'a'," osa koło nosa.");
		// don't stop at whitespace lines
		checkMotion(wordMotion,
				"Ala ma kot",'a',"\n  \nLata osa koło nosa.",
				"Ala ma kota\n  \nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota",'\n',"  \nLata osa koło nosa.",
				"Ala ma kota\n  \nLat",'a'," osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota\n",' '," \nLata osa koło nosa.",
				"Ala ma kota\n  \nLat",'a'," osa koło nosa.");
		// long word tests
		checkMotion(wordMotion,
				"checkthisou", 't', "cause" + longWord + "is so" + longWord,
				"checkthisoutcause" + longWord + "i", 's', " so" + longWord);
		checkMotion(wordMotion,
				longWord + "isn",'t',"it",
				longWord + "isnti",'t',"");
		checkMotion(wordMotion,
				longWord + "isnti", 't', "",
				longWord + "isntit", EOF, "");
		checkMotion(wordMotion,
				longWord + "isntit", EOF, "",
				longWord + "isntit", EOF, "");
	}

	@Test
	public void testMoveWORDRight() {
		Motion moveWORDRight = new MoveBigWORDRight();

		testCommonMoveWordWORDRight(moveWORDRight);

		checkMotion(moveWORDRight,
				"wh",'i',"le(true) ++aw3rs0meness;",
				"while(true) ",'+',"+aw3rs0meness;");
		checkMotion(moveWORDRight,
				"while(true) ",'+',"+aw3rs0meness;",
				"while(true) ++aw3rs0meness;", EOF, "");
	}

	@Test
	public void testMoveWORDEndRight() {
		Motion moveWORDRight = new MoveBigWORDEndRight();

		testCommonMoveWordEndWORDEndRight(moveWORDRight);

		checkMotion(moveWORDRight,
				"wh",'i',"le(true) ++aw3rs0meness;",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWORDRight,
				"while(true",')'," ++aw3rs0meness;",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWORDRight,
				"while(true) ++aw3rs0meness",';',"",
				"while(true) ++aw3rs0meness;", EOF, "");
	}

	@Test
	public void testMoveWordRight() {
		Motion moveWordRight = new MoveWordRight();

		testCommonMoveWordWORDRight(moveWordRight);

		checkMotion(moveWordRight,
				"wh",'i',"le(true) ++aw3rs0meness;",
				"while",'(',"true) ++aw3rs0meness;");
		checkMotion(moveWordRight,
				"while",'(',"true) ++aw3rs0meness;",
				"while(",'t',"rue) ++aw3rs0meness;");
		checkMotion(moveWordRight,
				"while(",'t',"rue) ++aw3rs0meness;",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWordRight,
				"while(true",')'," ++aw3rs0meness;",
				"while(true) ",'+',"+aw3rs0meness;");
		checkMotion(moveWordRight,
				"while(true) ",'+',"+aw3rs0meness;",
				"while(true) ++",'a',"w3rs0meness;");
		checkMotion(moveWordRight,
				"while(true) ++",'a',"w3rs0meness;",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWordRight,
				"while(true) ++aw3rs0meness",';',"",
				"while(true) ++aw3rs0meness;", EOF, "");
	}

	@Test
	public void testMoveWordEndRight() {
		Motion moveWordEndRight = new MoveWordEndRight();

		testCommonMoveWordEndWORDEndRight(moveWordEndRight);

		checkMotion(moveWordEndRight,
				"wh",'i',"le(true) ++aw3rs0meness;",
				"whil",'e',"(true) ++aw3rs0meness;");
		checkMotion(moveWordEndRight,
				"whil",'e',"(true) ++aw3rs0meness;",
				"while",'(',"true) ++aw3rs0meness;");
		checkMotion(moveWordEndRight,
				"while",'(',"true) ++aw3rs0meness;",
				"while(tru",'e',") ++aw3rs0meness;");
		checkMotion(moveWordEndRight,
				"while(tru",'e',") ++aw3rs0meness;",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWordEndRight,
				"while(true",')'," ++aw3rs0meness;",
				"while(true) +",'+',"aw3rs0meness;");
		checkMotion(moveWordEndRight,
				"while(true) +",'+',"aw3rs0meness;",
				"while(true) ++aw3rs0menes",'s',";");
		checkMotion(moveWordEndRight,
				"while(true) ++aw3rs0menes",'s',";",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWordEndRight,
				"while(true) ++aw3rs0meness",';',"",
				"while(true) ++aw3rs0meness;", EOF, "");
	}

	public void testCommonMoveWordWORDLeft(Motion wordMotion) {
		// simple stuff
		checkMotion(wordMotion,
				"Ala ",'m',"a kota",
				"",'A',"la ma kota");
		checkMotion(wordMotion,
				"Ala",' ',"ma kota",
				"",'A',"la ma kota");
		checkMotion(wordMotion,
				"Ala m",'a'," kota",
				"Ala ",'m',"a kota");
		checkMotion(wordMotion,
				"Ala MA_KO",'T',"A",
				"Ala ",'M',"A_KOTA");
		// beginning of buffer
		checkMotion(wordMotion,
				"",'A',"la ma kota",
				"",'A',"la ma kota");
		// end of buffer
		checkMotion(wordMotion,
				"Ala ma kot",'a',"",
				"Ala ma ",'k',"ota");
		checkMotion(wordMotion,
				"Ala ma kota", EOF, "",
				"Ala ma ",'k',"ota");
		checkMotion(wordMotion,
				"Ala ma X", EOF, "",
				"Ala ma ",'X',"");
		// jump over newlines
		checkMotion(wordMotion,
				"Ala ma kota\n",'L',"ata osa koło nosa.",
				"Ala ma ",'k',"ota\nLata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  \n   ",'L',"ata osa koło nosa.",
				"Ala ma ",'k',"ota  \n   Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  \n   ",' ',"Lata osa koło nosa.",
				"Ala ma ",'k',"ota  \n    Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota   i\n   ",'p',"sa Asa",
				"Ala ma kota   ",'i',"\n   psa Asa");
		// DO stop at empty lines
		checkMotion(wordMotion,
				"Ala ma kota\n\n",'L',"ata osa koło nosa.",
				"Ala ma kota\n",'\n',"Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota \n\n",' ',"  Lata osa koło nosa.",
				"Ala ma kota \n",'\n',"   Lata osa koło nosa.");
		// don't stop at whitespace lines
		checkMotion(wordMotion,
				"Ala ma kota\n   \n",'L',"ata osa koło nosa.",
				"Ala ma ",'k',"ota\n   \nLata osa koło nosa.");
		// long word tests
		checkMotion(wordMotion,
				"checkthisoutcauseitsso" + longWord, 'A', "ndSoOn",
				"", 'c', "heckthisoutcauseitsso" + longWord + "AndSoOn");
		checkMotion(wordMotion,
				"So" + longWord + "isn", 't', "it",
				"", 'S', "o" + longWord + "isntit");
		checkMotion(wordMotion,
				"So" + longWord + "isntit", EOF, "",
				"", 'S', "o" + longWord + "isntit");
	}


	public void testCommonMoveWordWORDEndLeft(Motion wordMotion) {
		// simple stuff
		checkMotion(wordMotion,
				"Ala m",'a'," kota",
				"Al",'a'," ma kota");
		checkMotion(wordMotion,
				"Ala ",'m',"a kota",
				"Al",'a'," ma kota");
		checkMotion(wordMotion,
				"Ala",' ',"ma kota",
				"Al",'a'," ma kota");
		checkMotion(wordMotion,
				"Al",'a'," ma kota",
				"",'A',"la ma kota");
		checkMotion(wordMotion,
				"Ala MA_KO",'T',"A",
				"Al",'a'," MA_KOTA");
		// beginning of buffer
		checkMotion(wordMotion,
				"",'A',"la ma kota",
				"",'A',"la ma kota");
		// end of buffer
		checkMotion(wordMotion,
				"Ala ma kot",'a',"",
				"Ala m",'a'," kota");
		checkMotion(wordMotion,
				"Ala ma kota", EOF, "",
				"Ala ma kot",'a',"");
		checkMotion(wordMotion,
				"Ala ma X", EOF, "",
				"Ala ma ",'X',"");
		// jump over newlines
		checkMotion(wordMotion,
				"Ala ma kota\n",'L',"ata osa koło nosa.",
				"Ala ma kot",'a',"\nLata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  \n   ",'L',"ata osa koło nosa.",
				"Ala ma kot",'a',"  \n   Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota  \n   ",' ',"Lata osa koło nosa.",
				"Ala ma kot",'a',"  \n    Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota   i\n   ",'p',"sa Asa",
				"Ala ma kota   ",'i',"\n   psa Asa");
		// DO stop at empty lines
		checkMotion(wordMotion,
				"Ala ma kota\n\n",'L',"ata osa koło nosa.",
				"Ala ma kota\n",'\n',"Lata osa koło nosa.");
		checkMotion(wordMotion,
				"Ala ma kota \n\n",' ',"  Lata osa koło nosa.",
				"Ala ma kota \n",'\n',"   Lata osa koło nosa.");
		// don't stop at whitespace lines
		checkMotion(wordMotion,
				"Ala ma kota\n   \n",'L',"ata osa koło nosa.",
				"Ala ma kot",'a',"\n   \nLata osa koło nosa.");
		// long word tests
		checkMotion(wordMotion,
				"checkthisout causeitsso" + longWord, 'A', "ndSoOn",
				"checkthisou",'t'," causeitsso" + longWord + "AndSoOn");
		checkMotion(wordMotion,
				"So" + longWord + "isn", 't', "it",
				"", 'S', "o" + longWord + "isntit");
		checkMotion(wordMotion,
				"So" + longWord + "isntit", EOF, "",
				"So" + longWord + "isnti", 't', "");
		checkMotion(wordMotion,
				"So" + longWord + "isntit   ", EOF, "",
				"So" + longWord + "isnti", 't', "   ");
	}

	@Test
	public void testMoveWORDLeft() {
		Motion moveWORDLeft = new MoveBigWORDLeft();

		testCommonMoveWordWORDLeft(moveWORDLeft);

		checkMotion(moveWORDLeft,
				"while(true) ++aw3rs0meness;", EOF, "",
				"while(true) ",'+',"+aw3rs0meness;");
		checkMotion(moveWORDLeft,
				"while(true) ",'+',"+aw3rs0meness;",
				"",'w',"hile(true) ++aw3rs0meness;");
	}

	@Test
	public void testMoveWORDEndLeft() {
		Motion moveWORDEndLeft = new MoveBigWORDEndLeft();

		testCommonMoveWordWORDEndLeft(moveWORDEndLeft);

		checkMotion(moveWORDEndLeft,
				"while(true) ++aw3rs0meness;", EOF, "",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWORDEndLeft,
				"while(true) ++aw3rs0meness",';',"",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWORDEndLeft,
				"while(true",')'," ++aw3rs0meness;",
				"",'w',"hile(true) ++aw3rs0meness;");
	}

	@Test
	public void testMoveWordLeft() {
		Motion moveWordLeft = new MoveWordLeft();

		testCommonMoveWordWORDLeft(moveWordLeft);

		checkMotion(moveWordLeft,
				"while(true) ++aw3rs0meness;", EOF, "",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWordLeft,
				"while(true) ++aw3rs0meness",';',"",
				"while(true) ++",'a',"w3rs0meness;");
		checkMotion(moveWordLeft,
				"while(true) ++",'a',"w3rs0meness;",
				"while(true) ",'+',"+aw3rs0meness;");
		checkMotion(moveWordLeft,
				"while(true) ",'+',"+aw3rs0meness;",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWordLeft,
				"while(true",')'," ++aw3rs0meness;",
				"while(",'t',"rue) ++aw3rs0meness;");
		checkMotion(moveWordLeft,
				"while(",'t',"rue) ++aw3rs0meness;",
				"while",'(',"true) ++aw3rs0meness;");
		checkMotion(moveWordLeft,
				"while",'(',"true) ++aw3rs0meness;",
				"",'w',"hile(true) ++aw3rs0meness;");
	}

	@Test
	public void testMoveWordEndLeft() {
		Motion moveWordEndLeft = new MoveWordEndLeft();

		testCommonMoveWordWORDEndLeft(moveWordEndLeft);

		checkMotion(moveWordEndLeft,
				"while(true) ++aw3rs0meness;", EOF, "",
				"while(true) ++aw3rs0meness",';',"");
		checkMotion(moveWordEndLeft,
				"while(true) ++aw3rs0meness",';',"",
				"while(true) ++aw3rs0menes",'s',";");
		checkMotion(moveWordEndLeft,
				"while(true) ++aw3rs0menes",'s',";",
				"while(true) +",'+',"aw3rs0meness;");
		checkMotion(moveWordEndLeft,
				"while(true) +",'+',"aw3rs0meness;",
				"while(true",')'," ++aw3rs0meness;");
		checkMotion(moveWordEndLeft,
				"while(true",')'," ++aw3rs0meness;",
				"while(tru",'e',") ++aw3rs0meness;");
		checkMotion(moveWordEndLeft,
				"while(tru",'e',") ++aw3rs0meness;",
				"while",'(',"true) ++aw3rs0meness;");
		checkMotion(moveWordEndLeft,
				"while",'(',"true) ++aw3rs0meness;",
				"whil",'e',"(true) ++aw3rs0meness;");
		checkMotion(moveWordEndLeft,
				"whil",'e',"(true) ++aw3rs0meness;",
				"",'w',"hile(true) ++aw3rs0meness;");
	}

	@Test
	public void test_f_motionWorks() {
	    Motion fa = new FindMotion('a', true, false);
	    checkMotion(fa,
	            "",'A',"la ma kota",
	            "Al",'a'," ma kota");
	    checkMotion(fa,
	            "Al",'a'," ma kota",
	            "Ala m",'a'," kota");
	    checkMotion(fa,
	            "Ala m",'a'," kota",
	            "Ala ma kot",'a',"");
	    // TODO: assert raises
	}

	@Test
	public void test_F_motionWorks() {
	    Motion Fa = new FindMotion('a', true, true);
	    checkMotion(Fa,
	            "Ala ",'m',"a kota",
	            "Al",'a'," ma kota");
	    checkMotion(Fa,
	            "Ala m",'a'," kota",
	            "Al",'a'," ma kota");
	    checkMotion(Fa,
	            "Ala ma kot",'a',"",
	            "Ala m",'a'," kota");
	    // TODO: assert raises
	}

	@Test
	public void test_ftFT_getParsed() {
	    mode = new NormalMode(adaptor);
	    checkCommand(forKeySeq("fa"),
	            "",'A',"la ma kota",
	            "Al",'a'," ma kota");
	    checkCommand(forKeySeq("ta"),
	            "",'A',"la ma kota",
	            "A",'l',"a ma kota");
	    checkCommand(forKeySeq("Fl"),
	            "Ala m",'a'," kota",
	            "A",'l',"a ma kota");
	    checkCommand(forKeySeq("TA"),
	            "Ala m",'a'," kota",
	            "A",'l',"a ma kota");
	}

	@Test
    public void test_semicolon_and_comma() {
		mode = new NormalMode(adaptor);
		RegisterManager manager = new DefaultRegisterManager();
		when(adaptor.getRegisterManager()).thenReturn(manager);
        checkCommand(forKeySeq("2fa;"),
                "",'A',"la ma kota",
                "Ala ma kot",'a',"");
        checkCommand(forKeySeq("3fa,"),
                "",'A',"la ma kota",
                "Ala m",'a'," kota");
    }

}