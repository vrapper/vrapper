package net.sourceforge.vrapper.eclipse.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ModeContributionItem extends ContributionItem {
    
    private static final String RECORDING_MESSAGE = "recording";
    private static final int MODEMESSAGE_WIDTH = 50;
    private String mode = "";
    private boolean isRecording;
    
    public ModeContributionItem(String id) {
        super(id);
    }

    @Override
    public void fill(Composite parent) {
        Composite reservedSpace = new Composite(parent, SWT.NONE);
        reservedSpace.setBackground(parent.getBackground());
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 50;
        layout.numColumns = 2;
        reservedSpace.setLayout(layout);
        
        Text recordingText = new Text(reservedSpace, SWT.BOLD);
        recordingText.setText(RECORDING_MESSAGE);
        recordingText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        recordingText.setBackground(parent.getBackground());
        recordingText.setVisible(isRecording);
        Font boldFont = recordingText.getFont();
        for (FontData fontData : boldFont.getFontData()) {
            fontData.setStyle(SWT.BOLD);
        }
        recordingText.setFont(boldFont);
        
        Text modeText = new Text(reservedSpace, SWT.NONE);
        GridData modeLayoutData = new GridData(SWT.RIGHT, SWT.CENTER, true, false);
        modeLayoutData.minimumWidth = MODEMESSAGE_WIDTH;
        modeText.setLayoutData(modeLayoutData);
        modeText.setBackground(parent.getBackground());
        modeText.setText(mode);
        
        reservedSpace.layout();
    }
    
    public void setText(String text) {
        mode = text;
        super.getParent().update(true);
    }

    public void setRecording(boolean b) {
    	isRecording = b;
        super.getParent().update(true);
    }
}
