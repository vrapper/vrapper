package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.commands.motions.FindBalancedMotion;

public class ParenthesisPairTextObject extends MotionPairTextObject {

    public ParenthesisPairTextObject(char left, char right, boolean inclusive) {
        super(new FindBalancedMotion(left, right, inclusive, true), new FindBalancedMotion(right, left, inclusive, false), true);
    }

}
