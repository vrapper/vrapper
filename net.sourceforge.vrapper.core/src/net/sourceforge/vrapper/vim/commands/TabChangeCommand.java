package net.sourceforge.vrapper.vim.commands;

import net.sourceforge.vrapper.vim.EditorAdaptor;


/*
SWITCHING TO ANOTHER TAB PAGE:

Using the mouse: If the tab page line is displayed you can click in a tab page
label to switch to that tab page.  Click where there is no label to go to the
next tab page.  |'tabline'|

:tabn[ext]				*:tabn* *:tabnext* *gt*
<C-PageDown>				*CTRL-<PageDown>* *<C-PageDown>*
gt					*i_CTRL-<PageDown>* *i_<C-PageDown>*
		Go to the next tab page.  Wraps around from the last to the
		first one.

:tabn[ext] {count}
{count}<C-PageDown>
{count}gt	Go to tab page {count}.  The first tab page has number one.


:tabp[revious]				*:tabp* *:tabprevious* *gT* *:tabN*
:tabN[ext]				*:tabNext* *CTRL-<PageUp>*
<C-PageUp>			 *<C-PageUp>* *i_CTRL-<PageUp>* *i_<C-PageUp>*
gT		Go to the previous tab page.  Wraps around from the first one
		to the last one.

:tabp[revious] {count}
:tabN[ext] {count}
{count}<C-PageUp>
{count}gT	Go {count} tab pages back.  Wraps around from the first one
		to the last one.

:tabr[ewind]			*:tabfir* *:tabfirst* *:tabr* *:tabrewind*
:tabfir[st]	Go to the first tab page.

							*:tabl* *:tablast*
:tabl[ast]	Go to the last tab page.


Other commands:
							*:tabs*
:tabs		List the tab pages and the windows they contain.
		Shows a ">" for the current window.
		Shows a "+" for modified buffers.

							NOTE: Would probably show the "ctrl-e" menu in Eclipse -- BRD

REORDERING TAB PAGES:

:tabm[ove] [N]						*:tabm* *:tabmove*
		Move the current tab page to after tab page N.  Use zero to
		make the current tab page the first one.  Without N the tab
		page is made the last one.


LOOPING OVER TAB PAGES:

							*:tabd* *:tabdo*
:tabd[o] {cmd}	Execute {cmd} in each tab page.
		It works like doing this: >
			:tabfirst
			:{cmd}
			:tabnext
			:{cmd}
			etc.
<		This only operates in the current window of each tab page.
		When an error is detected on one tab page, further tab pages
		will not be visited.
		The last tab page (or where an error occurred) becomes the
		current tab page.
		{cmd} can contain '|' to concatenate several commands.
		{cmd} must not open or close tab pages or reorder them.
		{not in Vi} {not available when compiled without the
		|+listcmds| feature}
		Also see |:windo|, |:argdo| and |:bufdo|.
*/
public class TabChangeCommand extends CountIgnoringNonRepeatableCommand {
	private static enum Options {
	    TAB_NEXT,
	    TAB_PREVIOUS,
	    TAB_FIRST,
	    TAB_LAST,
	    LIST_TABS,
	    TAB_MOVE,
	    TAB_DO
	}
	
	private static final String TAB_NEXT   	 = "tabnext";
	private static final String TAB_PREVIOUS = "tabprevious";
	private static final String TAB_FIRST    = "tabfirst";
	private static final String TAB_LAST     = "tablast";
	private static final String LIST_TABS    = "listtabs";
	private static final String TAB_MOVE     = "tabmove";
	private static final String TAB_DO       = "tabdo";
	
	private static final String OPTIONS = TAB_NEXT
										+ ","
										+ TAB_PREVIOUS
										+ ","
										+ TAB_FIRST
										+ ","
										+ TAB_LAST
										+ ","
										+ LIST_TABS
										+ ","
										+ TAB_MOVE
										+ ","
										+ TAB_DO;
	
    private boolean tabNext = false;
    private boolean tabPrevious = false;
    private boolean tabFirst = false;
    private boolean tabLast = false;
    private boolean listTabs = false;
    private boolean tabMove = false;
    private boolean tabDo = false;
    

	public void execute(EditorAdaptor editorAdaptor) throws CommandExecutionException {
		
	}
}
