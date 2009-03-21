package de.jroene.vrapper.vim;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import de.jroene.vrapper.vim.InsertMode.Parameters;
import de.jroene.vrapper.vim.commandline.CommandLineMode;
import de.jroene.vrapper.vim.commandline.SearchMode;
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
    private final SearchMode searchMode;
    private final VimConfig variables;
    private int horizontalPosition;
    private RegisterManager registerManager;
    private final RegisterManager globalRegisterManager;

    public VimEmulator(Platform platform, RegisterManager globalRegisterManager) {
        this.platform = platform;
        this.insertMode = new InsertMode(this);
        this.normalMode = new NormalMode(this);
        this.commandLineMode = new CommandLineMode(this);
        this.searchMode = new SearchMode(this);
        this.variables = new VimConfig();
        this.registerManager = globalRegisterManager;
        this.globalRegisterManager = globalRegisterManager;
        readConfiguration();
        autoDetectNewline();
        toNormalMode();
    }

    private void autoDetectNewline() {
        if (platform.getNumberOfLines() > 1) {
            LineInformation first = platform.getLineInformation(0);
            LineInformation second = platform.getLineInformation(1);
            int start = first.getEndOffset();
            int length = second.getBeginOffset()-start;
            String newLine = platform.getText(start, length);
            variables.setNewLine(newLine);
        }
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
        updateHorizontalPosition();
    }

    public void toCommandLineMode() {
        mode = commandLineMode;
        commandLineMode.type(new VimInputEvent.Character(
                VimConstants.COMMAND_LINE_CHAR.charAt(0)));
        platform.toCommandLineMode();
    }

    public void toSearchMode(boolean backwards) {
        mode = searchMode;
        String character = backwards ? VimConstants.BACKWARD_SEARCH_CHAR
                : VimConstants.FORWARD_SEARCH_CHAR;
        searchMode.type(new VimInputEvent.Character(character.charAt(0)));
        platform.toCommandLineMode();
    }

    public void toCharacterMode() {
        mode.toKeystrokeMode();
    }

    public void toVisualMode(boolean lineWise) {
        NormalMode.VisualMode visual = normalMode.getVisualMode(lineWise);
        mode = visual;
        platform.toVisualMode();
        visual.initialize();
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

    public VimConfig getVariables() {
        return variables;
    }

    public RegisterManager getRegisterManager() {
        return registerManager;
    }

    public NormalMode getNormalMode() {
        return normalMode;
    }

    public Mode getMode() {
        return mode;
    }

    public void useGlobalRegisters() {
        registerManager = globalRegisterManager;
    }

    public void useLocalRegisters() {
        registerManager = new DefaultRegisterManager();
    }

    private void readConfiguration() {
        File homeDir = new File(System.getProperty("user.home"));
        File config = new File(homeDir, ".vrapperrc");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(config));
            String line;
            while((line = reader.readLine()) != null) {
                commandLineMode.parseAndExecute(null, line.trim());
            }
        } catch (FileNotFoundException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}