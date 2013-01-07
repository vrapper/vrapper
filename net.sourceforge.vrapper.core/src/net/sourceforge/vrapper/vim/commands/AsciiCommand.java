package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.UserInterfaceService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * :as[cii] or                  *ga* *:as* *:ascii*
 * Print the ascii value of the character under the
 * cursor in decimal, hexadecimal and octal.  For
 * example, when the cursor is on a 'R':
 *      <R>  82,  Hex 52,  Octal 122
 *      
 * TODO:  When the character is a non-standard ASCII character,
 *        but printable according to the 'isprint' option, the
 *        non-printable version is also given.  When the
 *        character is larger than 127, the <M-x> form is also
 *        printed.  For example:
 *      <~A>  <M-^A>  129,  Hex 81,  Octal 201
 *      <p>  <|~>  <M-~>  254,  Hex fe,  Octal 376
 *       (where <p> is a special character)
 *       
 * The <Nul> character will be shown as:
 *      <^@>  0,  Hex 00,  Octal 000
 *
 * TODO: Vim code takes special care to handle EBCDIC (barcode) font.
 *       From the code: 
 *              Finding the position in the alphabet is not straightforward in EBCDIC.
 *              There are gaps in the code table.
 *              'a' + 1 == 'b', but: 'i' + 7 == 'j' and 'r' + 8 == 's'
 *      
 * @author Brian Detweiler
 */
public class AsciiCommand extends AbstractCommand {

    private UserInterfaceService userInterfaceService;

    public static AsciiCommand INSTANCE = new AsciiCommand();
    
    private static final char CTRL_AT   = 0;
    private static final char CTRL_A    = 1;
    private static final char CTRL_B    = 2;
    private static final char CTRL_C    = 3;
    private static final char CTRL_D    = 4;
    private static final char CTRL_E    = 5;
    private static final char CTRL_F    = 6;
    private static final char CTRL_G    = 7;
    private static final char CTRL_H    = 8;
    private static final char CTRL_I    = 9;
    private static final char CTRL_J    = 10;
    private static final char CTRL_K    = 11;
    private static final char CTRL_L    = 12;
    private static final char CTRL_M    = 13;
    private static final char CTRL_N    = 14;
    private static final char CTRL_O    = 15;
    private static final char CTRL_P    = 16;
    private static final char CTRL_Q    = 17;
    private static final char CTRL_R    = 18;
    private static final char CTRL_S    = 19;
    private static final char CTRL_T    = 20;
    private static final char CTRL_U    = 21;
    private static final char CTRL_V    = 22;
    private static final char CTRL_W    = 23;
    private static final char CTRL_X    = 24;
    private static final char CTRL_Y    = 25;
    private static final char CTRL_Z    = 26;
                                              // CTRL-[ Left Square Bracket == ESC
    private static final char CTRL_BSL  = 28; // \ BackSLash
    private static final char CTRL_RSB  = 29; // ] Right Square Bracket
    private static final char CTRL_HAT  = 30; // ^ Carat/Hat
    private static final char CTRL__    = 31; // _

    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
        userInterfaceService = editorAdaptor.getUserInterfaceService();
        int offset = editorAdaptor.getCursorService().getPosition().getModelOffset();
        if(editorAdaptor.getModelContent().getTextLength() == 0) {
            userInterfaceService.setInfoMessage("NUL");
            return;
        }
      
        // Sometimes this happens, where the cursor ends up after the last char in the editor
        if(editorAdaptor.getModelContent().getTextLength() <= offset) {
            userInterfaceService.setInfoMessage("NUL");
            return;
        }
        
        String asciiStr = editorAdaptor.getModelContent().getText(offset, 1);
        char[] asciiChr = asciiStr.toCharArray();
        char ascii = asciiChr[0];
        asciiStr = ascii + "";
       
        switch(ascii) {
            case CTRL_AT :
                asciiStr = "^@";
                break;
            case CTRL_A :
                asciiStr = "^A";
                break;
            case CTRL_B :
                asciiStr = "^B";
                break;
            case CTRL_C :
                asciiStr = "^C";
                break;
            case CTRL_D :
                asciiStr = "^D";
                break;
            case CTRL_E :
                asciiStr = "^E";
                break;
            case CTRL_F :
                asciiStr = "^F";
                break;
            case CTRL_G :
                asciiStr = "^G";
                break;
            case CTRL_H :
                asciiStr = "^H";
                break;
            case CTRL_I :
                asciiStr = "^I";
                break;
            case CTRL_J :
                asciiStr = "^J";
                break;
            case CTRL_K :
                asciiStr = "^K";
                break;
            case CTRL_L :
                asciiStr = "^L";
                break;
            case CTRL_M :
                asciiStr = "^M";
                break;
            case CTRL_N :
                asciiStr = "^N";
                break;
            case CTRL_O :
                asciiStr = "^O";
                break;
            case CTRL_P :
                asciiStr = "^P";
                break;
            case CTRL_Q :
                asciiStr = "^Q";
                break;
            case CTRL_R :
                asciiStr = "^R";
                break;
            case CTRL_S :
                asciiStr = "^S";
                break;
            case CTRL_T :
                asciiStr = "^T";
                break;
            case CTRL_U :
                asciiStr = "^U";
                break;
            case CTRL_V :
                asciiStr = "^V";
                break;
            case CTRL_W :
                asciiStr = "^W";
                break;
            case CTRL_X :
                asciiStr = "^X";
                break;
            case CTRL_Y :
                asciiStr = "^Y";
                break;
            case CTRL_Z :
                asciiStr = "^Z";
                break;
            case CTRL_BSL :
                asciiStr = "^\\";
                break;
            case CTRL_RSB :
                asciiStr = "^]";
                break;
            case CTRL_HAT :
                asciiStr = "^^";
                break;
            case CTRL__ :
                asciiStr = "^_";
                break;
            default :
                break;
        }
        
        userInterfaceService.setAsciiValues(asciiStr,
                                            (int)ascii,
                                            String.format("%02x", (int)ascii),
                                            String.format("%03o", (int)ascii));
    }

    public Command repetition() {
        // NOOP
        return null;
    }

    public Command withCount(int count) {
        // NOOP
        return null;
    }
}