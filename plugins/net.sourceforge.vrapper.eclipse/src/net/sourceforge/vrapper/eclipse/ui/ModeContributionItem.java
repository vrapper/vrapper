package net.sourceforge.vrapper.eclipse.ui;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.StatusLineLayoutData;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import net.sourceforge.vrapper.platform.UserInterfaceService;

public class ModeContributionItem extends ContributionItem {

    private static final String DEFAULT_MESSAGE = UserInterfaceService.VRAPPER_DISABLED;
    private static final String RECORDING_MESSAGE = "recording: ";
    // Disabled message is treated as longest possible string.
    private static final int LABEL_MARGIN = 3;
    private String mode = "";
    private boolean isRecording;
    private String recMacro;
    private CLabel recordingText;
    private CLabel modeText;

    public ModeContributionItem(String id) {
        super(id);
    }

    @Override
    public void fill(Composite parent) {

        Font boldFont = JFaceResources.getFontRegistry().getBold("");
        Font regularFont = JFaceResources.getFontRegistry().defaultFont();

        // Calculate height and width based on current above fonts.
        int fontHeight;
        int modeLabelWidth;
        int recordingLabelWidth;
        GC gc = null;
        try {
            gc = new GC(parent);
            gc.setFont(regularFont);
            fontHeight = gc.getFontMetrics().getHeight();
            modeLabelWidth = gc.textExtent(DEFAULT_MESSAGE).x + LABEL_MARGIN * 2;
            gc.setFont(boldFont);
            recordingLabelWidth = gc.textExtent(RECORDING_MESSAGE + "@").x + LABEL_MARGIN * 2;
        } finally {
            if (gc != null) {
                gc.dispose();
            }
        }

        StatusLineLayoutData layoutData;

        //Note: attentive readers might remark that no dispose is called on the following widgets.
        // The status bar manager actually disposes all child widgets of the parent Composite before
        // calling this fill method again.

        Label sep = new Label(parent, SWT.SEPARATOR);
        layoutData = new StatusLineLayoutData();
        layoutData.heightHint = fontHeight;
        sep.setLayoutData(layoutData);

        if (isRecording) {
            recordingText = new org.eclipse.swt.custom.CLabel(parent, SWT.SHADOW_NONE | SWT.CENTER);
            recordingText.setText(getRecordingLabelText());
            recordingText.setVisible(isRecording);
            recordingText.setFont(boldFont);
            layoutData = new StatusLineLayoutData();
            layoutData.widthHint = recordingLabelWidth;
            recordingText.setLayoutData(layoutData);
        }
        // Setting the CSS class is not enough for CSS to apply because it needs to be refreshed.
        // Since that gives an unwanted dependency on Eclipse 4.x we fool the Class.getSimpleName()
        // detection method by defining our own custom CLabel class.
        modeText = new net.sourceforge.vrapper.eclipse.ui.CLabel(parent, SWT.SHADOW_NONE | SWT.CENTER);
        //modeText.setData("org.eclipse.e4.ui.css.CssClassName", "CLabel org.eclipse.swt.custom.CLabel ModeLabel");
        modeText.setText(mode);
        modeText.setFont(regularFont);
        StatusLineLayoutData modeLayoutData = new StatusLineLayoutData();
        modeLayoutData.widthHint = modeLabelWidth;
        modeText.setLayoutData(modeLayoutData);
    }

    private String getRecordingLabelText() {
        return RECORDING_MESSAGE + recMacro;
    }

    public void setText(String text) {
        mode = text;
        if (modeText != null && ! modeText.isDisposed()) {
            modeText.setText(text);
        }
    }

    public void setRecording(boolean b, String m) {
        isRecording = b;
        recMacro = m;
        if (recordingText != null && ! recordingText.isDisposed()) {
            recordingText.setText(getRecordingLabelText());
            recordingText.setVisible(isRecording);
        }
        if (getParent() != null) {
            // Add or remove recording label widget by forcing status bar refresh.
            getParent().update(true);
        }
    }
}
