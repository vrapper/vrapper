package net.sourceforge.vrapper.eclipse.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ModeContributionItem extends ContributionItem {
    
    private static final String RECORDING_MESSAGE = "recording: ";
    private static final int MODEMESSAGE_WIDTH = 50;
    private String mode = "";
    private boolean isRecording;
    private String recMacro;
    
    public ModeContributionItem(String id) {
        super(id);
    }

    @Override
    public void fill(Composite parent) {
        Composite reservedSpace = new Composite(parent, SWT.NO_FOCUS);
        reservedSpace.setBackground(parent.getBackground());
        GridLayout layout = new GridLayout();
        layout.horizontalSpacing = 50;
        layout.numColumns = 2;
        reservedSpace.setLayout(layout);
        
        Label recordingText = new Label(reservedSpace, SWT.CENTER);
        recordingText.setText(RECORDING_MESSAGE + recMacro);
        recordingText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
        recordingText.setBackground(parent.getBackground());
        recordingText.setVisible(isRecording);
        Font boldFont = recordingText.getFont();
        FontData fontData[] = boldFont.getFontData();
        for (FontData fd : fontData) {
            fd.setStyle(SWT.BOLD);
        }
        recordingText.setFont(new Font(boldFont.getDevice(), fontData));
        
        Label modeText = new Label(reservedSpace, SWT.NONE);
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

    public void setRecording(boolean b, String m) {
        isRecording = b;
        recMacro = m;
        super.getParent().update(true);
    }
}
