package net.sourceforge.vrapper.vim.commands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.utils.PatternUtils;
import net.sourceforge.vrapper.utils.StringUtils;
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

        } else if (count < 0) {
            throw new CommandExecutionException("Invalid count specified: " + count);
            
        } else if (count > 0 && "#".equals(targetBuffer)) {
            switchBufferNumber(batService, count);
            
        } else if (StringUtils.isInteger(targetBuffer)) {
            try {
                final int targetBufferId = Integer.parseInt(targetBuffer);
                switchBufferNumber(batService, targetBufferId);
            } catch (NumberFormatException e) {
                throw new CommandExecutionException(targetBuffer + " is not a valid number!");
            }
            
        } else {
            switchBufferName(batService);
        }
    }
    
    /** Switch to a buffer number
     * @param pBufferService Service to use for buffer switching
     * @param pTargetBufferId Buffer ID to switch to
     * @throws CommandExecutionException In case of buffer ID not found
     */
    private void switchBufferNumber(BufferAndTabService pBufferService, int pTargetBufferId) throws CommandExecutionException {
        int i = 0;
        List<Buffer> buffers = pBufferService.getBuffers();
        while (i < buffers.size() && ! new Integer(pTargetBufferId).equals(buffers.get(i).getId())) {
            i++;
        }
        
        if (i == buffers.size()) {
            throw new CommandExecutionException("No buffer found with id " + pTargetBufferId);
        }
        
        Buffer buffer = buffers.get(i);
        pBufferService.switchBuffer(buffer);
    }
    
    /** Switch to a buffer whose name matches the command argument (a substring)<br>
     * Source Vim: src/buffer.c
     * @param pBufferService Service to use for buffer switching
     * @throws CommandExecutionException In case of non unique or inexistant buffer name
     */
    private void switchBufferName(BufferAndTabService pBufferService) throws CommandExecutionException {
        final Pattern pattern = PatternUtils.shellPatternToRegex(targetBuffer);
        
        final List<Buffer> matchingBuffers = new ArrayList<Buffer>();
        
        for (Buffer currentBuffer : pBufferService.getBuffers()) {
            final String bufferName = currentBuffer.getDisplayName();
            if (pattern.matcher(bufferName).matches()) {
                matchingBuffers.add(currentBuffer);
            }
        }
        
        if (matchingBuffers.isEmpty()) {
            throw new CommandExecutionException("E94: No matching buffer for " + targetBuffer);
        } else if (matchingBuffers.size() >= 2) {
            throw new CommandExecutionException("E93: More than one match for " + targetBuffer);
        }
        
        pBufferService.switchBuffer(matchingBuffers.get(0));
    }

    @Override
    public CountAwareCommand repetition() {
        return null;
    }
}
