package de.jroene.vrapper.vim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.jroene.vrapper.vim.InsertMode.Parameters;
import de.jroene.vrapper.vim.register.DefaultRegisterManager;
import de.jroene.vrapper.vim.register.RegisterManager;

/**
 * Manages the different modes, configuration and registers.
 *
 * @author Matthias Radig
 */
public class VimEmulator {

    private final Platform platform;
    private Mode mode;
    private final InsertMode insertMode;
    private final NormalMode normalMode;
    private final CommandLineMode commandLineMode;
    private final VimVariables variables;
    private int horizontalPosition;
    private final RegisterManager registerManager;

    public VimEmulator(Platform platform) {
        this.platform = platform;
        this.insertMode = new InsertMode(this);
        this.normalMode = new NormalMode(this);
        this.commandLineMode = new CommandLineMode(this);
        this.variables = new VimVariables();
        this.registerManager = new DefaultRegisterManager();
        readConfiguration();
        toNormalMode();
    }

    public boolean type(VimInputEvent e) {
        return mode.type(e);
    }

    public void toInsertMode(Parameters params) {
        mode = insertMode;
        insertMode.initializeWithParams(params);
        platform.toInsertMode();
    }

    public void toNormalMode() {
        mode = normalMode;
        platform.toNormalMode();
    }

    public void toCommandLineMode() {
        mode = commandLineMode;
        commandLineMode.type(new VimInputEvent.Character(':'));
        platform.toCommandLineMode();
    }

    public void toCharacterNormalMode() {
        mode = normalMode.getKeystrokeMode();
    }

    public void toVisualMode(boolean lineWise) {
        mode = normalMode.getVisualMode(lineWise);
    }

    public boolean inInsertMode() {
        return mode.equals(insertMode);
    }

    public boolean inNormalMode() {
        return mode.equals(normalMode);
    }

    public Platform getPlatform() {
        return platform;
    }

    public int getHorizontalPosition() {
        return horizontalPosition;
    }

    public void updateHorizontalPosition() {
        LineInformation currLine = platform.getLineInformation();
        horizontalPosition = platform.getPosition() - currLine.getBeginOffset();
    }

    public VimVariables getVariables() {
        return variables;
    }

    public RegisterManager getRegisterManager() {
        return registerManager;
    }

    public NormalMode getNormalMode() {
        return normalMode;
    }

    private void readConfiguration() {
        File homeDir = new File(System.getProperty("user.home"));
        File config = new File(homeDir, ".vrapperrc");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config));
            String line;
            while((line = reader.readLine()) != null) {
                commandLineMode.parseAndExecute(line.trim());
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}