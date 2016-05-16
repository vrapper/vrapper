package net.sourceforge.vrapper.plugin.surround.state;

public class SimpleDelimiterHolder implements DelimiterHolder {
    
    private String left;
    private String right;
    
    public SimpleDelimiterHolder(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public String getLeft() {
        return left;
    }

    @Override
    public String getRight() {
        return right;
    }

}
