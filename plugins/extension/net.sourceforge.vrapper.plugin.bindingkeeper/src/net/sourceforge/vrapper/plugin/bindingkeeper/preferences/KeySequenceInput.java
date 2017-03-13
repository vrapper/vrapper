package net.sourceforge.vrapper.plugin.bindingkeeper.preferences;

import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.keys.IBindingService;

/**
 * Copied and adapted from org.eclipse.ui.internal.key.NewKeysPreferencePage
 * 
 * This program are made available under the terms of the Eclipse Public License
 * v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 */
public class KeySequenceInput extends InputDialog {
	private IBindingService bindingService;

	public KeySequenceInput(String title, IBindingService bindingService) {
		super(Display.getCurrent().getActiveShell(), title, "Enter a shortcut", null, null);
		this.bindingService = bindingService;
	}

	@Override
	protected Control createDialogArea(Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);

		getText().addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
				bindingService.setKeyFilterEnabled(false);
			}

			@Override
			public void focusLost(FocusEvent e) {
				bindingService.setKeyFilterEnabled(true);
			}
		});
		getText().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				if (!bindingService.isKeyFilterEnabled()) {
					bindingService.setKeyFilterEnabled(true);
				}
			}
		});

		final KeySequenceText keySequenceText = new KeySequenceText(getText());
		keySequenceText.setKeyStrokeLimit(1);
		keySequenceText.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public final void propertyChange(final PropertyChangeEvent event) {
				if (!event.getOldValue().equals(event.getNewValue())) {
					final KeySequence keySequence = keySequenceText.getKeySequence();
					if (!keySequence.isComplete()) {
						return;
					}

					getText().setSelection(getText().getTextLimit());
				}
			}
		});

		applyDialogFont(composite);
		return composite;

	}
}