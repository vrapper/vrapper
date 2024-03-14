package net.sourceforge.vrapper.plugin.sneak.modes;

import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.EditorAdaptor;
import net.sourceforge.vrapper.vim.commands.AbstractCommand;
import net.sourceforge.vrapper.vim.commands.Command;
import net.sourceforge.vrapper.vim.commands.CommandExecutionException;
import net.sourceforge.vrapper.vim.commands.SuspendVisualModeCommand;
import net.sourceforge.vrapper.vim.modes.ModeSwitchHint;
import net.sourceforge.vrapper.vim.modes.WithCountHint;

public class ChangeToSneakModeCommand extends AbstractCommand {
    private final ModeSwitchHint[] args;

    public static Command withoutHints() {
        return new ChangeToSneakModeCommand();
    }

    public static Command withHints(ModeSwitchHint...hints) {
        return new ChangeToSneakModeCommand(hints);
    }

    public static Command backwards(ModeSwitchHint...hints) {
        ModeSwitchHint[] hintsAndBackwards = new ModeSwitchHint[hints.length + 1];
        System.arraycopy(hints, 0, hintsAndBackwards, 0, hints.length);
        int indexOfLastHint = hints.length /* -1 + 1 */;

        hintsAndBackwards[indexOfLastHint] = SneakInputMode.SNEAK_BACKWARDS;

        return new ChangeToSneakModeCommand(hintsAndBackwards);
    }

    public static Command fromVisual(ModeSwitchHint...hints) {
        ModeSwitchHint[] hintsFromVisual = new ModeSwitchHint[hints.length + 1];
        System.arraycopy(hints, 0, hintsFromVisual, 0, hints.length);
        int indexOfLastHint = hints.length /* -1 + 1 */;

        hintsFromVisual[indexOfLastHint] = SneakInputMode.FROM_VISUAL;

        return new ChangeToSneakModeCommand(hintsFromVisual);
    }

    public static Command backwardsAndFromVisual(ModeSwitchHint...hints) {
        ModeSwitchHint[] hintsAndBackwardsFromVisual = new ModeSwitchHint[hints.length + 2];
        System.arraycopy(hints, 0, hintsAndBackwardsFromVisual, 0, hints.length);
        int indexOfSecondLastHint = hints.length /* -1 + 1 */;

        hintsAndBackwardsFromVisual[indexOfSecondLastHint] = SneakInputMode.FROM_VISUAL;
        hintsAndBackwardsFromVisual[indexOfSecondLastHint + 1] = SneakInputMode.SNEAK_BACKWARDS;

        return new ChangeToSneakModeCommand(hintsAndBackwardsFromVisual);
    }


    protected ChangeToSneakModeCommand(ModeSwitchHint... args) {
        this.args = args;
    }

    @Override
    public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {

        if (VimUtils.findModeHint(SneakInputMode.FROM_VISUAL.getClass(), args) != null) {
            SuspendVisualModeCommand.INSTANCE.execute(editorAdaptor);
        }
        editorAdaptor.changeMode(SneakInputMode.NAME, args);
    }

    @Override
    public Command repetition() {
        return null;
    }

    @Override
    public Command withCount(int count) {
        ModeSwitchHint[] hintsWithCount = new ModeSwitchHint[args.length + 1];
        System.arraycopy(args, 0, hintsWithCount, 0, args.length);
        int indexOfLastHint = args.length /* -1 + 1 */;

        hintsWithCount[indexOfLastHint] = new WithCountHint(count);

        return new ChangeToSneakModeCommand(hintsWithCount);
    }
}