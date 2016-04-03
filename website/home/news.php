<div class="newsbox">
    <div class="date">2016-04-03</div>
    <h4>0.66.0 Released</h4>
    
    <p>This new release of Vrapper is pretty sizable.  We received some
    great help in the form of Pull Requests.  Thank you to all
    our users and contributors keeping this project going!</p>
    
    <ul>
		<li>Added support for Doxygen (//!) and C# (///) comments in `gq` command</li>
		<li>Added support for 'is'/'as' sentence text object</li>
		<li>Added support for &lt;C-n&gt; and &lt;C-p&gt; in Line Completion mode</li>
		<li>Added '%' expansion to filename when using ':!' shell command</li>
		<li>Add mapping for &lt;C-w&gt;_q to close current tab</li>
		<li>Fixed g_&lt;C-g&gt; to match Vim output (rather than Vrapper debug info)</li>
		<li>Fixed intermittent issue causing NullPointerExceptions when switching tabs</li>
		<li>Fixed issue where a macro didn't have a single atomic undo</li>
		<li>Fixed issue where :normal command didn't have a single atomic undo</li>
		<li>Changed behavior to prevent recursive macros from executing</li>
		<li>Fixed issue where deleting after yanking text to a register would clobber the register</li>
		<li>Fixed issue with Position Categories which could render editor unusable</li>
		<li>Fixed issue where setting 'let @x=""' in .vrapperrc would clobber that register every time a new file was opened</li>
		<li>Fixed inconsistencies in Line Completion mode (&lt;C-x&gt;&lt;C-l&gt;)</li>
		<li>Fixed issue using a visual selection with the Surround plugin</li>
		<li>Fixed issue with Vrapper interacting with JasperSoft Studio</li>
		<li>Shift-Arrow keys now enter Visual mode</li>
		<li>Disable Eclipse's Overwrite mode if the user hits the 'Insert' key</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2016-02-01</div>
    <h4>0.64.0 Released</h4>
    
    <p>This new release of Vrapper only has a couple defect fixes but they're
    important fixes.  One defect could create an infinite re-paint loop
    (crashing Eclipse) and another broke Vrapper in older (< 3.7) versions of
    Eclipse.  Both of those defects are fixed!</p>
    
    <ul>
		<li>Fixed issue causing an infinite loop when performing an Eclipse refactor</li>
		<li>Fixed issue preventing Vrapper from working in older versions of Eclipse (< 3.7)</li>
		<li>Fixed issue where confirm substitution wouldn't open folds</li>
		<li>Fixed issue where setting register contents with 'let @' wouldn't allow '=' character</li>
		<li>Fixed issue with entering search mode on the last line of a file when that line is empty</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-12-06</div>
    <h4>0.62.0 Released</h4>
    
    <p>Time for another Vrapper release.  Some great progress
    was made in the last two months fixing older defects that
    have been around for a while.  There was also a lot of general
    refactoring to cleanup the code and fix certain little annoyances.
    Hope everyone enjoys this new release!</p>
    
    <ul>
		<li>Added support for 'o' and 'O' in visual block mode</li>
		<li>Added support for g_CTRL-G to display cursor position info</li>
		<li>Fixed issue where a bad regex pattern could crash a Vrapper instance</li>
		<li>Fixed issue where pressing DELETE in visual mode wouldn't delete</li>
		<li>Fixed issue when multiple possible mappings are pending</li>
		<li>Fixed issue with multi-page editors in ABAP Development Tools</li>
		<li>Fixed error when using Vrapper with Eclipse Overview plugin</li>
		<li>Fixed corner case where 'gv' wasn't restoring the correct selection</li>
		<li>Fixed corner case related to executing a mapping after Eclipse auto-completed a set of parentheses</li>
		<li>Refactored some code to remove hacks for drawing the cursor</li>
		<li>Refactored code around visual block mode</li>
		<li>Refactored command-line parsing so multiple spaces can appear between certain commands and their arguments</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-10-04</div>
    <h4>0.60.0 Released</h4>
    
    <p>It's been another two months so I'm releasing our current changes.
    Development is still moving slowly, but it's moving forward.  Albertdev did
    a bunch of refactoring to clean up some code under the hood.  The work
    wasn't related to any specific defect but a lot of the commands should be
    more stable.  Here are the changes I can describe:</p>
    
    <ul>
		<li>Added support for 'wrapscan' setting</li>
		<li>We now determine certain default values based on Eclipse settings</li>
			<ul><li>tabstop, expandtab, newline type, etc.</li></ul>
		<li>Fixed intermittent display issue with dark themes</li>
		<li>Fixed issue with 'gq' counting a tab character as one column</li>
		<li>Massive refactoring of many line-based and :ex commands</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-08-02</div>
    <h4>0.58.0 Released</h4>

    <p>Development for Vrapper is slowing to a halt.  We're all too busy to find
    the time to make enhancements anymore.  I apologize to all of you who have
    been filing new enhancement requests which we've been ignoring.  Hopefully
    we'll find the time to implement some of them here and there.  However,
    Vrapper is stable and has lots of features so I'm hoping most users are
    happy with the current state of things.</p>
    <p>With all that said, there were some fixes in the last couple months so
    I'm releasing another new version.  Here are the changes:</p>
    
    <ul>
        <li>Fixed issue where :g command with 'normal' wouldn't return to normal mode</li>
        <li>Fixed issue with :g command and 'normal' inserting newlines</li>
        <li>Fixed issue with 'o' and 'cleanindent' at the end of a file</li>
        <li>Fixed issue with 'A-O' from insert mode not returning to insert mode</li>
        <li>Fixed issue where selecting text within a fold didn't open the fold</li>
        <li>Fixed issue with mouse-selections going over a fold</li>
        <li>Fixed display of mode and recording status when using Eclipse's dark themes</li>
        <li>Fixed selection highlighting when using 'c'onfirm flag for substitutions</li>
        <li>Fixed performance issue with search highlighting</li>
    </ul>
    
    <p>Thank you to all the users who have used and enjoyed Vrapper throughout the years!</p>
</div>
<div class="newsbox">
    <div class="date">2015-06-07</div>
    <h4>0.56.0 Released</h4>

    <p>It's been a bit of a slow month for us.  There weren't many changes
    but it's been two months so I might as well do another release.
    Here are the changes since my last update</p>
    
    <ul>
        <li>Fixed issue where some jumps weren't added to Eclipse history and &lt;C-O&gt;/&lt;C-I&gt;</li>
        <li>Fixed issue where setting a string option to "" wouldn't clear the value</li>
        <li>Fixed issue where `:s//` wouldn't clear previous search</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-05-10</div>
    <h4>Updates to unstable update site</h4>

    <p>Here's your monthly status update on recent changes to Vrapper's
    unstable update site:</p>
    
    <ul>
        <li>Added support for fold commands (za, zo, zc, zR, zM) in optional PyDev plugin</li>
        <li>Fixed issue so ex commands will reuse last search if no pattern is provided</li>
        <li>Fixed issue with Ctrl- keys when replaying a macro</li>
        <li>Fixed issue where switching visual modes reset selection</li>
        <li>Fixed issue with switching to search mode from a visual mode</li>
        <li>Fixed issue which required whitespace when defining a :move range</li>
        <li>Fixed issue with :d, :y, and :retab requiring line ranges</li>
        <li>Fixed issue with Ctrl-A incrementing numbers larger than 32-bits</li>
        <li>Other various refactoring (not tied to any specific defect)</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-04-05</div>
    <h4>0.54.0 Released</h4>

    <p>Another two months, another Vrapper release.  We've now received over
    50,000 installs via the Eclipse Marketplace, which puts us at number 42 out
    of all 1,800+ plugins available in the Marketplace.  Thank you everyone who
    continues to use and support Vrapper!</p>

    <p>We now have over 60 issues on our GitHub repository but I noticed
    something recently: there are only 11 defects.  Of course, since I'm the one
    who labels these items there are another 10 "annoyances", which are items
    that don't really prevent you from doing anything but would be nice to have.
    The reason I bring this up is because we have 35 feature requests.  Once
    again, it seems like Vrapper is at the point where we can meet the majority
    of user's use cases and we're down to the really obscure and difficult
    features that only power users even know exist.  Also, I don't think we have
    any feature requests which have a "me too!" comment in them; I think all of
    our feature requests each have one person waiting on them.  I may be a
    little biased, but I think Vrapper is one of the best Vim emulation plugins
    for any text editor. I'm not aware of any other Vim emulator that has
    support for additional (ported) vimscript plugins.  I think we're doing
    pretty good!</p>
    
    <p>Anyway, down to business.  0.54.0 includes the following changes since my
    last status update:</p>

    <ul>
        <li>Added support for counts before an ex command (4:s/foo/bar/g)</li>
        <li>Added support for setting a register to the contents of another register (:let @*=@3)</li>
        <li>Fixed issue where indenting a block selection indented the beginning of the line</li>
        <li>Fixed issue with potential off-by-one when indenting a block selection</li>
        <li>Fixed issue with ':source' not interpreting '~' as '$HOME'</li>
        <li>Fixed issue with copying a block selection to the clipboard</li>
        <li>Fixed issue with restoring selection after toggling between visual mode and linewise visual mode</li>
        <li>Fixed issue with reverse search of a single character</li>
        <li>Fixed issue with stale selections potentially throwing an exception</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-03-09</div>
    <h4>Updates to unstable update site</h4>
    <p>I'm still sticking with our monthly status updates.
    Here are the changes which are currently on our unstable update site:</p>

    <ul>
        <li>Added new optional plugin for indent text object</li>
        <ul>
          <li>Useful with Python and other whitespace languages</li>
          <li>'ii' for indent block, 'ai' for indent block plus line above</li>
          <li><a href="http://www.vim.org/scripts/script.php?script_id=3037">http://www.vim.org/scripts/script.php?script_id=3037</a></li>
        </ul>
        <li>Added support for ':let @/ = ""' to clear search highlight</li>
        <li>Introduced config property 'hlscope' to determine search highlight scope</li>
        <ul>
          <li>Only valid when ':set hlsearch' is active</li>
          <li>'window' - highlight the same keyword in all editors</li>
          <li>'clear' - only one editor will have a highlight at a time</li>
          <li>'local' - each editor has its own search highlight</li>
          <li>Default is 'clear'</li>
        </ul>
        <li>Fixed issue with 'c' and 's' in visual-block mode</li>
        <li>Fixed issue with XML editor provided by ADT</li>
        <li>Fixed issue with unintentionally recursive mappings</li>
        <li>Fixed issue with drawing command-line when no scrollbars present in certain editors</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-02-11</div>
    <h4>0.52.1 Released</h4>
    <p>I just released 0.52.0 a couple days ago but a number of people quickly
    hit the same problem.  Certain command-line commands (such as :w and any
    mappings to an :eclipseaction) caused the editor to lose focus.  This meant
    you had to click your mouse within the editor to regain focus.  As Vim
    users, we all know how frustrating it is when your hands have to leave the
    keyboard to use the mouse.  So we fixed this issue and released 0.52.1 as
    quickly as we could. We apologize for any inconvenience.</p>
    
    <p>The only difference between 0.52.0 and 0.52.1 is that single defect fix:</p>

    <ul>
        <li>Fixed issue where some command-line commands would cause the editor to lose focus</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-02-08</div>
    <h4>0.52.0 Released</h4>
    <p>While December was extremely slow for us, we did have some code changes
    in January so I'm sticking with our two-month release cycle and releasing
    0.52.0 today.  The following changes are in 0.52.0:</p>

    <ul>
        <li>Added support for 'gn' and 'gN'</li>
        <li>Added support for ':buffer#' command and Ctrl-^</li>
        <li>Added support for 'C' in visual mode (alias for 'c' or 's')</li>
        <li>Changed ':ls' command to be ':files'</li>
        <li>Changed ':buffers' and ':ls' to be more vim-like</li>
        <li>Fixed issue where '/e' in search didn't work with regex</li>
        <li>Fixed issue where '&lt;c-=&gt;' couldn't be used in a mapping</li>
        <li>Fixed issue where canceling a search would also clear the visual selection</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2015-01-04</div>
    <h4>(Lack of) Status Update</h4>
    <p>It's been a month since 0.50.0 was released so I figured I should stick
    with my monthly status updates and tell you about the recent changes.
    However, there is nothing to report. Since 0.50.0 was released there hasn't
    been a single line of code changed and there hasn't been a single defect
    filed.  December is usually a slow month for us anyway, but I'm surprised we
    haven't had any defects filed either. I guess 0.50.0 must be a rock-solid
    release (or you guys must all be busy during December too).</p>
    <p>I hope you all enjoy Vrapper 0.50.0!</p>
</div>

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>