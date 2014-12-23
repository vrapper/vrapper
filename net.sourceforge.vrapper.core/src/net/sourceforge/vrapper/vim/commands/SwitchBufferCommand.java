package net.sourceforge.vrapper.vim.commands;

import java.util.List;
import java.util.regex.Pattern;

import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.vim.EditorAdaptor;


public class SwitchBufferCommand extends CountAwareCommand {

    public static final Command INSTANCE = new SwitchBufferCommand();
    /**
     * Pattern with which we can detect that at least a prefix of the ':buffer' command was given.
     * Also matches ':buffers', so an extra check must be done to exclude such a command.
     */
    public static final Pattern BUFFER_CMD_PATTERN = Pattern.compile("(b(?:u(?:f(?:f(?:er?)?)?)?)?)");
    /**
     * Pattern which recognizes the arguments that can be glued to the ':buffer' command.
     * Numbers are captured in a group.
     */
    public static final Pattern BUFFER_CMD_GLUED_ARG_PATTERN = Pattern.compile("^!?(?:%|#|([0-9]+))$");

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
