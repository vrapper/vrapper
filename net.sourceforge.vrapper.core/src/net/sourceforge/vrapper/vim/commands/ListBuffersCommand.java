package net.sourceforge.vrapper.vim.commands;

import java.util.List;

import net.sourceforge.vrapper.platform.Buffer;
import net.sourceforge.vrapper.platform.BufferAndTabService;
import net.sourceforge.vrapper.vim.EditorAdaptor;

public class ListBuffersCommand extends AbstractMessagesCommand {

    public static final Command INSTANCE = new ListBuffersCommand();

    @Override
    protected String getMessages(EditorAdaptor editorAdaptor)
            throws CommandExecutionException {
        StringBuilder sb = new StringBuilder();
        BufferAndTabService bufferAndTabService = editorAdaptor.getBufferAndTabService();
        List<Buffer> buffers = bufferAndTabService.getBuffers();
        for (Buffer buffer : buffers) {
            int bufferId = buffer.getId();
            char unlisted = ' ';
            char activeAlt = ' ';
            char visible = ' ';
            char modifiableReadonly = ' ';
            char modified = ' ';
            if (buffer.isActive()) {
                activeAlt = '%';
            } else if (buffer.isAlternate()) {
                activeAlt = '#';
            }
            sb.append(String.format("%3d%c%c%c%c%c \"%s\"\n", bufferId, unlisted, activeAlt,
                    visible, modifiableReadonly, modified, buffer.getDisplayName()));
        }
        return sb.toString();
    }

    @Override
    public boolean isClipped() {
        return true;
    }
}
