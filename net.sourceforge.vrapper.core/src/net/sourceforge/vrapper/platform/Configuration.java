package net.sourceforge.vrapper.platform;

import net.sourceforge.vrapper.platform.SimpleConfiguration.NewLine;

/**
 * Holds variables which influence the behaviour of different commands.
 *
 * @author Matthias Radig
 */
public interface Configuration {

    public abstract boolean isStartOfLine();

    public abstract void setStartOfLine(boolean startOfLine);

    public abstract String getNewLine();

    public abstract void setNewLine(String newLine);

    public abstract void setNewLine(NewLine newLine);

    public abstract boolean isAutoIndent();

    public abstract void setAutoIndent(boolean autoIndent);

    public abstract boolean isSmartIndent();

    public abstract void setSmartIndent(boolean smartIndent);

    public abstract boolean isAtomicInsert();

    public abstract void setAtomicInsert(boolean atomicInsert);

}