package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.platform.HistoryService;
import net.sourceforge.vrapper.platform.TextContent;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.StringUtils;
import net.sourceforge.vrapper.utils.TextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.VimConstants;
import net.sourceforge.vrapper.vim.commands.BlockWiseSelection.TextBlock;
import net.sourceforge.vrapper.vim.commands.motions.StickyColumnPolicy;
import net.sourceforge.vrapper.vim.register.RegisterContent;
import net.sourceforge.vrapper.vim.register.TextBlockRegisterContent;

public class BlockwisePasteCommand extends CountAwareCommand {

    public final static BlockwisePasteCommand INSTANCE = new BlockwisePasteCommand();

    @Override
    public CountAwareCommand repetition() {
        return null;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        if (count == Command.NO_COUNT_GIVEN)
            count = 1;
        editorAdaptor.rememberLastActiveSelection();
        final BlockWiseSelection selection = (BlockWiseSelection) editorAdaptor.getSelection();
        final TextContent content = editorAdaptor.getModelContent();
        final TextBlock rect = BlockWiseSelection.getTextBlock(
                selection.getFrom(), selection.getTo(), content,
                editorAdaptor.getCursorService());
        final TextRange blockRange = selection.getRegion(editorAdaptor, NO_COUNT_GIVEN);
        final RegisterContent registerContent = editorAdaptor
                .getRegisterManager().getActiveRegister().getContent();
        final ContentType pastingContentType = registerContent.getPayloadType();
        final HistoryService history = editorAdaptor.getHistory();
        history.beginCompoundChange();
        history.lock("block-paste");
        try {
            //
            // Regardless of the paste content -- delete the block first.
            //
            SelectionBasedTextOperationCommand.doIt(editorAdaptor,
                    NO_COUNT_GIVEN, DeleteOperation.INSTANCE, content,
                    blockRange, true);
            int position = blockRange.getLeftBound().getModelOffset();
            switch (pastingContentType) {
            case TEXT: {
                //
                // Create a fake text block of equal line count with the text
                // replicated on every line and multiplied by count.
                //
                final TextBlockRegisterContent repl = new TextBlockRegisterContent(0, VimConstants.REGISTER_NEWLINE);
                final String text = StringUtils.multiply(
                    VimUtils.replaceNewLines(registerContent.getText(), ""), count);
                for (int l = rect.startLine; l <= rect.endLine; ++l) {
                    repl.appendLine(text);
                }
                // Paste the fake block at old block position.
                position = BlockPasteHelper.execute(editorAdaptor, 1, 0, false,
                        repl, blockRange.getLeftBound());
                break;
            }
            case TEXT_RECTANGLE: {
                position = BlockPasteHelper.execute(editorAdaptor, count, 0,
                        false, registerContent, blockRange.getLeftBound());
                break;
            }
            case LINES: {
                String text = StringUtils.multiply(registerContent.getText(), count);
                final String newLine = editorAdaptor.getConfiguration().getNewLine();
                text = newLine + VimUtils.stripLastNewline(VimUtils.replaceNewLines(text, newLine));
                final int pastePos = content.getLineInformation(rect.endLine).getEndOffset();
                content.replace(pastePos, 0, text);
                final LineInformation firstPastedLine = content.getLineInformation(rect.endLine + 1);
                position = VimUtils.getFirstNonWhiteSpaceOffset(content, firstPastedLine);
                break;
            }
            default:
                break;
            }
            final Position destination = editorAdaptor.getCursorService()
                    .newPositionForModelOffset(position);
            editorAdaptor.setPosition(destination, StickyColumnPolicy.ON_CHANGE);
        } finally {
            history.unlock("block-paste");
            history.endCompoundChange();
            LeaveVisualModeCommand.doIt(editorAdaptor);
        }
    }

}
