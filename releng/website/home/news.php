<div class="newsbox">
    <div class="date">2018-11-24</div>
    <h4>0.74.0 Released</h4>
    <p>You guys just won't let this project die.  It's been well over a year
    since our last release but people still submit issues and code changes.  We
    just recently found an issue in the current version of Eclipse which causes
    lots of headaches for anyone with an ALT_GR key.  This issue seemed drastic
    enough to justify another stable release.  This new version includes all
    code changes submitted in the 20 months since 0.72.0.</p>
    <p>Changes since 0.72.0 are:</p>
    <ul>
		<li>Fixed issues with ALT_GR handling</li>
		<li>Fixed behavior of '%' when cursor is on EOL</li>
		<li>Fixed newline replacement when using :g/[pattern]/s/^/...</li>
		<li>Fixed mode indicator when using dark theme</li>
		<li>Fixed issue with pasting a register from visual-block mode</li>
		<li>Fixed issue with recursive mapping loop detection</li>
		<li>Fixed potential stack overflow when setting caret location</li>
		<li>Fixed ':marks' to include marks from closed files</li>
		<li>Fixed issue with '//' to repeat last search (rather than just '/')</li>
		<li>Fixed infinite loop with sneak plugin when the first characters of a file matched search string</li>
		<li>Added support for &lt;c-home&gt; and &lt;c-end&gt; to go to first, last character of file</li>
		<li>Added ':set spell' command</li>
		<li>Added '"%' register to get current filename</li>
		<li>Added ability to escape '\%' when using ':r!'</li>
		<li>Added support for counts when manipulating camelCase objects</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2017-03-11</div>
    <h4>0.72.0 Released</h4>
    <p>This project has been quiet for a while.  We only have
    2 main developers and neither of us use Eclipse in our daily
    job anymore.  But, people are still submitting Pull Requests
    so I'll keep merging them in and doing releases as long as
    people are still interested.</p>
    <p>Changes since 0.70.0 are:</p>
    <ul>
		<li>Added a port of justinmk's <a href="https://github.com/vrapper/vrapper/pull/764">Sneak plugin</a></li>
		<li>Added ability to set custom .vrapperrc location with `vrapper.vrapperrc` system property</li>
		<li>Added ability to map eclipse motions with <a href="https://github.com/vrapper/vrapper/pull/762">&lt;Plug&gt;()</a></li>
		<li>Added support for `:delmarks` command</li>
		<li>Added `:se` shortcut for `:set` command</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2016-08-07</div>
    <h4>0.70.0 Released</h4>
    <p>This is a very small release and I probably could've just waited longer
    until there were more changes worth releasing, but I think it's good to have
    a reliable release cycle and we've been doing two-month releases for awhile now.
    Of course I rushed out 0.68.1 a little more than a month ago and we've only had
    two changes since then.  So I do 0.xx.0 releases every two months and 0.xx.1 releases
    when needed.  It makes sense to me at least.  Anyway, these are the only changes between
    0.68.1 and 0.70.0:</p>
    <ul>
    	<li>Fixed behavior when one custom mapping is a subset of another custom mapping</li>
		<li>Fixed behavior in subword text object plugin when cursor is on word boundary</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2016-06-23</div>
    <h4>0.68.1 Released</h4>
    <p>We had a regression in the 0.68.0 release and I debated whether it was
    critical enough to release a 0.68.1 to fix it.  The defect isn't major but
    it's in a common enough feature ('*' and '#') that a lot of people have
    noticed it. Given that Eclipse Neon was just released, I'm guessing a lot of
    people will be re-installing the current stable build of Vrapper with it.  I
    figured it's best to have a solid release for those people.  So here's a
    quick release with a couple issues we've fixed in the last month.  They are:</p>
    <ul>
		<li>Fixed regression where '*' and '#' behaved like 'g*' and 'g#'</li>
		<li>Fixed stupid mistake which prevented you from setting &lt;leader&gt; in .vrapperrc</li>
		<li>Fixed issue with Vrapper resetting an Eclipse selection when using 'select all'</li>
		<li>Fixed regression which broke the ':set &lt;option&gt;?' feature to display value</li>
		<li>Changed cycle plugin so cursor stays at beginning of changed word</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2016-06-04</div>
    <h4>0.68.0 Released</h4>
    <p>I was able to find some time this month to work on new Vrapper features, including
    some plugin requests that have been sitting around for a while.  Of course, this project
    is so mature now that we're looking at the more obscure features in Vim but all of
    these were requested by users filing Issues on our GitHub page.  We hope you continue to enjoy
    Vrapper!</p>

    <ul>
        <li>Fixed issue where recording a macro would overwrite the unnamed register</li>
        <li>Fixed issue executing a macro with a count</li>
        <li>Fixed issue where `*` search wasn't using iskeyword value to determine word boundaries</li>
        <li>Fixed sentence motions '( )' and text object `s` when sentence ends on a newline</li>
    </ul>

    <ul>
        <li>Added support for Mac command key mappings `&lt;D-`</li>
        <li>Added support for `%` to jump between block comment `/* */` tokens</li>
        <li>Added support for `\%V` in command-line substitutions and search for limiting to visually selected area</li>
        <ul>
            <li>useful when selection isn't line-based</li>
        </ul>
        <li>Added support for `\k` regex class for iskeyword value</li>
        <li>Added support for searching from command-line (ex) mode `:/` and `:?`</li>
        <li>Added support for `&lt;Leader&gt;` and `:let mapleader=`</li>
        <li>Added support for comment text objects `ic`/`ac` and `iC`/`aC`</li>
        <ul>
            <li>supports single line comments `//`, `#`, `--`</li>
            <li>supports block comments `/* */`, `&lt;!-- --&gt;`</li>
            <li>See <a href="https://github.com/vrapper/vrapper/issues/586#issuecomment-211575385">this comment</a> for more details</li>
        </ul>
    </ul>

    <ul>
        <li>Created a new optional plugin for sub-word motions</li>
        <ul>
            <li>Moves over words within snake_case and camelCase names</li>
            <li>Based loosely on <a href="https://github.com/bkad/CamelCaseMotion">https://github.com/bkad/CamelCaseMotion</a></li>
            <li>Details here: <a href="http://vrapper.sourceforge.net/documentation/?topic=optional_plugins#subword">Sub-Word Plugin</a></li>
            <li>Convenience command `:subwordmappings` will setup default mappings:</li>
        </ul>
        <li>map \b &lt;Plug&gt;(subword-back)</li>
        <li>map \e &lt;Plug&gt;(subword-end)</li>
        <li>map \w &lt;Plug&gt;(subword-word)</li>
        <li>map i\ &lt;Plug&gt;(subword-inner)</li>
        <li>map a\ &lt;Plug&gt;(subword-outer)</li>
    </ul>

    <ul>
        <li>Created a new optional plugin to "cycle" through word replacements</li>
        <ul>
            <li>Based on <a href="https://github.com/zef/vim-cycle">https://github.com/zef/vim-cycle</a></li>
            <li>Details here: <a href="http://vrapper.sourceforge.net/documentation/?topic=optional_plugins#cycle">Cycle Plugin</a></li>
            <li>`&lt;C-a&gt;`/`&lt;C-x&gt;` will replace word under cursor with next item in cycle</li>
            <li>`:AddCycleGroup foo bar baz` = foo-&gt;bar-&gt;baz-&gt;foo-&gt;...</li>
            <li>Default cycle true-&gt;false will toggle "true" to "false" with a single keystroke</li>
        </ul>
    </ul>
</div>
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

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>