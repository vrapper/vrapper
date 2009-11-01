package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.commands.motions.FindBalancedMotion;

public class QuotedTextObject extends MotionPairTextObject {

    public QuotedTextObject(char delimiter, boolean inclusive) {
        super(new FindBalancedMotion(delimiter, '\0', inclusive, true), new FindBalancedMotion(delimiter, '\0', inclusive, false), false, false);
    }
}
