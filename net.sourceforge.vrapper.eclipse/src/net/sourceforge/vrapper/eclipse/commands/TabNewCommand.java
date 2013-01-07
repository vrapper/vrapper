package net.sourceforge.vrapper.eclipse.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;

/**
 * :[count]tabe[dit]                *:tabe* *:tabedit* *:tabnew*
 * :[count]tabnew
 *      Open a new tab page with an empty window, after the current
 *      tab page.  For [count] see |:tab| below.
 *       
 * :[count]tabe[dit] [++opt] [+cmd] {file}
 * :[count]tabnew [++opt] [+cmd] {file}
 *      Open a new tab page and edit {file}, like with |:edit|.
 *
 *      
 *  NOTE: Since we're opening a New Wizard, most of the options won't
 *        make much sense. Maybe it would be beneficial to accept an 
 *        argument that, instead of a filename, it takes a wizard type.
 *        Something to think about.
 * @author Brian Detweiler
 *
 */
public class TabNewCommand extends EclipseCommand {
   
    // Commands like this can be found in org.eclipse.ui/plugin.xml
    private static final String NEW_WIZARD = "org.eclipse.ui.newWizard";
    
    public static TabNewCommand NEW_EDITOR = new TabNewCommand(NEW_WIZARD);

    public TabNewCommand(String action) {
        super(action);
    }
    
    public void execute(EditorAdaptor editorAdaptor) {
        doIt(1, getCommandName(), editorAdaptor);
    }
}