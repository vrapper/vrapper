package net.sourceforge.vrapper.vim.commands;

import java.util.List;

import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.vim.EditorAdaptor;


public class SwitchBufferCommand extends CountAwareCommand {

    public static final Command INSTANCE = new SwitchBufferCommand();

    public String targetBuffer;

    /** Switch to previous buffer if no count is given. */
    public SwitchBufferCommand() {
        targetBuffer = "#";
    }

    /** Switch to the buffer with the given id or target code. */
    public SwitchBufferCommand(String buffer) {
        targetBuffer = buffer;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor, int count)
            throws CommandExecutionException {
        BufferAndTabService batService = editorAdaptor.getBufferAndTabService();
        if ("%".equals(targetBuffer)) {
            return;

        } else if ((count < 0 || count == NO_COUNT_GIVEN) && "#".equals(targetBuffer)) {
            Buffer previousBuffer = batService.getPreviousBuffer();
            batService.switchBuffer(previousBuffer);

        } else {
            Integer targetBufferId;
            if (count < 0) {
                throw new CommandExecutionException("Invalid count specified: " + count);
            } else if (count > 0 && "#".equals(targetBuffer)) {
                targetBufferId = count;
            } else {
                try {
                    targetBufferId = Integer.parseInt(targetBuffer);
                } catch (NumberFormatException e) {
                    throw new CommandExecutionException(targetBuffer + " is not a valid number!");
                }
            }
            int i = 0;
            List<Buffer> buffers = batService.getBuffers();
            while (i < buffers.size() && ! targetBufferId.equals(buffers.get(i).getId())) {
                i++;
            }
            if (i == buffers.size()) {
                throw new CommandExecutionException("No buffer found with id " + targetBufferId);
            }
            Buffer buffer = buffers.get(i);
            batService.switchBuffer(buffer);
        }
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }
}
