package net.sourceforge.vrapper.vim.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.ProcessHelper;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

public class ReadExternalOperation extends SimpleTextOperation
{
    private static final Pattern READ_PIPE_RE = Pattern.compile("^\\s*r\\s*!\\s*(\\S.*)");
    private String externalCommand;

    public ReadExternalOperation(String cmdLine) {
        Matcher matcher = READ_PIPE_RE.matcher(cmdLine);
        if (matcher.matches()) {
            externalCommand = matcher.group(1);
        }
    }

    public void execute(EditorAdaptor editorAdaptor, TextRange region, ContentType contentType) {
        editorAdaptor.getHistory().beginCompoundChange();
        try {
            doIt(editorAdaptor, region, contentType);
        } finally {
            editorAdaptor.getHistory().endCompoundChange();
        }
    }

    public TextOperation repetition() {
        return this;
    }

    public void doIt(EditorAdaptor editorAdaptor, TextRange range, ContentType contentType) {
        if (externalCommand.isEmpty()) {
            editorAdaptor.getUserInterfaceService().setErrorMessage("syntax error for 'r!'");
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
