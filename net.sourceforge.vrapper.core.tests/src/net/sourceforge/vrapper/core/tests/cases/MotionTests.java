package net.sourceforge.vrapper.core.tests.cases;

import net.sourceforge.vrapper.core.tests.utils.CommandTestCase;
import net.sourceforge.vrapper.vim.commands.motions.FindMotion;
import net.sourceforge.vrapper.vim.commands.motions.GoToLineMotion;
import net.sourceforge.vrapper.vim.commands.motions.Motion;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveBigWORDRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveLeftAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveRightAcrossLines;
import net.sourceforge.vrapper.vim.commands.motions.MoveUpDownNonWhitespace;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordEndRight;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordLeft;
import net.sourceforge.vrapper.vim.commands.motions.MoveWordRight;
import net.sourceforge.vrapper.vim.commands.motions.ParagraphMotion;
import net.sourceforge.vrapper.vim.commands.motions.ParenthesesMove;
import net.sourceforge.vrapper.vim.modes.NormalMode;
import net.sourceforge.vrapper.vim.register.DefaultRegisterManager;

import org.junit.Test;

public class MotionTests extends CommandTestCase {
	private final String longWord;

	public MotionTests() {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<100; i++) {
            builder.append("VeryLongWord");
        }
		longWord = builder.toString();
	}

	@Override
	protected void reloadEditorAdaptor() {
	    super.reloadEditorAdaptor();
        adaptor.changeModeSafely(NormalMode.NAME);
	};

	@Test
	public void testMoveRight() {
		Motion moveRight = MoveRight.INSTANCE;
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
		Motion moveLeft = MoveLeft.INSTANCE;
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
	
	@Test
	public void testMoveLeftAcrossLines() {
		Motion moveLeft = MoveLeftAcrossLines.INSTANCE;
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
				"Ala ma kota",'\n',"Lata osa koło nosa.");
		checkMotion(moveLeft,
				"Ala ma kota\n\r",'L',"ata osa koło nosa.",
				"Ala ma kota\n",'\r',"Lata osa koło nosa.");
	}

	@Test
	public void testMoveRightAcrossLines() {
		Motion moveRight = MoveRightAcrossLines.INSTANCE;
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
				"Ala ma kota\n",'L',"ata osa koło nosa.");
		checkMotion(moveRight,
				"Ala ma kota",'\n',"\rLata osa koło nosa.",
				"Ala ma kota\n\r",'L',"ata osa koło nosa.");
	}
	
	@Test
	public void testMoveUp() {
		Motion moveUp = MoveUpDownNonWhitespace.MOVE_UP;
		//this isn't actually how Vim behaves, but I still want to test
		//the case where the cursor is already on the first line
		//(vim wouldn't move the cursor to the beginning of the line)
		checkMotion(moveUp,
				"something ", 's', "omething",
				"", 's', "omething something");
		
		//normal "move up" case
		checkMotion(moveUp,
				"something something\nsomething ", 's', "omething\nelse",
				"", 's', "omething something\nsomething something\nelse");
		
		//leading spaces
		checkMotion(moveUp,
				"     something something\nsomething ", 's', "omething\nelse",
				"     ", 's', "omething something\nsomething something\nelse");
		
		//leading tab
		checkMotion(moveUp,
				"	something something\n    something ", 's', "omething\nelse",
				"	", 's', "omething something\n    something something\nelse");
	}
	
	@Test
	public void testMoveDown() {
		Motion moveDown = MoveUpDownNonWhitespace.MOVE_DOWN;
		//this isn't actually how Vim behaves, but I still want to test
		//the case where the cursor is already on the last line
		//(vim wouldn't move the cursor to the beginning of the line)
		checkMotion(moveDown,
				"something ", 's', "omething",
				"", 's', "omething something");
		
		//normal "move down" case
		checkMotion(moveDown,
				"something ", 's', "omething\nsomething something\nelse",
				"something something\n", 's', "omething something\nelse");
		
		//leading spaces
		checkMotion(moveDown,
				"something ", 's', "omething\n   something something\nelse",
				"something something\n   ", 's', "omething something\nelse");
		
		//leading tab
		checkMotion(moveDown,
				"something ", 's', "omething\n	something something\nelse",
				"something something\n	", 's', "omething something\nelse");
	}
	   
    @Test
    public void testMoveDownLessOne() {
        Motion moveDownLessOne = MoveUpDownNonWhitespace.MOVE_DOWN_LESS_ONE;
        checkMotion(moveDownLessOne,
                "something ", 's', "omething",
                "", 's', "omething something");
        
        //should stay on the same line
        checkMotion(moveDownLessOne,
                "line 1\nlin",'e'," 2\nline 3",
                "line 1\n",'l',"ine 2\nline 3");
        
        checkMotion(moveDownLessOne,
                "line 1\nline 2\nli",'n',"e 3",
                "line 1\nline 2\n",'l',"ine 3");
        
        //the motion needs to be given a count of 2 to actually move down
        checkMotion(moveDownLessOne, 2,
                "something ", 's', "omething\nsomething something\nelse",
                "something something\n", 's', "omething something\nelse");
        
        //otherwise it stays on the current line
        checkMotion(moveDownLessOne,
                "something ", 's', "omething\nsomething something\nelse",
                "",'s',"omething something\nsomething something\nelse");
        
        //leading spaces
        checkMotion(moveDownLessOne,
                "    something ", 's', "omething\nsomething else",
                "    ",'s',"omething something\nsomething else");
        
        //leading tab
        checkMotion(moveDownLessOne,
                "   something ", 's', "omething",
                "   ",'s',"omething something");
    
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
		Motion moveWORDRight = MoveBigWORDRight.INSTANCE;

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
		Motion moveWORDRight = MoveBigWORDEndRight.INSTANCE;

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
		Motion moveWordRight = MoveWordRight.INSTANCE;

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
		Motion moveWordEndRight = MoveWordEndRight.INSTANCE;

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
		Motion moveWORDLeft = MoveBigWORDLeft.INSTANCE;

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
		Motion moveWORDEndLeft = MoveBigWORDEndLeft.INSTANCE;

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
		Motion moveWordLeft = MoveWordLeft.INSTANCE;

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
	public void testMoveWordLeftBug() {
		Motion moveWordLeft = MoveWordLeft.INSTANCE;
		checkMotion(moveWordLeft,
				"so",'m',"ething",
				"",'s',"omething");
	}

	@Test
	public void testMoveWordEndLeft() {
		Motion moveWordEndLeft = MoveWordEndLeft.INSTANCE;

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
	    checkMotion(fa,
	            "A",'l',"a ma kota",
	            "Al",'a'," ma kota");
	    // TODO: assert raises
	}

	@Test
	public void test_t_motionWorks() {
	    Motion ta = new FindMotion('a', false, false);
	    checkMotion(ta,
	            "",'A',"la ma kota",
	            "A",'l',"a ma kota");
	    checkMotion(ta,
	            "Al",'a'," ma kota",
	            "Ala ",'m',"a kota");
	    checkMotion(ta,
	            "Ala m",'a'," kota",
	            "Ala ma ko",'t',"a");
	    checkMotion(ta,
	            "A",'l',"a ma kota",
	            "A",'l',"a ma kota");
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
        registerManager = new DefaultRegisterManager();
        reloadEditorAdaptor();
        checkCommand(forKeySeq("2fa;"),
                "",'A',"la ma kota",
                "Ala ma kot",'a',"");
        checkCommand(forKeySeq("3fa,"),
                "",'A',"la ma kota",
                "Ala m",'a'," kota");
        checkCommand(forKeySeq("2ta;"),
                "",'A',"la ma kota",
                "Ala ma ko",'t',"a");
        checkCommand(forKeySeq("3ta,"),
                "",'A',"la ma kota",
                "Ala ma",' ',"kota");
    }

	@Test
	public void test_gg() {
	    checkMotion(GoToLineMotion.FIRST_LINE,
	            "  Ala ma kota\n  Ala ",'m',"a kota\n  Ala ma kota",
	            "  ",'A',"la ma kota\n  Ala ma kota\n  Ala ma kota");

	    checkMotion(GoToLineMotion.FIRST_LINE, 2,
	            "  Ala ma kota\n  Ala ",'m',"a kota\n  Ala ma kota",
	            "  Ala ma kota\n  ",'A',"la ma kota\n  Ala ma kota");
	}

	@Test
    public void test_G() {
	    checkMotion(GoToLineMotion.LAST_LINE,
	            "  Ala ma kota\n  Ala ",'m',"a kota\n  Ala ma kota",
	            "  Ala ma kota\n  Ala ma kota\n  ",'A',"la ma kota");

	    checkMotion(GoToLineMotion.LAST_LINE, 2,
	            "  Ala ma kota\n  Ala ",'m',"a kota\n  Ala ma kota",
	            "  Ala ma kota\n  ",'A',"la ma kota\n  Ala ma kota");
    }
	
	@Test
    public void testParagrapForwardMotion() {
	    Motion oneParagraphForward = ParagraphMotion.FORWARD;
        checkMotion(oneParagraphForward,
	            "Lorem ipsum do",'l',"or sit amet, consectetuer adipiscing elit.\n"+
	            "Proin nibh augue, suscipit a, scelerisque sed, lacinia in, mi.\n"+
	            "\n"+
	            "\n"+
	            "Almost like Cicero\n",
	            
	            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.\n"+
	            "Proin nibh augue, suscipit a, scelerisque sed, lacinia in, mi.\n",
	            '\n',
	            "\n"+
	            "Almost like Cicero\n"
        );
        
        checkMotion(oneParagraphForward,
                "Marry has ",'a'," little lamb",
                "Marry has a little lamb",EOF,"");
        
        checkMotion(oneParagraphForward,
	            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.\n"+
	            "Proin nibh augue, suscipit a, scelerisque sed, lacinia in, mi.\n",
	            '\n',
	            "\n"+
	            "Almost like Cicero\n",
	            
	            "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.\n"+
	            "Proin nibh augue, suscipit a, scelerisque sed, lacinia in, mi.\n"+
	            "\n"+
	            "\n"+
	            "Almost like Cicero\n",EOF,""
        );
        
        checkMotion(oneParagraphForward, 3,
                "e",'i',"ns\n\nzwei\n\ndrei\n\nPolizei!",
                "eins\n\nzwei\n\ndrei\n",'\n',"Polizei!");
    }

	
	@Test
    public void testParagraphBackwardMotion() {
	    Motion oneParagraphBackward = ParagraphMotion.BACKWARD;
        checkMotion(oneParagraphBackward,
	            "aaah!\n"+
	            "\n"+
	            "\n"+
	            "this is\n"+
	            "so ",'g',"reat",
	            
	            "aaah!\n"+
	            "\n",
	            '\n',
	            "this is\n"+
	            "so great"
        );
        checkMotion(oneParagraphBackward,
                "Marry has ",'a'," little lamb",
                "",'M',"arry has a little lamb");
        
        checkMotion(oneParagraphBackward,
                "\n\n",'\n',"\n\n",
                "",'\n',"\n\n\n\n");
        
        checkMotion(oneParagraphBackward, 3,
                "eins\n\nzwei\n\ndrei\n\n",'P',"olizei!",
                "eins\n",'\n',"zwei\n\ndrei\n\nPolizei!");
    }
	
	@Test
	public void testPercentMatch() {
		Motion parenthesesMove = ParenthesesMove.INSTANCE;
		
		//basic case
		checkMotion(parenthesesMove,
			"(match",')',"",
			"",'(',"match)");
		
		checkMotion(parenthesesMove,
			"",'(',"match)",
			"(match",')',"");
		
		checkMotion(parenthesesMove,
			"{match",'}',"",
			"",'{',"match}");
		
		checkMotion(parenthesesMove,
			"",'{',"match}",
			"{match",'}',"");
		
		checkMotion(parenthesesMove,
			"[match",']',"",
			"",'[',"match]");
		
		checkMotion(parenthesesMove,
			"",'[',"match]",
			"[match",']',"");
		
		//with nesting
		checkMotion(parenthesesMove,
			"(ma()tch",')',"",
			"",'(',"ma()tch)");
		
		checkMotion(parenthesesMove,
			"",'(',"ma()tch)",
			"(ma()tch",')',"");
		
		checkMotion(parenthesesMove,
			"{ma{}tch",'}',"",
			"",'{',"ma{}tch}");
		
		checkMotion(parenthesesMove,
			"",'{',"ma{}tch}",
			"{ma{}tch",'}',"");
		
		checkMotion(parenthesesMove,
			"[ma[]tch",']',"",
			"",'[',"ma[]tch]");
		
		checkMotion(parenthesesMove,
			"",'[',"ma[]tch]",
			"[ma[]tch",']',"");
		
		//no match found
		checkMotion(parenthesesMove,
			"match",')',"",
			"match",')',"");
		
		checkMotion(parenthesesMove,
			"",'(',"match",
			"",'(',"match");
		
		checkMotion(parenthesesMove,
			"match",'}',"",
			"match",'}',"");
		
		checkMotion(parenthesesMove,
			"",'{',"match",
			"",'{',"match");
		
		checkMotion(parenthesesMove,
			"match",']',"",
			"match",']',"");
		
		checkMotion(parenthesesMove,
			"",'[',"match",
			"",'[',"match");
	}
	
	@Test
	public void testParenthesesMatching() {
		Motion matchOpenParen  = ParenthesesMove.MATCH_OPEN_PAREN;
		Motion matchCloseParen = ParenthesesMove.MATCH_CLOSE_PAREN;
		Motion matchOpenCurly  = ParenthesesMove.MATCH_OPEN_CURLY;
		Motion matchCloseCurly = ParenthesesMove.MATCH_CLOSE_CURLY;
		
		//basic case
		checkMotion(matchOpenParen,
			"(matc",'h',")",
			"",'(',"match)");
		
		checkMotion(matchCloseParen,
			"(",'m',"atch)",
			"(match",')',"");
		
		checkMotion(matchOpenCurly,
			"{matc",'h',"}",
			"",'{',"match}");
		
		checkMotion(matchCloseCurly,
			"{",'m',"atch}",
			"{match",'}',"");
		
		//with nesting
		checkMotion(matchOpenParen,
			"(ma()tc",'h',")",
			"",'(',"ma()tch)");
		
		checkMotion(matchCloseParen,
			"(",'m',"a()tch)",
			"(ma()tch",')',"");
		
		checkMotion(matchOpenCurly,
			"{ma{}tc",'h',"}",
			"",'{',"ma{}tch}");
		
		checkMotion(matchCloseCurly,
			"{",'m',"a{}tch}",
			"{ma{}tch",'}',"");
		
		//no match found
		checkMotion(matchOpenParen,
			"matc",'h',")",
			"matc",'h',")");
		
		checkMotion(matchCloseParen,
			"(",'m',"atch",
			"(",'m',"atch");
		
		checkMotion(matchOpenCurly,
			"matc",'h',"}",
			"matc",'h',"}");
		
		checkMotion(matchCloseCurly,
			"{",'m',"atch",
			"{",'m',"atch");
		
	}

}