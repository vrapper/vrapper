package net.sourceforge.vrapper.eclipse.platform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.vrapper.eclipse.ui.CaretUtils;
import net.sourceforge.vrapper.platform.Configuration;
import net.sourceforge.vrapper.platform.CursorService;
import net.sourceforge.vrapper.platform.SelectionService;
import net.sourceforge.vrapper.utils.CaretType;
import net.sourceforge.vrapper.utils.ContentType;
import net.sourceforge.vrapper.utils.LineInformation;
import net.sourceforge.vrapper.utils.Position;
import net.sourceforge.vrapper.utils.Space;
import net.sourceforge.vrapper.utils.StartEndTextRange;
import net.sourceforge.vrapper.utils.VimUtils;
import net.sourceforge.vrapper.vim.commands.Selection;
import net.sourceforge.vrapper.vim.commands.SimpleSelection;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Caret;

public class EclipseCursorAndSelection implements CursorService, SelectionService {

    public static final String POSITION_CATEGORY_NAME = "net.sourceforge.vrapper.position";
    private final ITextViewer textViewer;
    private int stickyColumn;
    private boolean stickToEOL = false;
    private boolean isReversed = false;
    private boolean yankOperation = false;
    private final ITextViewerExtension5 converter;
    private Selection selection;
    private final SelectionChangeListener selectionChangeListener;
    private final StickyColumnUpdater caretListener;
    private final Map<String, org.eclipse.jface.text.Position> marks;
    private final List<org.eclipse.jface.text.Position> changeList;
    private int changeListIndex;
    private final Configuration configuration;
    private final EclipseTextContent textContent;

	public EclipseCursorAndSelection(Configuration configuration,
			ITextViewer textViewer, EclipseTextContent textContent) {
        this.configuration = configuration;
        this.textViewer = textViewer;
        this.textContent = textContent;
        converter = OffsetConverter.create(textViewer);
        selectionChangeListener = new SelectionChangeListener();
        caretListener = new StickyColumnUpdater();
        marks = new HashMap<String, org.eclipse.jface.text.Position>();
        changeList = new ArrayList<org.eclipse.jface.text.Position>();
        textViewer.getTextWidget().addSelectionListener(selectionChangeListener);
        textViewer.getTextWidget().addCaretListener(caretListener);
        textViewer.getDocument().addPositionCategory(POSITION_CATEGORY_NAME);
    }

    public Position getPosition() {
        return new TextViewerPosition(textViewer, Space.VIEW, textViewer.getTextWidget().getCaretOffset());
    }

    public void setPosition(Position position, boolean updateColumn) {
    	if (!updateColumn) {
	    	caretListener.disable();
    	}
    	int viewOffset = position.getViewOffset();
    	if(viewOffset == -1) {
    		//Something went screwy, avoid getting into a bad state.
    		//Just put the cursor at offset 0.
    		viewOffset = 0;
    	}
        textViewer.getTextWidget().setSelection(viewOffset);
        caretListener.enable();
    }

    public Position stickyColumnAtViewLine(int lineNo) {
        // FIXME: do this properly
        StyledText tw = textViewer.getTextWidget();
        if (!stickToEOL) {
            try {
                int y = tw.getLocationAtOffset(tw.getOffsetAtLine(lineNo)).y;
                int offset = tw.getOffsetAtLocation(new Point(stickyColumn, y));
                return new TextViewerPosition(textViewer, Space.VIEW, offset);
            } catch (IllegalArgumentException e) {
                // fall through silently and return line end
            }
        }
        try {
            int line = converter.widgetLine2ModelLine(lineNo);
            int lineLen = textViewer.getDocument().getLineLength(line);
            String nl = textViewer.getDocument().getLineDelimiter(line);
            int nlLen = nl != null ? nl.length() : 0;
            int offset = tw.getOffsetAtLine(lineNo) + lineLen - nlLen;
            return new TextViewerPosition(textViewer, Space.VIEW, offset);
        } catch (BadLocationException e) {
            throw new RuntimeException(e);
        }
    }

    public Position stickyColumnAtModelLine(int lineNo) {
        // FIXME: do this properly
        if (stickToEOL) {
            try {
                int lineLength = textViewer.getDocument().getLineLength(lineNo);
            	//getLineLength includes the line's delimiter
            	//we need to find the last non-delimiter character
                int startOffset = textViewer.getDocument().getLineInformation(lineNo).getOffset();
                int endOffset = startOffset + lineLength;
                //don't leave the cursor on a newline
                if(lineLength > 0) {
                	endOffset--;
                }
                //check for multi-byte (windows) line-endings (\r\n)
                if(endOffset > startOffset && VimUtils.isNewLine(textContent.getModelContent().getText(endOffset, 1))) {
                	endOffset--;
                }
                return new TextViewerPosition(textViewer, Space.MODEL, endOffset);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return stickyColumnAtViewLine(converter.modelLine2WidgetLine(lineNo));
            } catch (RuntimeException e) {
                try {
                    int caretOffset = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
                    int lineOffset = textViewer.getDocument().getLineInformationOfOffset(caretOffset).getOffset();
                    int y = Math.abs(caretOffset - lineOffset);
                    IRegion line = textViewer.getDocument().getLineInformation(lineNo);
                    int offset = line.getOffset() + Math.min(y, line.getLength());
                    return new TextViewerPosition(textViewer, Space.MODEL, offset);
                } catch (BadLocationException e1) {
                    throw new RuntimeException(e1);
                }
            }
        }
    }

    public Selection getSelection() {
        if (selection != null) {
            return selection;
        }
        int start, end, pos, len;
        start = end = textViewer.getSelectedRange().x;
        len = textViewer.getSelectedRange().y;
        pos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            if (start == pos) {
                start += len+1;
            } else {
                end += len;
            }


        Position from = new TextViewerPosition(textViewer, Space.MODEL, start);
        Position to =   new TextViewerPosition(textViewer, Space.MODEL, end);
        return new SimpleSelection(new StartEndTextRange(from, to));
    }

    public void setSelection(Selection newSelection) {
        setSelection(newSelection, false);
    }
    
    public void setSelection(Selection newSelection, boolean leaveVisualMode) {
        if (newSelection == null) {
            int cursorPos = converter.widgetOffset2ModelOffset(textViewer.getTextWidget().getCaretOffset());
            // Back up one if we are leaving visual mode
            // This is to compensate for the emulated block cursor vs. Eclipse's line cursor -- BRD
            if(leaveVisualMode && !isReversed && !yankOperation)
                --cursorPos;
           
            if(yankOperation)
                yankOperation = false;
            
            
            textViewer.setSelectedRange(cursorPos, 0);
            selection = null;
        } else {
            textViewer.getTextWidget().setCaretOffset(newSelection.getStart().getViewOffset());
            int from = newSelection.getStart().getModelOffset();
            int length = !newSelection.isReversed() ? newSelection.getModelLength() : -newSelection.getModelLength();
            if(length < 0)
                isReversed = true;
            else
                isReversed = false;
            // linewise selection includes final newline, this means the cursor
            // is placed in the line below the selection by eclipse. this
            // corrects that behaviour
            if (ContentType.LINES.equals(newSelection.getContentType(configuration))) {
                if (newSelection.isReversed()) {
                    from -= 1;
                    length += 1;
                } else {
                    length -=1;
                }
            }
            selection = newSelection;
            selectionChangeListener.disable();
            textViewer.setSelectedRange(from, length);
            selectionChangeListener.enable();
        }
    }

    public Position newPositionForModelOffset(int offset) {
        return new TextViewerPosition(textViewer, Space.MODEL, offset);
    }

    public Position newPositionForViewOffset(int offset) {
        return new TextViewerPosition(textViewer, Space.VIEW, offset);
    }

    public void setCaret(CaretType caretType) {
        StyledText styledText = textViewer.getTextWidget();
        Caret old = styledText.getCaret();
        styledText.setCaret(CaretUtils.createCaret(caretType, styledText));
        // old caret is not disposed automatically
        old.dispose();
    }

    public void stickToEOL() {
        stickToEOL = true;
    }

    private final class SelectionChangeListener implements SelectionListener {
        boolean enabled = true;
        public void widgetDefaultSelected(SelectionEvent arg0) {
            if (enabled) {
                selection = null;
            }
        }

        public void widgetSelected(SelectionEvent arg0) {
            if (enabled) {
                selection = null;
            }
        }

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }
    }
    
    private final class StickyColumnUpdater implements CaretListener {
    	
        boolean enabled = true;
        
		public void caretMoved(CaretEvent e) {
			if (enabled) {
				int offset = e.caretOffset;
	            stickyColumn = textViewer.getTextWidget().getLocationAtOffset(offset).x;
	            //if the desired stickyColumn is off the screen to the left
	            //(horizontal scrollbars are scrolled to the right) then I get a
	            //negative number here, which throws an IllegalArgumentException
	            //later.  You would think '0' would be the best fallback value but
	            //that throws an IllegalArgumentException too.  For some reason,
	            //'2' is the 'x' value of the first column.
	            stickyColumn = Math.max(2, stickyColumn);
	            
				// if the user clicks to the right of the line end
				// (i.e. the newline is selected) stick to EOL
				LineInformation line = textContent.getViewContent().getLineInformationOfOffset(offset);
	            stickToEOL = offset >= line.getEndOffset();
			}
		}

        public void enable() {
            enabled = true;
        }

        public void disable() {
            enabled = false;
        }
    	
    }

    public void setMark(String id, Position position) {
        org.eclipse.jface.text.Position p = new org.eclipse.jface.text.Position(position.getModelOffset());
        try {
        	//add listener so Position automatically updates as the document changes
			textViewer.getDocument().addPosition(p);
		} catch (BadLocationException e) {
			e.printStackTrace();
			return;
		}
        
        if(id == LAST_EDIT_MARK) {
        	changeList.add(p);
        	if(changeList.size() > 100) {
        		//remove (and stop tracking changes for) old positions
        		textViewer.getDocument().removePosition( changeList.remove(0) );
        	}
        	//new edit, restart index position
        	changeListIndex = changeList.size();
        }
        else if(marks.containsKey(id)) {
        	//we're about to overwrite an old position
        	//no need to track its changes anymore
        	textViewer.getDocument().removePosition( marks.get(id) );
        }
        
        //update mark position
        marks.put(id, p);
    }

    public Position getMark(String id) {
    	//`` and '' are the same position, so we need to return that position
    	//regardless of which way the user accessed it
    	if(id.equals("`")) {
    		id = LAST_JUMP_MARK;
    	}
    	
        org.eclipse.jface.text.Position p = marks.get(id);
        if (p == null || p.isDeleted) {
        	//leave deleted entries in marks Map
        	//in case an 'undo' brings it back
            return null;
        }
        int offset = p.getOffset();
        return newPositionForModelOffset(offset);
    }
    
    public Position getNextChangeLocation(int count) {
    	int index = changeListIndex + count;
    	return getChangeLocation(index);
    }
    
    public Position getPrevChangeLocation(int count) {
    	int index = changeListIndex - count;
    	return getChangeLocation(index);
    }
    
    private Position getChangeLocation(int index) {
    	if(changeList.size() == 0) {
    		return null;
    	}
    	
    	if(index < 0) {
    		index = 0;
    	}
    	else if(index >= changeList.size()) {
    		index = changeList.size() -1;
    	}
    	
    	org.eclipse.jface.text.Position p = changeList.get(index);
    	if(p == null || p.isDeleted) {
    		changeList.remove(index);
    		changeListIndex = changeList.size();
    		if(p != null) { //deleted
    			textViewer.getDocument().removePosition(p);
    		}
    		return null;
    	}
    	else {
    		changeListIndex = index; //prepare for next invocation
    		return newPositionForModelOffset(p.getOffset());
    	}
    }
    
    public void setYankOperation(boolean yankOperation) {
        this.yankOperation = yankOperation;
    }

}
