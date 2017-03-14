package net.sourceforge.vrapper.plugin.bindingkeeper.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.vrapper.plugin.bindingkeeper.BindingKeeper;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.keys.IBindingService;

/**
 * @author Pedro Santos
 * 
 */
public class BindingkeeperPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private IBindingService bindingService;
	private TableViewer conflictViewer;
	private TableConflicts tableConflicts;

	private GridData verticalLayoutData = new GridData();
	private GridData horizontalLayoutData = new GridData();

	private BooleanFieldEditor disableConflictingShortcutsField;
	private Table conflictTable;
	private Button addButton;
	private Button removeButton;

	public BindingkeeperPreferencePage() {
		super(GRID);
		setPreferenceStore(BindingKeeper.getDefault().getPreferenceStore());
		tableConflicts = new TableConflicts();
		verticalLayoutData.grabExcessVerticalSpace = true;
		verticalLayoutData.verticalAlignment = SWT.FILL;
		verticalLayoutData.grabExcessHorizontalSpace = true;
		verticalLayoutData.horizontalAlignment = SWT.FILL;

		horizontalLayoutData.grabExcessHorizontalSpace = true;
		horizontalLayoutData.horizontalAlignment = SWT.NONE;

	}

	@Override
	public void init(IWorkbench workbench) {
		bindingService = (IBindingService) workbench.getService(IBindingService.class);
	}

	@Override
	protected void performDefaults() {
		tableConflicts.performDefaults();
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		tableConflicts.performOk();
		return super.performOk();
	}

	public void createFieldEditors() {

		Group group = SWTFactory.createGroup(getFieldEditorParent(), "Eclipse's shortcuts conflicting VIM ones", 1, 1,
				GridData.FILL_HORIZONTAL);
		Composite spacer = SWTFactory.createComposite(group, 1, 1, GridData.FILL_HORIZONTAL);

		disableConflictingShortcutsField = new BooleanFieldEditor(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS,
				"&Disable Eclipse's shortcuts when Vrapper is enabled", spacer);
		addField(disableConflictingShortcutsField);

		// CREATES CONFLICTS TABLE
		horizontalLayoutData.horizontalAlignment = SWT.FILL;
		Composite conflictsArea = new Composite(spacer, SWT.NONE);
		conflictsArea.setLayout(new GridLayout(3, true));
		conflictsArea.setLayoutData(horizontalLayoutData);

		Composite tableArea = new Composite(conflictsArea, SWT.NONE);
		tableArea.setLayout(new GridLayout(1, true));
		tableArea.setLayoutData(verticalLayoutData);

		Label descriptionLabel = new Label(tableArea, SWT.NONE);
		descriptionLabel.setText("Shortcuts");

		conflictViewer = new TableViewer(tableArea, SWT.SINGLE | SWT.V_SCROLL | SWT.BORDER);
		conflictTable = conflictViewer.getTable();

		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 200;
		conflictTable.setLayoutData(layoutData);

		conflictViewer.add(tableConflicts.getSorted());

		Composite buttonsArea = new Composite(conflictsArea, SWT.NONE);
		buttonsArea.setLayout(new GridLayout(1, true));
		buttonsArea.setLayoutData(verticalLayoutData);

		addButton = new Button(buttonsArea, SWT.PUSH);
		addButton.setText("Add...");
		addButton.addSelectionListener(new AddShortcut());
		setButtonLayoutData(addButton);

		removeButton = new Button(buttonsArea, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.addSelectionListener(new RemoveShortcut());
		setButtonLayoutData(removeButton);

		updateConflictUI(getPreferenceStore().getBoolean(PreferenceConstants.P_DISABLE_UNWANTED_CONFLICTS));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() == disableConflictingShortcutsField)
			updateConflictUI((Boolean) event.getNewValue());
	}

	private void updateConflictUI(Boolean enabled) {
		conflictTable.setEnabled(enabled);
		addButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	class AddShortcut extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			InputDialog dlg = new KeySequenceInput("Add conflicting shortcut", bindingService);
			if (dlg.open() == Window.OK && dlg.getValue() != null) {
				conflictViewer.remove(tableConflicts.getSorted());
				tableConflicts.add(dlg.getValue());
				conflictViewer.add(tableConflicts.getSorted());
			}
		}

	}

	class RemoveShortcut extends SelectionAdapter {

		@Override
		public void widgetSelected(SelectionEvent e) {
			if (!conflictViewer.getSelection().isEmpty()) {
				String removed = ((IStructuredSelection) conflictViewer.getSelection()).getFirstElement().toString();
				tableConflicts.remove(removed);
				conflictViewer.remove(removed);
			}
		}

	}

	class TableConflicts {

		Set<String> conflicts;

		public TableConflicts() {
			conflicts = new HashSet<String>();
			conflicts.clear();
			conflicts.addAll(Arrays.asList(getPreferenceStore().getString(PreferenceConstants.P_UNWANTED_CONFLICTS).split(":")));
		}

		public void performDefaults() {
			conflictViewer.remove(getSorted());
			conflicts.clear();
			conflicts.addAll(Arrays.asList(getPreferenceStore().getDefaultString(PreferenceConstants.P_UNWANTED_CONFLICTS).split(":")));
			conflictViewer.add(getSorted());
		}

		void performOk() {
			StringBuilder commaSeparated = new StringBuilder();
			for (String shortcut : conflicts) {
				commaSeparated.append(shortcut);
				if (commaSeparated.length() > 0)
					commaSeparated.append(":");
			}

			getPreferenceStore().setValue(PreferenceConstants.P_UNWANTED_CONFLICTS, commaSeparated.toString());
		}

		void add(String shortcut) {
			conflicts.add(shortcut.toUpperCase());
		}

		void remove(String shortcut) {
			conflicts.remove(shortcut.toUpperCase());
		}

		Object[] getSorted() {
			ArrayList<String> sorted = new ArrayList<String>(conflicts);
			Collections.sort(sorted);
			return sorted.toArray();
		}

	}

}