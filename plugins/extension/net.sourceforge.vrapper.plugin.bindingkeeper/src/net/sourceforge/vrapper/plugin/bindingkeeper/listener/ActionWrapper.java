package net.sourceforge.vrapper.plugin.bindingkeeper.listener;

import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Event;

/**
 * @author Pedro Santos
 *
 */
public class ActionWrapper implements IAction {

	private IAction action;
	private IExecutionListener executionListener;

	public ActionWrapper(IAction action) {
		this.action = action;
	}

	public void setExecutionListener(IExecutionListener executionListener) {
		this.executionListener = executionListener;
	}

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		action.addPropertyChangeListener(listener);
	}

	public int getAccelerator() {
		return action.getAccelerator();
	}

	public String getActionDefinitionId() {
		return action.getActionDefinitionId();
	}

	public String getDescription() {
		return action.getDescription();
	}

	public ImageDescriptor getDisabledImageDescriptor() {
		return action.getDisabledImageDescriptor();
	}

	public HelpListener getHelpListener() {
		return action.getHelpListener();
	}

	public ImageDescriptor getHoverImageDescriptor() {
		return action.getHoverImageDescriptor();
	}

	public String getId() {
		return action.getId();
	}

	public ImageDescriptor getImageDescriptor() {
		return action.getImageDescriptor();
	}

	public IMenuCreator getMenuCreator() {
		return action.getMenuCreator();
	}

	public int getStyle() {
		return action.getStyle();
	}

	public String getText() {
		return action.getText();
	}

	public String getToolTipText() {
		return action.getToolTipText();
	}

	public boolean isChecked() {
		return action.isChecked();
	}

	public boolean isEnabled() {
		return action.isEnabled();
	}

	public boolean isHandled() {
		return action.isHandled();
	}

	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		action.removePropertyChangeListener(listener);
	}

	public void run() {
		executionListener.preExecute(action.getId(), null);
		try {
			action.run();
		} catch (RuntimeException e) {
			executionListener.postExecuteFailure(action.getId(), null);
			throw e;
		}
		executionListener.postExecuteSuccess(action.getId(), null);
	}

	public void runWithEvent(Event event) {
		executionListener.preExecute(action.getId(), null);
		try {
			action.runWithEvent(event);
		} catch (RuntimeException e) {
			executionListener.postExecuteFailure(action.getId(), null);
			throw e;
		}
		executionListener.postExecuteSuccess(action.getId(), null);
	}

	public void setActionDefinitionId(String id) {
		action.setActionDefinitionId(id);
	}

	public void setChecked(boolean checked) {
		action.setChecked(checked);
	}

	public void setDescription(String text) {
		action.setDescription(text);
	}

	public void setDisabledImageDescriptor(ImageDescriptor newImage) {
		action.setDisabledImageDescriptor(newImage);
	}

	public void setEnabled(boolean enabled) {
		action.setEnabled(enabled);
	}

	public void setHelpListener(HelpListener listener) {
		action.setHelpListener(listener);
	}

	public void setHoverImageDescriptor(ImageDescriptor newImage) {
		action.setHoverImageDescriptor(newImage);
	}

	public void setId(String id) {
		action.setId(id);
	}

	public void setImageDescriptor(ImageDescriptor newImage) {
		action.setImageDescriptor(newImage);
	}

	public void setMenuCreator(IMenuCreator creator) {
		action.setMenuCreator(creator);
	}

	public void setText(String text) {
		action.setText(text);
	}

	public void setToolTipText(String text) {
		action.setToolTipText(text);
	}

	public void setAccelerator(int keycode) {
		action.setAccelerator(keycode);
	}
}
