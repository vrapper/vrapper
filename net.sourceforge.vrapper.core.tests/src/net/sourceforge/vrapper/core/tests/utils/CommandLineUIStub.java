package net.sourceforge.vrapper.core.tests.utils;

import net.sourceforge.vrapper.platform.CommandLineUI;

/**
 * Stub class to test commands using the command line. Could in theory serve as sample code if
 * the command line would not be rendered by a text widget.
 */
public class CommandLineUIStub implements CommandLineUI {
    
    protected boolean opened;
    protected String prompt = "";
    protected StringBuilder contents = new StringBuilder();
    protected int position;

    @Override
    public void setMode(CommandLineMode mode) {
    }

    @Override
    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    @Override
    public String getPrompt() {
        return prompt;
    }

    @Override
    public void resetContents(String contents) {
        this.contents.setLength(0);
        this.contents.append(contents);
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public void setPosition(int offset) {
        position = offset;
        if (position < 0) {
            position = 0;
        } else if (position > contents.length()) {
            position = contents.length();
        }
    }

    @Override
    public void addOffsetToPosition(int offset) {
        this.position += offset;
        if (position < 0) {
            position = 0;
        } else if (position > contents.length()) {
            position = contents.length();
        }
    }

    @Override
    public String getContents() {
        return contents.toString();
    }

    @Override
    public String getFullContents() {
        return new StringBuilder(contents).insert(0, prompt).toString();
    }

    @Override
    public void type(String characters) {
        contents.insert(position, characters);
        position += characters.length();
    }

    @Override
    public void open() {
        if (opened) {
            //Only here for sanity checking, real implementation is not that strict.
            throw new RuntimeException("Allready opened?");
        }
        opened = true;
    }

    @Override
    public void close() {
        if ( ! opened) {
            //Only here for sanity checking, real implementation is not that strict.
            throw new RuntimeException("Never opened?");
        }
        opened = false;
        prompt = "";
        contents.setLength(0);
        position = 0;
    }

    @Override
    public void erase() {
        if (position > 0) {
            contents.delete(position - 1, position);
            position--;
        }
    }

    @Override
    public void delete() {
        if (position < contents.length()) {
            contents.delete(position, position + 1);
        }
    }

    @Override
    public int getEndPosition() {
        return contents.length();
    }

    @Override
    public void replace(int start, int end, String string) {
        contents.replace(start, end, string);
    }

    @Override
    public void copySelectionToClipboard() {
        // Nothing to do here, stub doesn't know about selections.
    }

    @Override
    public boolean isLastLineShown() {
        return true;
    }

    @Override
    public void scrollDown(boolean wholeScreen) {
        // Does nothing.
    }

}
