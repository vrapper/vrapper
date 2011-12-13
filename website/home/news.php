<div class="newsbox">
    <div class="date">2011-12-12</div>
    <h4>Another Release Candidate (0.18.0)</h4>
    <p>We've made a couple fixes so I've updated the unstable update site.  Changes include:</p>
    
    <ul>
		<li> :{line number} was broken in the last Release Candidate, fixed</li>
		<li> 'ctx' where 'x' not found was broken in last Release Candidate, fixed</li>
		<li> Fixed logic when deleting/yanking last line of a file</li>
		<li> Implemented :wa[ll] command to write all dirty editors</li>
		<li> Fixed cursor location on yank.  At least I think we did.  Vim isn't always consistent on when to move
		the cursor and when not to but I think we match vim's behavior now.
		I've removed the 'moveonyank' setting from vrapper with this fix because it is no longer relevant. 
		Rather than an all-or-nothing setting to move the cursor or not, we now sometimes move the cursor to match vim's behavior.</li>
    </ul>

</div>
<div class="newsbox">
    <div class="date">2011-11-11</div>
    <h4>0.18.0 Release Candidate</h4>
    <p>The Unstable update site has been updated with the latest version of the code (0.17.20111111).  I would like to
    think of this version as the Release Candidate for the next Vrapper release (0.18.0).  Thanks to the help of many
    contributors I think we have plenty of defect fixes and new features that everyone will enjoy.  Please play with this
    unstable version and see if there are any changes you think we should make before calling it stable.</p>
    
    <p>For those of you keeping track, here are the differences between version 0.16.0 and this Release Candidate:</p>
    <ul>
		<li> Search results can now be used as text motions</li>
		  <ul><li>c/&lt;pattern&gt;, d/&lt;pattern&gt;, y/&lt;pattern&gt;, v/&lt;pattern&gt;</li></ul>
		<li> Move into VisualMode on mouse select</li>
		  <ul><li>You can disable this by setting the 'visualmouse' option to 'false' (true by default)</li></ul>
		<li> Fixed cursor location when pasting</li>
		  <ul><li>p, P, gp, gP</li></ul>
		<li> Added 'za' operation for toggling fold open/close</li>
		<li> Added &lt;TAB&gt; as a bindable key</li>
		<li> Added &lt;C-c&gt; key binding to mimic &lt;ESC&gt;</li>
		   <ul><li>Only works if you unbind &lt;C-c&gt; in Eclipse first</li></ul>
		<li> Added key binding for 'z&lt;CR&gt;' (same operation as 'zt')</li>
		<li> Added configurable option 'imdisable' to disable Input Method when exiting insert mode (useful on Japanese keyboards)</li>
		<li> Added optional vrapper plugins (for CDT, JDT, Surround.vim)</li>
		<li> Fix bug #97 PageUp/PageDown don't work in visual mode</li>
		<li> Fix bug #79 'cw' for single character changes include next word</li>
		<li> Fix bug #70 'dw' on the last word of a line will join lines</li>
		<li> Fix bug #98 Uninstalling vrapper + ADT and now I can't quit Eclipse</li>
    </ul>

</div>
<div class="newsbox">
    <div class="date">2011-08-25</div>
    <h4>What I'd like to see in Vrapper 0.18.0</h4>
    <p>The last version of Vrapper took over a year to be released.  I'd like to make sure that doesn't happen again.
    Of course, I also don't want to release a new version with too few modifications from the previous version.
    I decided to look at the current list of defects and see which ones I feel are the highest priority (or provide the largest benefit).
    This of course is very selfish because I'm selecting the defects I've personally run into or new features I would personally use.
    With that said, this is an open source project.  If anyone contributes code to fix some other defect or implement some other feature I certainly won't reject it.</p>
    
    <p>If we could fix the following defects, I think it would be significant enough to be the next release:</p>
    <ul>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/70">#70</a>	'dw' on the last word of a line will join lines</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/73">#73</a>	% text movement operator doesn't work in visual mode.</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/79">#79</a>	cw for single characters changes include next word</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/81">#81</a>	need d/<pattern> please</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/97">#97</a>	PageUp/PageDown don't work in visual mode</li>
    </ul>
    
    <p>In addition to those defects, I'd love to fit in these stretch goals.  I think we're at the point where these should be feasible.</p>
    <ul>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/50">#50</a>	Search and replace support</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/59">#59</a>	blockwise-visual missing</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/82">#82</a>	Pasting a search string using the keyboard doesn't work</li>
    </ul>
    
    <p>We're always willing to accept code contributions.  If you can come up with a solution for any of the defects listed above, simply initiate a Pull Request on <a href="https://github.com/vrapper/vrapper">our GitHub project</a> to submit your code.</p>

</div>
<div class="newsbox">
    <div class="date">2011-07-06</div>
    <h4>Testing Help Needed</h4>
    <p>The unstable update site has been updated to include some optional plugins.  These plugins add some language-specific commands for Java and C/C++.  We didn't release them as part of 0.16.0 because they haven't been fully tested.  We would appreciate it if some of you Java or C/C++ developers out there installed these optional plugins and played with them a bit.  Let us know how it goes.</p>
    <p>Both the JDT and CDT plugins provide the following commands for their respective languages:</p>
    <ul>
        <li>Toggle comment (gc&lt;movement&gt;)</li>
        <li>Go to declaration (CTRL+])</li>
        <li>Auto-indent (== for current line or =&lt;movement&gt;)</li>
    </ul>
    <p>The JDT plugin also has the following shortcuts:</p>
    <ul>
        <li>Rename element (gR)</li>
        <li>'Refactor' Menu (gr)</li>
        <li>'Source' Menu (gm)</li>
    </ul>
    <p>In addition to those plugins, we implemented a port of a useful vim script called 'surround.vim' (from <a href="http://www.vim.org/scripts/script.php?script_id=1697">http://www.vim.org/scripts/script.php?script_id=1697</a>).  It isn't a complete port but it's a start.</p>
    <p>The Surround plugin works on the 'c', 'd', and 'y' commands.  After initiating one of those commands hit 's' followed by one of the following characters:</p>
    <ul>
        <li>a &lt; &gt;</li>
        <li>b ( )</li>
        <li>B { }</li>
        <li>[ ]</li>
        <li>'</li>
        <li>"</li>
        <li>`</li>
    </ul>
    <p>For the 'c' command you'll have to select the character that represents what the text is currently surrounded by then the character you want to replace it with.  For example, cs"( would replace the surrounding double-quotes with parentheses.</p>

</div>
<div class="newsbox">
    <div class="date">2011-06-27</div>
    <h4>0.16.0 released</h4>
    <p>Version 0.16.0 has been released. It is not a major release in terms of functionality but it is an attempt to keep the project moving forward.  Changes are:</p>
    <ul>
        <li>Added incremental search (:set incsearch)</li>
        <li>Added highlight search (:set hlsearch)</li>
        <li>Added line number toggle (:set number)</li>
        <li>Added show whitespace toggle (:set list)</li>
        <li>Added scrolljump and scrolloff options (:set scrolljump=10 :set scrolloff=10) </li>
        <li>Added commands for lenient word search (g* and g#)</li>
        <li>Center the line after jumping to a line that was far away from the viewport</li>
        <li>Ctrl-V in search/command-line mode pastes text from the clipboard</li>
    </ul>

</div>
<div class="newsbox">
    <div class="date">2011-06-17</div>
    <h4>We're not quite dead</h4>
    <p>The last release of vrapper may have been over a year ago but this project is not dead.  A new version of vrapper will be released soon and this project will hopefully become a little more active in the future.</p>
    <p>The purpose of this post was mostly just to make sure I knew how to update the main page. :)</p>

</div>
<div class="newsbox">
    <div class="date">2010-04-12</div>
    <h4>0.14.0 released</h4>
    <p>Version 0.14.0 has been released. Changes are:</p>
    <ul>
        <li>Corrected line-wise visual mode behaviour on empty lines.</li>
        <li>Yanking text objects moves the cursor on the start of the text object. Use ":set nomoveonyank" for old behaviour.</li>
        <li>Toggling Vrapper enables/disables Vim-emulation for all open editors.</li>
        <li>Visual mode operations may be repeated using the dot command.</li>
        <li>Pasting over selections in visual mode is possible.</li>
        <li>Join lines positions the cursor between the joined parts.</li>
        <li>Added paragraph motions and text objects.</li>
        <li>Bound ctrl-u and ctrl-d to Eclipse actions page-up and page-down.</li>
        <li>Fixed zz command to work correctly with folded sections and added z., zb, zt, z- commands.</li>
        <li>gt and gT behave more like in Vim.</li>
        <li>Pressing caps lock or alt is not interpreted as keystroke anymore, so caps lock and alt can be used in command line mode to input caps and special characters.</li>
    </ul>

</div>
<div class="newsbox">
    <div class="date">2009-11-09</div>
    <h4>0.12.0 released</h4>
    <p>Version 0.12.0 has been released. Changes are:</p>
    <ul>
        <li>lots of new
    <a href="../documentation/?page=3#text_objects">text objects</a></li>
        <li>it is now possible to switch from character to linewise visual
            mode and vice versa by using V and v</li>
        <li>added 'selection' option to control selection behaviour during
            visual mode</li>
        <li>boolean options can now be toggled (e.g. "set ignorecase!")</li>
        <li>lots of minor bugfixes and improvements</li>
    </ul>
</div>
