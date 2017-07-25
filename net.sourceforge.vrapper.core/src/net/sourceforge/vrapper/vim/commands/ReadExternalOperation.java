package net.sourceforge.vrapper.vim.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ProcessHelper;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class ReadExternalOperation extends AbstractLinewiseOperation {

    private static final Pattern READ_PIPE_RE = Pattern.compile("^\\s*r\\s*!\\s*(\\S.*)");
    private static final Pattern FILENAME_RE = Pattern.compile("(?<!\\\\)%");
    private String externalCommand;

    public ReadExternalOperation(String cmdLine) {
        Matcher matcher = READ_PIPE_RE.matcher(cmdLine);
        if (matcher.matches()) {
            externalCommand = matcher.group(1);
        }
    }

    @Override
    public LineRange getDefaultRange(EditorAdaptor editorAdaptor, int count, Position currentPos)
            throws CommandExecutionException {
        return SimpleLineRange.singleLine(editorAdaptor, currentPos);
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, LineRange lineRange)
            throws CommandExecutionException {
        editorAdaptor.getHistory().beginCompoundChange();
        try {
            doIt(editorAdaptor, lineRange.getRegion(editorAdaptor, 0));
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public TextOperation repetition() {
        return this;
    }

    protected void doIt(EditorAdaptor editorAdaptor, TextRange range) {
        if (externalCommand.isEmpty()) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("syntax error for 'r!'");
        }

        // replace '%' with current filename unless it is escaped
        if(externalCommand.contains("%")) {
        	Matcher matcher = FILENAME_RE.matcher(externalCommand);
        	String currentfile = editorAdaptor.getFileService().getCurrentFileName();
        	if(matcher.find()) {
        		externalCommand = externalCommand.replaceAll("%", currentfile);
        	}
        	else {
        		// change '\%' to '%' (remove backslash)
        		externalCommand = externalCommand.replaceAll("\\\\%", "%");
        	}
        }

        try {
            Process p = ProcessHelper.start(ProcessHelper.splitArgs(externalCommand));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String s = ProcessHelper.ReadProcessOutput(stdInput);
            if (p.waitFor() != 0) {
                StringBuffer errorMsg = new StringBuffer();
                while ((s = stdError.readLine()) != null) {
                    errorMsg.append('\n');
                    errorMsg.append(s);
                }
                VrapperLog.error("r!<cmd> failed with code " + p.exitValue() + " command: " + externalCommand + errorMsg);
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        "r!<cmd> failed with code " + p.exitValue() + " command: " + externalCommand + " (Check error log).");
            } else {
                // Get start of line after end line
                int position = range.getRightBound().getModelOffset();
                editorAdaptor.getModelContent().replace(position, 0, s);
                editorAdaptor.getCursorService().setPosition(range.getRightBound(),
                        StickyColumnPolicy.ON_CHANGE);
            }
        } catch (Exception e) {
            VrapperLog.error("r!<cmd> failed : " + e.getMessage(), e);
            editorAdaptor.getUserInterfaceService().setErrorMessage("r!<cmd> failed : " + e.getMessage());
        }
    }

    public static boolean isValid(EditorAdaptor vim, String command) {
        return READ_PIPE_RE.matcher(command).matches();
    }
}
