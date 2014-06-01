<div class="newsbox">
    <div class="date">2014-06-01</div>
    <h4>0.44.0 Released</h4>
    
    <p>It seems we're still sticking with this 2-month release cycle.
    0.44.0 has been released and is primarily a bug-fix release with a ton
    of defects fixed.  In addition to last month's list of fixes, 0.44.0
    includes the following changes:</p>
    
    <ul>
        <li>Fixed XML tag 't' text object to ignore JSP tags</li>
        <li>Fixed issue where accidentally using XML tag 't' text object in a non-XML file would cause StackOverflowError</li>
        <li>Fixed issue with multi-character mappings affecting regular commands</li>
        <li>Fixed issue to keep cursor visible when swapping visual selection sides</li>
        <li>Fixed issue where named register was sometimes still active after an operation</li>
        <li>Modified behavior of new "syncmodifiable" property</li>
        <ul>
          <li>Now a string rather than a boolean, valid values are 'matchreadonly' and 'nosync'</li>
          <li>'matchreadonly' will set Vrapper's read-only flag to the state of Eclipse's read-only flag (this is the default)</li>
          </ul>
        <li>Other miscellaneous improvements of fixes made last month</li>
    </ul>

    <p>Split Editor Plugin changes:</p>
    <ul>
        <li>Added support for ':split' commands in detached windows</li>
        <li>Fixed 'gt' behavior</li>
        <ul>
          <li>Added ':wincmd n/p' and '&lt;C-W&gt;gt/gT' (which can be mapped to 'gt/gT')</li>
        </ul>
        <li>Fixed issue where a cloned editor would place its cursor at the beginning of the file</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-05-04</div>
    <h4>Updates to unstable update site</h4>

    <p>Time for your monthly status update on Vrapper.  We've got another long
    list of changes in the last month since 0.42.0 was released.</p>
    
    <p>First of all, there was a defect in 0.42.0 where the '?' command (reverse
    search) simply didn't work. I was tempted to release a 0.42.1 with this
    defect fixed since it seems like such a fundamental feature. However, it
    actually took a couple weeks for anyone to find it.  This tells me that the
    '?' feature isn't actually used that often.  So we've fixed this defect on
    the unstable update site but I think it can probably wait for 0.44.0 to be
    in a stable release.  If you actually use the '?' feature a lot and noticed
    it was broken, you'll have to update to the latest unstable build or wait
    another month to receive the fix.  Sorry for breaking such a basic
    feature.</p>
    
    <p>On to the list of changes since 0.42.0 was released:</p>
    
    <ul>
        <li>Added 'N%' feature to jump to a percent of the file</li>
        <li>Add searches from '*' and '#' to search history</li>
        <li>Added support for searches with '\&lt;' and '\&gt;'</li>
        <ul>
           <li>Maps to Eclipse's '\b' regex; we're still using Java regex under the hood</li>
        </ul>
        <li>Fixed issue where '?' (reverse search) was broken</li>
        <li>Fixed issue with using counts before a register switch command</li>
        <li>Fixed issue with using counts before an omap'd command</li>
        <li>Fixed issue with using counts and InsertMode</li>
        <li>Fixed issue with using counts after a command and before a motion</li>
        <li>Fixed issue with 'w' not jumping over windows line-endings</li>
        <li>Fixed issue where commands that take arbitrary characters (f/F/t/T/m) were affected by omap mappings</li>
        <li>Fixed issue where 'scrolloffset' and 'scrolljump' were ignored when jumping to a mark</li>
        <li>Fixed issue where Vrapper appeared to modify read-only files</li>
        <ul>
        <li>Added 'modifiable' and 'syncmodifiable' to set 'modifiable' when Eclipse says the file is read-only</li>
        </ul>
        <li>Fixed issue where Vrapper would re-enable itself if Vrapper was disabled and text was selected (while 'visualmouse' option set)</li>
        <li>Fixed issue with cursor position in '(insert) VISUAL' mode</li>
        <li>Fixed issue with double-clicking to select a word in '(insert) VISUAL' mode</li>
        <li>Fixed issue with deleting too many pending characters during a mapping in InsertMode when unprintable key presses (Ctrl/Alt) were used </li>
        <li>Fixed long-standing annoyance where cursor would appear after the end of a line when clicking with the mouse</li>
        <li>Other various internal fixes (refactoring)</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-04-06</div>
    <h4>0.42.0 Released</h4>

    <p>It seems springtime brought some new life for Vrapper development after
    such a slow winter. 0.42.0 has been released and just look at the list of
    changes made in the last month (included in 0.42.0):</p>
    
    <ul>
        <li>Added new plugin for exchange.vim port</li>
        <ul>
            <li>based on <a href="https://github.com/tommcdo/vim-exchange">https://github.com/tommcdo/vim-exchange</a></li>
            <li>cx&lt;text object&gt; highlights a text object</li>
            <li>cx&lt;text object&gt; again swaps text object with highlighted text object</li>
        </ul>
        <li>Added support for 'gvimargs' property</li>
        <ul>
            <li>Defines args sent to 'gvimpath' for use by :vim command</li>
            <li>Uses placeholder strings {line}, {col}, and {file}</li>
            <li>Defaults to: +{line} -c normal zv{col}| -c set nobackup -f -n {file}</li>
        </ul>
        <li>Added support for 'ad hoc delimiter' m&lt;char&gt;</li>
        <ul>
            <li>Based on a Vim patch which has not yet been merged into Vim</li>
            <li><a href="https://groups.google.com/forum/#!searchin/vim_dev/im$20am/vim_dev/pZxLAAXxk0M/wdOgpOQ05Z8J">Google Groups discussion</a></li>
            <li>Use m&lt;char&gt; with i/a to match strings delimited by any arbitrary &lt;char&gt;</li>
            <li>For example, 'vim*' will select (in VisualMode) the text between two '*' characters</li>
            <li>**Modified methodtextobj.vim plugin to use 'f' rather than 'm' to avoid this collision**</li>
        </ul>
        <li>Added support for '(insert) VISUAL' mode</li>
        <ul>
            <li>Highlighting text while in InsertMode moves into '(insert) VISUAL' mode for a single visual operation, then returns to InsertMode</li>
        </ul>
        <li>Added support for ':let @&lt;char&gt;=' to set register contents via command-line</li>
        <ul>
            <li>Using ':let' ONLY supports setting registers right now</li>
        </ul>
        <li>Added ability to map '|' character using '&lt;BAR&gt;'</li>
        <li>Fixed issue with Vrapper not re-initializing when Eclipse re-uses editors</li>
        <ul>
            <li>For the "Close editors automatically" feature in Eclipse</li>
        </ul>
        <li>Fixed issue with :eclipseaction from command-line of an editor</li>
        <li>Fixed issue with :eclipseaction being defined statically for all editors</li>
        <li>Fixed issue with :surround command in Surround.vim plugin</li>
        <li>Fixed issue with mappings which use '&lt;' or '&gt;' character</li>
        <li>Fixed issue with search strings that end in a backslash</li>
        <li>Fixed issue where :t/:move/:copy were requiring a space after the command (which Vim doesn't require)</li>
        <li>Fixed issue with map and noremap not including omap</li>
        <li>Fixed issue with cursor position when exiting visual line mode</li>
        <li>Fixed issue swapping between beginning and end of a visual selection when Windows line-endings are present</li>
        <li>Fixed issue where having a visual selection, moving into command-line mode, then hitting &lt;ESC&gt; didn't clear the selection</li>
        <li>Fixed issue where executing a macro wasn't a single undo-able operation</li>
        <li>Fixed issue with visually selecting the last character in a file</li>
        <li>Fixed display issue when restoring a reversed selection</li>
        <li>Refactored how text objects are defined to better support plugins</li>
    </ul>
    
    <p>I occasionally like to include some shameless self-congratulation when
    releasing a new version.  I searched Twitter for 'Vrapper' and here are some
    of my favorite tweets about Vrapper.  Thank you for all the love!</p>
    <p>‏@RobertFischer: Installed Vrapper and rebooted Eclipse. The skies opened opened up,
    angels sang, a tear came to my eye. IDE Heaven.</p>
    <p>@bigtbigtbigt: Just discovered Vrapper for Eclipse after a long session of coding (I'm
    new to @EclipseFdn).  I am in heaven!</p>
    <p>@yottamoto: Just discovered the vrapper VI plugin for eclipse
    (http://vrapper.sourceforge.net/home/ ). Now, my day is made, And possibly,
    the rest of my days also.</p>
    <p>@NicolasBrailo: Vrapper: a real text editor for Eclipse</p>
    <p>@jmaicher: How could I have used eclipse without vrapper (vim-like editing
    in Eclipse)? Awesome tool, it just works! &lt;3</p>
    <p>@mojavelinux: OMG, my dev environment is complete! Just discovered
    vrapper, a *lightweight* vim mode for all #Eclipse editors</p>
</div>
<div class="newsbox">
    <div class="date">2014-03-02</div>
    <h4>Updates to unstable update site</h4>

    <p>It's been another slow month but I might as well give an update with
    the changes to the unstable update site since 0.40.0 released:</p>
    
    <ul>
        <li>Added support for '@:' to repeat last command-line command</li>
        <li>Added support for ':marks' command</li>
        <li>Added support for ':reg[isters]' command</li>
        <li>Fixed issue with 'gv' after modifying lines</li>
        <li>Fixed issue with visual mode editing at the end of a file</li>
    </ul>
    
</div>
<div class="newsbox">
    <div class="date">2014-02-08</div>
    <h4>0.40.0 Released</h4>

    <p>0.40.0 has been released.  This release is smaller than most but I'm
    keeping with my two-month release cycle one more time.  The following changes
    were added since the last status update and are included in 0.40.0:</p>

    <ul>
        <li>Created new optional plugin for clang-format</li>
        <li>Added support for ':y[ank] [x]' to yank into register</li>
        <li>Added support for 'iskeyword' property</li>
        <li>Added support for ':xa[ll]'</li>
        <li>Added support for 'startnormalmode' property</li>
        <ul>
            <li>When true, always start in NormalMode after changing tabs</li>
        </ul>
        <li>Fixed issue with ';' after 'T'</li>
        <li>Fixed \c and \C in substitution definitions</li>
        <li>Fixed issue where &lt;S-Space&gt; wasn't being recognized</li>
    </ul>
    
    <p>I'd like to thank all the contributors who continue keeping this project
    going even when I'm too busy to spend time with it myself.  Your dedication
    helps keep this project alive!</p>
</div>
<div class="newsbox">
    <div class="date">2014-01-05</div>
    <h4>Updates to unstable update site</h4>

    <p>As I mentioned in my last post, real life has been getting in the way of
    me spending time with Vrapper.  The holidays certainly didn't help things
    either. This was a slow month for Vrapper but here's your monthly status
    update.  The following changes have been made to the unstable update
    site:</p>

    <ul>
        <li>Added support for 'omap' commands</li>
        <li>Added support for :tabs command</li>
        <li>Added support for '[[', ']]', '[]', and ']['</li>
        <li>Fixed issue with 'O' near a blank line with Windows line-endings</li>
        <li>Fixed issue using counts with mapped keys</li>
    </ul>
    
    <p>I'll probably still release 0.40.0 in a month but after that I might relax my
    two-month release cycle to be whenever a release is worthwhile.</p>
</div>
<div class="newsbox">
    <div class="date">2013-12-08</div>
    <h4>0.38.0 Released</h4>

    <p>Still sticking with our two-month release cycle, 0.38.0 is now released.
    Changes since the last status update (included in 0.38.0) are:</p>

    <ul>
        <li>Added :set contentassistmode</li>
        <ul>
            <li>Enables &lt;c-n&gt; and &lt;c-p&gt; in content-assist dropdown</li>
            <li>Use camap to map &lt;c-n&gt; or &lt;c-p&gt; to other keys</li>
        </ul>
        <li>Added :startinsert command</li>
        <li>Added 'method text object' optional plugin</li>
        <li>Fixed issue with chaining vi{i{i{</li>
        <li>Fixed autocmd when changing tabs</li>
        <li>Fixed v%</li>
        <li>Fixed 'dw' on last word of a line with Windows line endings</li>
        <li>Fixed Alt key handling on keyboards with AltGr</li>
        <li>Fixed issue with newlines when pasting text</li>
    </ul>
    
    <p>Real life has been getting in the way recently, preventing me from
    spending time with Vrapper.  I'll do my best to stay on top of defects and
    feature requests but I'm afraid I might not be able to continue releasing
    every two months.  Or at least, the amount of changes in a given two-month
    period might not be enough to justify a release.  We'll see how it goes.</p>
</div>
<div class="newsbox">
    <div class="date">2013-11-04</div>
    <h4>Updates to unstable update site</h4>

    <p>It's time for your monthly status update on Vrapper.  Here are the
    current changes on the unstable update site.  Please feel free to install
    the latest build on the unstable update site and let us know if you find any
    defects.</p>

    <ul>
        <li>Fixed issue where escaped characters were being skipped with t/f motions</li>
        <li>Fixed issue with visual block mode starting on an empty line</li>
        <li>Fixed autocmd on multi-page editors (like XML Editor)</li>
        <li>Fixed Ctrl+I in NormalMode (and added TAB in NormalMode for the same operation)</li>
        <li>Added eclipseuiaction for mapping any Eclipse operation that pops up a dialog</li>
        <li>Added support for [m, ]m, [M, and ]M</li>
        <li>Added support for :setlocal command</li>
        <li>Added support for shiftround setting</li>
    </ul>

    <b>**Changes in behavior**</b><br/>
    The following changes modify existing behavior from previous versions of Vrapper:
    <ul>
        <li>Refactored &gt;&gt; and &lt;&lt; to use shiftwidth, tabstop, expandtab, and shiftround settings rather than using Eclipse's settings</li>
        <li>Enable regexsearch property by default.  This matches Vim behavior more closely.</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2013-10-05</div>
    <h4>0.36.0 Released</h4>

    <p>In keeping with our two-month release cycle, I have released Vrapper version 0.36.0.
    There weren't too many changes from last month and they were almost entirely bug fixes.
    The changes since the last status update (included in 0.36.0) are:</p>

    <ul>
        <li>Fixed repetition of delimited text</li>
        <li>Fixed replace mode in macros</li>
        <li>Fixed Ctrl-H (for backspace) in InsertMode</li>
        <li>Fixed '\t' in substitution replace</li>
        <li>Fixed numpad 'return' mapping to 'enter'</li>
        <li>Fixed incsearch</li>
        <li>Added support for C pre-processor conditionals with '%'</li>
        <ul>
            <li>#if, #ifdef, #else, #elif, #endif</li>
        </ul>
    </ul>
    
    <p>If you run into any issues with this release, please file an issue on our GitHub project:<br/>
    <a href="https://github.com/vrapper/vrapper/issues">https://github.com/vrapper/vrapper/issues</a><br/>
    I'm not sure how much testing our unstable update site really gets since we had to rush out a 0.34.1
    with the last release.  I'm hoping that doesn't happen again but Vrapper just gets more features
    and not everything is covered by our unit tests.  Fortunately we have a constantly growing userbase
    that helps us find those obscure defects.  Thank you for all your support and keeping Vrapper alive!</p>
    
</div>
<div class="newsbox">
    <div class="date">2013-09-02</div>
    <h4>Updates to unstable update site</h4>

    <p>Time for another monthly status update on Vrapper.  Our last release
    added a lot of new features so this last month has been mostly defect
    fixing.  The occasional new feature or two has been added though.  The
    unstable update site currently includes the following changes from
    0.34.1:</p>

    <ul>
        <li>Fixed issue with 'S', 'cc', 'Vs', and 'Vc' on the last line of a file</li>
        <li>Fixed issue where ':source' couldn't handle absolute paths</li>
        <li>Fixed ':eclipseaction!'</li>
        <li>Fixed Ctrl-R in command-line mode</li>
        <li>Fixed issue where substitution 'c' confirm would match on a replace</li>
        <li>Fixed issue with 'i_ctrl-o' followed by ':&lt;command&gt;'</li>
        <li>Fixed visual block selection on the last line of a file</li>
        <li>Fixed visual block selection with horizontally scrolled view</li>
        <li>Added support for 'i_ctrl-t' and 'i_ctrl-d'</li>
        <li>Added support for 'g~'</li>
        <li>Added support for '!' in NormalMode</li>
        <li>Added support for pipe '|' to chain commands</li>
        <li>Added support for ':move' and ':copy' without a line range definition</li>
        <li>Added support for `:split &lt;filename&gt;` in Split Editor plugin</li>
        <li>Refactored sticky column handling</li>
        <li>Refactored newline handling when pasting text</li>
        <li>Refactored function keys (F1-F20) mapping</li>
        <li>Refactored key handling of certain Ctrl keys</li>
    </ul>
    
    <p>There are still plenty more defects and feature requests to address but
    I'm constantly amazed by how much we get done each month.  I guess I'm just
    shocked that a Vim plugin for Eclipse requires monthly status updates and a
    2-month release cycle.  But, it seems we're doing something right, we've had
    over 1,500 installs via the Eclipse Marketplace for two months in a row!</p>
    
</div>
<div class="newsbox">
    <div class="date">2013-08-10</div>
    <h4>0.34.1 Released</h4>

    <p>I released 0.34.0 last week and a number of people found defects.  One defect
    in particular (where the cursor would disappear on Linux systems) was major
    enough to warrant a new release.  I've released 0.34.1 with that defect fixed along
    with a couple others.  Changes between 0.34.0 and 0.34.1 are:</p>

    <ul>
        <li>Fixed issue on Linux systems where cursor would disappear during horizontal movements</li>
        <li>Fixed issue where &lt;C-[&gt; was ignored</li>
        <li>Fixed issue where :sort was ignoring selections</li>
        <li>Fixed 'last insert mark' at the end of a file</li>
        <li>Fixed HOME and END behavior</li>
    </ul>
    
    <p>I apologize to anyone that was frustrated by these defects after
    upgrading to 0.34.0. It seems the unstable update site isn't being tested as
    much as I'd hoped.  I may have to introduce some concept of a 'release
    candidate update site' which isn't updated as often as unstable but provides
    our users who don't want an "unstable" build to play with new builds before I
    declare them "stable".</p>
    
</div>
<div class="newsbox">
    <div class="date">2013-08-04</div>
    <h4>0.34.0 Released</h4>
    <p>As I mentioned in my previous post, this is a big release for us.  This release
    adds support for the three most-requested features in Vrapper:</p>
    <ul>
    	<li>Split Editor commands (:split, :vplit) via optional plugin</li>
    	<li>Visual Block Mode</li>
    	<li>Confirm 'c' flag in substitutions</li>
    </ul>

    <p>There are plenty of other feature requests we can work on but those are
    the big three that I've heard from the most people.  Visual Block Mode still
    has some quirks moving beyond line endings and dealing with tabs but it
    should be usable at this point.  In addition to those three features,
    here are the following changes since the last post (included in 0.34.0):</p>
    
	<ul>
		<li>Added 'cleanindent' option to control whether auto-indent introduced with 'o'
		should be cleaned up if no text is entered on the new line</li>
		<li>Added support for '\=@<b>x</b>' in substitutions to replace with register contents</li>
		<li>Added support for 'vS' and 'vgS' in Surround Plugin</li>
		<li>Fixed behavior with AltGr on German keyboards</li>
		<li>Fixed issue with using arrow keys in mappings</li>
		<li>Other miscellaneous defect fixes</li>
	</ul>
	
	<p>I hope everyone continues to enjoy Vrapper, thank you for all your support!</p>
</div>
<div class="newsbox">
    <div class="date">2013-06-29</div>
    <h4>Updates to unstable update site</h4>
    <p>It's time for your monthly status update on Vrapper.  When I released
    0.32.0 last month I mentioned that I received a lot of help from
    contributors who really knew what they were doing (way more than I do). 
    Those same contributors have been extremely productive this month to the
    point that I feel like I've been relegated to a Project Manager position.
    Just look at what they've accomplished in this last month:</p>
    
	<ul>
		<li>Add support for 'gv' and 'gi' commands</li>
		<li>Add support for marks: '< '> '[ '] '^</li>
		<li>Add support for global marks (A-Z and 0-9)</li>
		<li>Add support for Ctrl+O while in InsertMode</li>
		<ul>
			<li> Perform a single NormalMode command then return to InsertMode</li>
		</ul>
		<li>Add support for Ctrl+U in InsertMode</li>
		<li>Add support for Ctrl+X-Ctrl+L sentence completion in InsertMode</li>
		<li>Add support for Ctrl+U in CommandLineMode</li>
		<li>Add support for Ctrl+Y in CommandLineMode</li>
		<li>Add support for :! and :r! to execute a shell command</li>
		<li>Add support for line ranges when using eclipseaction commands</li>
		<li>Add support for parameters with eclipseaction commands</li>
		<ul>
			<li>eclipseaction sc org.eclipse.ui.views.showView(org.eclipse.ui.views.showView.viewId<br/>=org.eclipse.ui.console.ConsoleView)</li>
		</ul>
		<li>Add support for 'autocmd' in .vrapperrc for editor-specific bindings</li>
		<ul>
			<li>autocmd "C/C++ Editor" eclipseaction gv org.eclipse.cdt.ui.edit.text.c.select.last</li>
			<li>au "CMake Editor" nnoremap &lt;CR&gt; gf</li>
		</ul>
		<li>Add support for pasting to CommandLineMode with Ctrl+V
		<ul>
			<li>(*without* unbinding Paste from Eclipse!)</li>
		</ul>
		<li>Add configurable option 'exitlinkmode' to disable Vrapper's attempt to exit to NormalMode after Eclipse performs certain text operations</li>
		<li>Add support for custom surround definitions in our Surround Plugin</li>
		<li>Introduce new Split Editor Plugin</li>
		<ul>
			<li>:split, :vsplit, :wincmd, Ctrl+w commands, etc.</li>
			<li>Requires Eclipse 4 (sorry Eclipse 3.x users)</li>
		</ul>
		<li>Introduce new Plugin for port of argtextobj.vim</li>
		<ul>
			<li>Treats method arguments as text objects</li>
			<li><a href="http://www.vim.org/scripts/script.php?script_id=2699">http://www.vim.org/scripts/script.php?script_id=2699</a></li>
		</ul>
		<li>Incremental improvements on cursor motion within our still-unstable VisualBlockMode</li>
		<li>Whole mess of miscellaneous defect fixes</li>
	</ul>
    
    <p>That's a lot of changes in a single month and I haven't been able to
    spend time with each new feature. It may take some extra testing to make
    sure nothing fell through the cracks. Anyway, if this next month is anything
    like the last month then 0.34.0 is looking to be a HUGE release for us.</p>
    
</div>

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>