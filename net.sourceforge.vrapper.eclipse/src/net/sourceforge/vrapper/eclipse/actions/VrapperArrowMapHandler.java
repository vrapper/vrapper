package net.sourceforge.vrapper.eclipse.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

import net.sourceforge.vrapper.log.VrapperLog;

/**
 * Remaps an kind of key into the press of an Up / Down / Left / Right arrow key.
 */
public class VrapperArrowMapHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        String commandId = event.getCommand().getId();

        if ( ! (event.getTrigger() instanceof Event)) {
            return null;
        }
        Event triggerEvent = (Event) event.getTrigger();
        if (triggerEvent.type != SWT.KeyDown) {
            VrapperLog.debug("Shortcut handler received an activation other than key type?!?");
            return null;
        }

        Widget widget = triggerEvent.widget;

        // Act as if we're in Insert mode if textbox is focused: insert input key in widget
        if (widget instanceof Text || widget instanceof StyledText) {
            // Check that this command is bound to a Unicode character (and not F1 or something)
            if ((triggerEvent.stateMask & SWT.MODIFIER_MASK) == 0
                    && (triggerEvent.keyCode & SWT.KEYCODE_BIT) == 0) {

                String input = new StringBuilder().appendCodePoint(triggerEvent.keyCode).toString();
                if (widget instanceof Text) {
                    Text textBox = (Text) widget;
                    textBox.insert(input);
                } else if (widget instanceof StyledText) {
                    StyledText styledText = (StyledText) widget;
                    styledText.insert(input);
                }
            }
        } else {

            Event mappedEvent = null;
            // This handler can accept 4 commands (up/down/left/right) and translates them into arrow key presses
            if (commandId.endsWith(".up")) {
                mappedEvent = keyEvent(SWT.ARROW_UP);
            } else if (commandId.endsWith(".down")) {
                mappedEvent = keyEvent(SWT.ARROW_DOWN);
            } else if (commandId.endsWith(".right")) {
                mappedEvent = keyEvent(SWT.ARROW_RIGHT);
            } else if (commandId.endsWith(".left")) {
                mappedEvent = keyEvent(SWT.ARROW_LEFT);
            }
            if (mappedEvent != null) {
                triggerEvent.display.post(mappedEvent);
            }
        }

        return null;
    }

    private static Event keyEvent(int keyCode) {
        Event result = new Event();
        result.type = SWT.KeyDown;
        result.keyCode = keyCode;
        return result;
    }
}