package net.sourceforge.vrapper.vim.commands;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.LineRange;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ProcessHelper;
import net.sourceforge.vrapper.utils.SimpleLineRange;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

/**
 * Filters a number of lines through an external program. Used to implement
 * <code>:&lt;range&gt;!&lt;filter&gt;</code>.
 */
public class PipeExternalOperation extends AbstractLinewiseOperation {

    private static final Pattern PIPE_RE = Pattern.compile("^\\s*!\\s*(\\S.*)");
    private static final Pattern FILENAME_RE = Pattern.compile("(?<!\\\\)%");
    private String externalCommand;

    public PipeExternalOperation(String cmdLine) {
        Matcher matcher = PIPE_RE.matcher(cmdLine);
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
            editorAdaptor.getUserInterfaceService().setErrorMessage("syntax error for '!'");
        }

        // replace '%' with current filename unless it is escaped
        if(externalCommand.contains("%")) {
        	Matcher matcher = FILENAME_RE.matcher(externalCommand);
        	if(matcher.find()) {
                String currentfile = editorAdaptor.getFileService().getCurrentFileName();
        		externalCommand = externalCommand.replaceAll("%", currentfile);
        	}
        	else {
        		// change '\%' to '%' (remove backslash)
        		externalCommand = externalCommand.replaceAll("\\\\%", "%");
        	}
        }

        TextContent txt = editorAdaptor.getModelContent();
        int position = range.getLeftBound().getModelOffset();
        int length = range.getModelLength();
        String s = txt.getText(range);
        try {
            Process p = ProcessHelper.start(ProcessHelper.splitArgs(externalCommand));
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedWriter stdOut   = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            boolean endsWithNL = s.endsWith(editorAdaptor.getConfiguration().getNewLine());
            stdOut.write(s);
            if (!endsWithNL) {
                stdOut.write(editorAdaptor.getConfiguration().getNewLine());
            }
            stdOut.close();
            s = ProcessHelper.ReadProcessOutput(stdInput);
            if (p.waitFor() != 0) {
                StringBuffer errorMsg = new StringBuffer();
                while ((s = stdError.readLine()) != null) {
                    errorMsg.append('\n');
                    errorMsg.append(s);
                }
                VrapperLog.error("!<cmd> failed with code " + p.exitValue() + " command: " + externalCommand + errorMsg);
                editorAdaptor.getUserInterfaceService().setErrorMessage(
                        "!<cmd> failed with code " + p.exitValue() + " command: " + externalCommand + " (Check error log).");
            } else {
                if (!endsWithNL)
                {
                    s = VimUtils.stripLastNewline(s);
                }
                txt.replace(position, length, s);
                editorAdaptor.getCursorService().setPosition(range.getLeftBound(),
                        StickyColumnPolicy.ON_CHANGE);
            }
        } catch (Exception e) {
            VrapperLog.error("!<cmd> failed : " + e.getMessage(), e);
            editorAdaptor.getUserInterfaceService().setErrorMessage("!<cmd> failed : " + e.getMessage());
        }
    }

    public static boolean isValid(EditorAdaptor vim, String command) {
        return PIPE_RE.matcher(command).matches();
    }
}
