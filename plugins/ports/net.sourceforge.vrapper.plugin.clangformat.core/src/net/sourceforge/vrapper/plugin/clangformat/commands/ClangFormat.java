package net.sourceforge.vrapper.plugin.clangformat.commands;

import static net.sourceforge.vrapper.platform.Configuration.Option.stringNoConstraint;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sourceforge.vrapper.log.VrapperLog;
import net.sourceforge.vrapper.platform.Configuration.Option;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.FileService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.ProcessHelper;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.LocalConfiguration;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.CountIgnoringNonRepeatableCommand;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Executes LLVM's clang-format on the content of the current editor.
 */
public class ClangFormat extends CountIgnoringNonRepeatableCommand {
    private final String style;

    public static final Option<String> STYLE_OPT = stringNoConstraint("clang-format-style", "llvm");

    public ClangFormat(Queue<String> command) {
        // Extra style configuration, can be empty.
        this.style = StringUtils.join(" ", command).replaceAll("(?i)<SPACE>", " ");
    }

    private void showProcessError(String command, EditorAdaptor editorAdaptor, int exitValue,
            String errorMsg) throws IOException {
        VrapperLog.error("clang-format failed with code " + exitValue + " command: " + command + '\n' + errorMsg);
        editorAdaptor.getUserInterfaceService().setErrorMessage(
                "clang-format failed with code " + exitValue + " command: " + command + " (Check error log).");
    }

    @Override
    public void execute(final EditorAdaptor editorAdaptor) throws CommandExecutionException {
        final FileService fileService = editorAdaptor.getFileService();
        LocalConfiguration configuration = editorAdaptor.getConfiguration();
        Position cursor = editorAdaptor.getPosition();
        final TextContent modelContent = editorAdaptor.getModelContent();
        final int length = modelContent.getTextLength();
        final String text = modelContent.getText(0, length);
        final Selection selection = editorAdaptor.getSelection();
        final int position = selection.getLeftBound().getModelOffset();
        final int rangeLength = selection.getModelLength();
        int rangeStart;
        int rangeEnd;
        try {
            final ArrayList<String> args = new ArrayList<String>(Arrays.asList(
                    "clang-format",
                    "-output-replacements-xml",
                    "-style=" + (style.isEmpty() ? configuration.get(STYLE_OPT) : style),
                    "-assume-filename=" + fileService.getCurrentFileLocation()));
            if (rangeLength != 0) {
                args.add("-offset=" + String.valueOf(position));
                args.add("-length=" + String.valueOf(rangeLength));
                rangeStart = position;
                rangeEnd = position + rangeLength;
            } else {
                args.add("-cursor=" + String.valueOf(cursor.getModelOffset()));
                rangeStart = 0;
                rangeEnd = length;
            }
            final Process p = ProcessHelper.start(args.toArray(new String[args.size()]));
            final BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            //
            // Send full editor content
            //
            final BufferedWriter stdOut = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
            stdOut.write(text);
            stdOut.close();
            //
            // Read and parse clang-format XML output
            //
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            Document replacements = null;
            try {
                replacements = builder.parse(p.getInputStream());
            } catch (Exception e) { }
            final String errors = ProcessHelper.ReadProcessOutput(stdError);
            if (p.waitFor() != 0 || replacements == null || !errors.isEmpty()) {
                showProcessError(StringUtils.join(" ", args), editorAdaptor, p.exitValue(), 
                        errors);
            } else {
                //
                // No errors detected, apply the formatting as single change
                //
                final CursorService cursorService = editorAdaptor.getCursorService();
                editorAdaptor.getHistory().beginCompoundChange();
                try {
                    cursor = cursorService.newPositionForModelOffset(
                            appyReplacements(replacements, modelContent, rangeStart,
                                    rangeEnd, cursor.getModelOffset()));
                } finally {
                    editorAdaptor.getHistory().endCompoundChange();
                }
                cursorService.setPosition(cursor, StickyColumnPolicy.ON_CHANGE);
            }
        } catch (Exception e) {
            throw new CommandExecutionException("clang-format error:" + e.getMessage());
        }
    }

    /**
     * Applies replacements specified in the parsed XML output.
     * @code
     * <?xml version='1.0'?>
     * <replacements xml:space='preserve'>
     *   <replacement offset='451' length='0'> </replacement>
     *   <replacement offset='474' length='1'> </replacement>
     * </replacements>
     * @endcode
     * @param result parsed XML output.
     * @param modelContent text to apply replacements to.
     * @param rangeStart selected region start.
     * @param rangeEnd selected region end.
     * @param cursor current cursor position.
     * @return new cursor position adjusted to match the modifications.
     */
    private int appyReplacements(final Document result, final TextContent modelContent, 
            final int rangeStart, final int rangeEnd, final int cursor)
    {
        final NodeList replacements = result.getFirstChild().getChildNodes();
        int newCursor = cursor;
        //
        // Applying replacements in reverse order to ensure correct offsets.
        //
        for (int i = replacements.getLength(); i > 0; --i) {
            final Node r = replacements.item(i - 1);
            if (r.getNodeName().equals("replacement")) {
                final int index = Integer.parseInt(r.getAttributes().getNamedItem("offset").getNodeValue());
                final int length = Integer.parseInt(r.getAttributes().getNamedItem("length").getNodeValue());
                // clang-format v3.4 tends to overshoot when a range is specified.
                if ((index + length) >= rangeEnd || (index + length) < rangeStart) {
                    continue;
                }
                final String text = r.getTextContent();
                modelContent.replace(index, length, text);
                if (newCursor > index) {
                    newCursor += text.length() - length;
                }
            }
        }
        return newCursor;
    }
}
