
<a href="index.php">&lt;&lt; Newer Posts</a><br/>

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
<div class="newsbox">
    <div class="date">2014-12-07</div>
    <h4>0.50.0 Released</h4>
    
    <p>0.50.0 has been released, continuing with our two-month release cycle. At
    this point, I think I've been fully relegated to Project Manager status as
    albertdev has stepped up and handled basically all development in this
    release.  He's much better at this than I am and Vrapper is stronger for it.
    I'll take credit for resurrecting this project when the original authors
    abandoned it, but albertdev is the one who is able to fix the really
    difficult defects and introduce tough new features.  Thank you albertdev for
    your amazing work!</p>
    
    <p>The following changes are included in 0.50.0:</p>
    
    <ul>
        <li>Added support for 'p'aste in visual block mode</li>
        <li>Added support for "* as a separate register on Linux</li>
        <li>Added support for the following 'clipboard' values: 'unnamedplus', 'autoselectplus', 'exclude:'</li>
        <li>Added support for 'g_' motion</li>
        <li>Added support for refreshing current file using ':e' with no arguments</li>
        <li>Added support for 'u' and 'U' in visual mode</li>
        <li>Fixed behavior of '{count}gT' to be more Vim-compliant</li>
        <li>Fixed issue with rendering command-line while scrolling</li>
        <li>Fixed issue with moving the cursor while in insert mode, then repeating</li>
        <li>Fixed issue with 'S' while on an empty line with Windows line-endings</li>
        <li>Fixed issue with eclipseaction in a vnoremap</li>
        <li>Fixed issue with ':reg' and ':marks' in Java 8</li>
        <li>Fixed issue with visual display of selecting a newline</li>
        <li>Fixed issue with incremental search highlight when match is under the cursor</li>
        <li>Fixed issue with editing certain configuration files in IBM's Rational Software Architect IDE</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-11-05</div>
    <h4>Updates to unstable update site</h4>
    
    <p>It's been another month, time for your monthly status update with Vrapper.
    We've made the following changes on the unstable update site:</p>
    
    <ul>
        <li>Added new "line" text object plugin</li>
            <ul>
            <li>'al' for column 0 to end-of-line</li>
            <li>'il' for first non-whitespace character to last non-whitespace character of a line</li>
            <li>Port of <a href="http://www.vim.org/scripts/script.php?script_id=3886">http://www.vim.org/scripts/script.php?script_id=3886</a></li>
            </ul>
        <li>Added support for 'gI' (insert at column 0)</li>
        <li>Added support for ':cmap'</li>
        <li>Fixed issue where word text objects would stop at unicode characters</li>
        <li>Fixed issue with 'O' on the line below a closed fold</li>
        <li>Fixed issue where 'q' (record macro) couldn't be re-mapped</li>
        <li>Fixed issue with 'A' in visual-block mode</li>
        <li>Fixed issue with '>' indent command with counts in visual mode</li>
        <li>Fixed issue which prevented a custom ':command' from using ':normal'</li>
        <li>Fixed issue where '\t' in substituion text couldn't be escaped</li>
        <li>Fixed styling issues in status bar</li>
        <li>Fixed behavior when toggling between visual mode and visual-line mode</li>
        <li>Changed 'ge', 'gE', and 'w' to be more vim-compliant in visual mode</li>
        <li>Various refactoring of old defect fixes</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-10-05</div>
    <h4>0.48.0 Released</h4>
    
    <p>Vrapper version 0.48.0 has been released, back on schedule with our
    two-month release cycle.  The only changes since the last news post were
    more esoteric bug fixes. The following fixes are included in 0.48.0:</p>
    
    <ul>
        <li>Fixed issue with cursor placement after multi-line 'gp'</li>
        <li>Fixed issue using counts with 'o' command</li>
        <li>Fixed behavior for 2di' and 2yi' around whitespace</li>
        <li>Fixed issue with '*' and '#' when 'smartcase' is enabled</li>
        <li>Fixed issue where red error text wouldn't disappear from status bar</li>
        <li>Fixed issue with hitting backspace as soon as you enter insert mode then hitting '.' to repeat insertion</li>
        <li>Fixed issue with performing multiple searches while in visual mode</li>
        <li>Fixed issues with `[ and `] not updating after a delete or after 'p'</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-09-07</div>
    <h4>Updates to unstable update site<br/>(plus a minor rant)</h4>
    
    <p>Time for another monthly status update on Vrapper.  For the last couple
    years, our GitHub Issues page has hovered around 30 open defects/feature
    requests. Defects have been filed and fixed and we always seemed to have
    about 30 open at any given time.  This last month brought with it what I'm
    going to call some "overzealous" new users.  We jumped from having ~30
    defects to having over 50 in one month.  13 of those issues were filed by a
    single person.  Now, I don't take this as a sign that Vrapper is unstable. I
    may be overly optimistic here, but I think it's actually because Vrapper has
    reached a point where we meet about 95% of most users' use cases.  We're
    down to the esoteric features and nit-picking defects; which is to say, the
    really hard stuff.  Just take a look at the current changes on our unstable
    update site:</p>
    
    <ul>
        <li>Added support for 'c_&lt;C-R&gt;_&lt;C-W&gt;' and 'c_&lt;C-R&gt;_&lt;C-A&gt;'</li>
        <ul>
        	<li>Insert word under cursor into command-line</li>
        </ul>
        <li>Added support for 'n' flag in substitutions</li>
        <ul>
         	<li>Count matches without performing substitution</li>
        </ul>
        <li>Fixed issue with using counts on a non-linewise paste</li>
        <li>Fixed issue with '}' in visual mode with ipmotion.vim plugin</li>
        <li>Fixed issue with 'incsearch' and '?' search mode</li>
        <li>Fixed issue with cursor position after a 'u' undo</li>
        <li>Fixed issue with ']' mark after deleting text</li>
        <li>Fixed issue with cursor position in visual mode when using "selection=exclusive"</li>
        <li>Fixed issue with performing multiple searches while in visual mode</li>
        <li>Fixed issue with enabling Vrapper while a text selection was active</li>
        <li>Fixed issue with pasting on an empty line with windows newlines</li>
        <li>Fixed issues with Vrapper interacting with Perforce plugin</li>
    </ul>

    <p>There are very few items in that list that I think are generally
    applicable or would affect a large number of users (and those are just the
    items we were able to fix).  Hopefully this is a sign that the majority of
    our users are happy with Vrapper.  Hopefully the majority of our users
    aren't even running into most of the defects we're fixing at this point.</p>
    
    <p>
    <b>**Changes in behavior**</b><br/>
	One last thing.  I removed a couple configurable options which nobody should've
	been using anyway.  We had support for <b>"saney"</b> and <b>"sanecw"</b> which would modify
	the behavior of 'Y' and 'cw' to be more consistent with other Vim commands
	(despite our 'Y' and 'cw' being consistent with Vim's 'Y' and 'cw').  I removed
	these options because Vim's own documentation says to use ":map Y y$" and ":map
	cw dwi" if you want that "sane" behavior.  I think our "saney" and "sanecw"
	options were added before Vrapper had support for ":map" but they are no longer
	necessary since we've had ":map" for a while now.</p>
</div>
<div class="newsbox">
    <div class="date">2014-08-08</div>
    <h4>0.46.0 Released</h4>
    
    <p>If I was sticking closely to my two-month release cycle I would've released last week.
    However, there weren't too many changes in this release.  I also had just fixed a defect
    and I wanted to make sure my fix solved the problem.  Now, a week later, I'm releasing 0.46.0.
    Here are the changes since the last news post (included in 0.46.0):</p>
    
    <ul>
        <li>Changed 'syncmodifiable' to ignore Eclipse's readonly flag by default.  This should help some Perforce users.</li>
        <li>Fixed issue where selecting from the end of the line backwards would skip the last character</li>
        <li>Fixed issue where Vrapper would stay in VisualMode after Eclipse cleared the selection</li>
        <li>Fixed issue where a visual selection could jump to the beginning of the next line</li>
        <li>Fixed issue where Mac command key was ignored.  It is now treated as Ctrl.</li>
        <li>Fixed issue with 'i_Ctrl-Y' after 'A'.</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2014-07-05</div>
    <h4>Updates to Unstable update site</h4>
    
    <p>Another month, another status update for Vrapper.  In the past
    month, we've made the following changes to the unstable update site:</p>
    
    <ul>
        <li>Added support for ':command' to define custom user commands</li>
        <li>Fixed issue where 'softtabstop' broke multi-character mappings (like 'imap jj &lt;esc&gt;')</li>
        <li>Fixed issue where you couldn't use unicode characters in .vrapperrc</li>
        <li>Fixed issue with mapping '0' to something then using counts for a command</li>
        <li>Fixed issue with 'R' replace mode at the end of a file</li>
        <li>Fixed issue with using &lt;TAB&gt; in a macro</li>
        <li>Fixed issue where you couldn't use a space in the right-hand side of a mapping</li>
        <li>Fixed issue preventing you from mapping '&lt;c-/&gt;' to anything</li>
    </ul>

    <p>Surround Plugin changes:</p>
    <ul>
    	<li>Added ability to define custom surrounds by editor type</li>
    	<ul>
            <li>au "Java" surround c /*\r*/</li>
            <li>au "XML"  surround c &lt;!--\r--&gt;</li>
    	</ul>
    </ul>

    <p>Split Editor Plugin changes:</p>
    <ul>
        <li>Fixed issue with cursor location in a new split </li>
    </ul>
</div>
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
<div class="newsbox">
    <div class="date">2013-06-02</div>
    <h4>0.32.0 Released</h4>
    <p>Keeping with my two-month release cycle, 0.32.0 is now out.  In the last month
    I got a lot of help from contributors who actually know how to navigate the Eclipse API.
    They were able to fix a bunch of defects which I had no hope of ever fixing myself.
    I'm amazed at what they've been able to do already.</p>
    <p>These are the changes since the last news update (included in 0.32.0):</p>
    <ul>
        <li>Added support for new ipmotion (improved paragraph) optional plugin</li>
        <ul>
           <li>Based on <a href="http://www.vim.org/scripts/script.php?script_id=3952">http://www.vim.org/scripts/script.php?script_id=3952</a></li>
        </ul>
        <li>Added support for XML tags in our Surround.vim optional plugin</li>
        <li>Added support for :tabrewind and :tablast</li>
        <li>Fixed issues with :only and :qall</li>
        <li>Fixed &lt;number&gt;gt</li>
        <li>Fixed issue where :q didn't work with some file types</li>
        <li>Miscellaneous minor fixes to text object parsing and visual line selections</li>
    </ul>
    <p>Also... Don't tell anyone, but we've started implementing visual block mode.
    The cursor doesn't always move correctly and it doesn't support 'y' or 'p'
    yet but it is there.  It isn't stable so don't expect too much from it though.
    We hope to have the feature ready in the next release.</p>
</div>
<div class="newsbox">
    <div class="date">2013-05-01</div>
    <h4>Updates to unstable update site</h4>
    <p>I've hit my stride with a two-month release cycle so I'm also trying to
    make a habit of posting status updates once a month. In addition to the
    usual requests for more Vim features, in the last month I received a couple
    requests for features which aren't in Vim but our users wanted anyway.</p>
    <p>The unstable update site currently has the following changes:</p>
    <ul>
        <li>Add support for '\c' and '\C' in search to temporarily override ignorecase setting</li>
        <li>Add support for '_' motion</li>
        <li>Add support for ':e!' to revert file</li>
        <li>Add support for "0-"9 and "- delete and yank registers</li>
        <li>Add support for ':maximize' command to maximize editor area</li>
        <li>Add support for ':copy' and ':move' operations</li>
        <ul>
            <li>Useful with line range operations ':10,20 copy 140'</li>
        </ul>
        <li>Add support for ':vim' command</li>
        <ul>
            <li>Launches current file in gvim (so you can do things Vrapper doesn't support)</li>
            <li>After saving and quitting gvim, Eclipse will reload the file</li>
            <li>Path to gvim is defined by 'gvimpath' property (defaults to /usr/bin/gvim)</li>
        </ul>
        <li>Refactored XML text object parsing to be more compliant with Vim</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2013-03-29</div>
    <h4>0.30.0 Released</h4>
    <p>Looks like I'm hitting my stride with a two-month release cycle.  0.30.0
    is released and ready for use.  There were only a few changes since my last
    update which were included in this release:</p>
    <ul>
        <li>Added support for :retab operation</li>
        <li>Added support for partial command-line names</li>
        <ul>
            <li>e.g., :tabprev, :tabpre, :tabpr, etc. all work now</li>
        </ul>
        <li>Reimplemented paragraph text objects to match Vim's behavior</li>
    </ul>
    
    <p>The partial command-line names work pretty well as long as there isn't a name collision.
    For example, ':tabne' could be ':tabnew' or ':tabnext'.  Vim has some way to define precedence but
    Vrapper will just fail when this happens.  I think the current solution should work in most cases
    but there will be a couple places where you may still need to be explicit with the name.  Either way,
    this is an improvement over the previous implementation which required me to explicitly define every
    partial name supported.  Now I only need to explicitly define the collisions like ':tabn' going to ':tabnext'.</p>
    
    <p>Also, as I mentioned in my previous post, there was a lot of code submitted by contributors this release.
    I'm glad people are willing to help me out and constantly improve Vrapper.  Thank you for helping to
    keep this project alive!</p>
</div>
<div class="newsbox">
    <div class="date">2013-03-09</div>
    <h4>More updates to unstable update site</h4>
    <p>It's been over a month since my last update so I decided to look
    through our commit log and see what we've been working on lately.  Here
    is the list of changes to the unstable update site since my last news post:</p>
    <ul>
        <li>Added support for '**' in path variable</li>
        <ul>
            <li>e.g., :set path=src/main/java/**</li>
            <li>Tells :find to search all subdirectories for a filename</li>
        </ul>
        <li>Added support for :source</li>
        <ul>
            <li>You can now "source .vimrc" in your .vrapperrc</li>
        </ul>
        <li>Added support for 'cursorline' option</li>
        <ul>
            <li>enable/disable highlighting line with cursor</li>
        </ul>
        <li>Create new file when ":e &lt;filename&gt;" doesn't exist</li>
        <li>Added support for :tabonly (alias of :only)</li>
        <li>Added support for :tabf (alias of :find)</li>
        <li>Allow '+' to be omitted in line range operations</li>
        <ul>
            <li>".5" == ".+5"</li>
        </ul>
        <li>Fixed behavior with quote text objects to match Vim's behavior</li>
        <ul>
            <li>ci" will only look for quotes on current line now</li>
        </ul>
        <li>Fixed issue with escaping delimiter character in substitution</li>
        <li>Fixed a couple defects in sentence motions, paragraph motions, and joining multiple lines</li>
    </ul>
    
    <p>I've received a lot of help from contributors in this last month, which
    makes me feel like things are moving quickly.  I still think a two-month release
    cycle works well for us though, so I'll probably wait until the end of March to release this next version.</p>
</div>
<div class="newsbox">
    <div class="date">2013-02-03</div>
    <h4>Updates to unstable update site</h4>
    <p>When I released 0.28.0 a couple days ago I mentioned that I wanted to release it mostly because
    I had a pile of feature requests I was itching to work on.  Well, it's less than
    a week later and I've already got a bunch of new features available on the unstable update site. They are:</p>
    
    <ul>
        <li>Add support for '(' and ')' sentence motions</li>
        <li>Add support for Shift key in mappings '&ltS-' (e.g., &lt;S-Left&gt;)</li>
        <li>Add support for Alt (meta) key in mappings '&lt;A-' and '&lt;M-' (e.g., &lt;A-x&gt;)</li>
        <li>Add support for 'g;' and 'g,' to traverse changelist</li>
        <li>Add support for `` to jump to last cursor position</li>
        <ul>
            <li>Similar to '' to jump to last line position</li>
        </ul>
        <li>Fix issue where modifying text above a mark would offset that mark's position</li>
        <ul>
            <li>Mark positions should now stay accurate as the document changes</li>
        </ul>
    </ul>
    
    <p>It's been a productive couple of days.</p>
</div>
<div class="newsbox">
    <div class="date">2013-01-29</div>
    <h4>0.28.0 Released</h4>
    <p>The code stayed relatively idle for the last couple weeks so I'm releasing 0.28.0.
    It includes all the changes listed in the previous news post along with a few other
    minor changes:</p>
    
    <ul>
        <li>Added support for :bd, :bp, :bn, and :ls</li>
        <li>Added basic support for 'g;'</li>
        <ul>
            <li>Only goes to previous edit location, no change lists</li>
        </ul>
        <li>Fixed off-by-one issue with cursor position after exiting VisualMode</li>
    </ul>
    
    <p>We have a pile of new feature requests that I'm itching to work on so I wanted to get
    0.28.0 out into the world before I start making some destabilizing changes.</p>
    
    <p>Finally, I like to use my release announcements to include a little bit of shameless
    self-congratulation.  Vrapper has gone 7 consecutive months with 800+ installs via
    the Eclipse Marketplace in each month!  There have been a couple months where we almost
    cracked 1,000 installs in a single month but it hasn't happened yet.  This means Vrapper
    is holding its place at #50 for installs across all 1,500 Eclipse plugins available via
    the Marketplace!  I think that's pretty impressive.</p>
</div>
<div class="newsbox">
    <div class="date">2013-01-12</div>
    <h4>Updates to Unstable Update Site</h4>
    <p>I've received a lot of help from a contributor in the last couple weeks
    so a lot of minor features have been added as he learns the code.  I'm hoping
    to release the next stable version at the end of January since it appears I've
    hit my stride with a two-month release cycle.</p>
    
    <p>The unstable update site has been updated with the following changes since 0.26.1:</p>
    <ul>
        <li>Added optional PyDev plugin for Python Vrapper users</li>
        <li>Added support for &lt;C-a&gt; and &lt;C-x&gt; increment/decrement operations</li>
        <li>Added support for :only</li>
        <li>Added support for :tabnext and :tabprevious</li>
        <li>Added support for :tabnew (invokes Eclipse's New File dialog)</li>
        <li>Added support for :update (performs :w)</li>
        <li>Added support for :ascii and 'ga'</li>
        <li>Added support for :qall and :wqall</li>
        <ul>
            <li>This throws a benign NullPointerException which I can't figure out</li>
        </ul>
        <li>Added basic support for :sort</li>
        <ul>
            <li>supports '!', 'i', 'n', 'x', 'o', 'u' flags</li>
            <li>supports /pattern/ with strings only (no regex)</li>
        </ul>
        <li>Shift+Insert will now paste clipboard contents in CommandLineMode</li>
        <li>Fixed problem with multi-character operations whose first character was also used in a mapping</li>
    </ul>
    
    <p>As with all Ctrl key bindings in Vrapper, you have to unbind those keys in Eclipse first for
    Vrapper to receive them.  The same is true of Shift+Insert since Eclipse has a mapping for that too.</p>
</div>
<div class="newsbox">
    <div class="date">2012-11-26</div>
    <h4>Experimental Optional PyDev Plugin Created</h4>
    <p>Vrapper has a couple optional plugins to provide support for some Vim
    commands which require knowledge of the underlying programming language.  So
    far, we only had plugins for Java (JDT) and C/C++ (CDT). I realized today
    that it would be trivial to create an optional plugin for PyDev support and
    a couple people might actually appreciate it.  So I've created an optional
    PyDev Vrapper plugin and tossed it up on the unstable update site for
    testing.</p>
    
    <p>The PyDev plugin only provides a couple extra commands (similar to our JDT and CDT plugins):</p>
    <ul>
        <li>Ctrl+], gd, gD = Go To Declaration</li>
        <li>gR = Rename Element</li>
        <li>gc&lt;text object&gt; = Toggle Comment</li>
        <li>gcc = Toggle Comment (current line)</li>
        <li>gc also works in visual mode</li>
    </ul>
    
    <p>So there you have it.  Not a whole lot of extra features but some people might find 
    use for them while writing Python.  Give it a try and hopefully it will become stable enough
    to include in the next release of Vrapper!</p>
</div>
<div class="newsbox">
    <div class="date">2012-11-24</div>
    <h4>0.26.1 Rushed out the door</h4>
    <p>I released 0.26.0 last week but today I found a defect re-introduced that
    a lot of people had issues with the first time around.  So I've rushed a
    0.26.1 release in the hopes of delivering a fix before anyone noticed I re-introduced
    that defect.</p>
    
    <p>The problem was with multi-character mappings while in Eclipse's SmartInsert mode.
    A surprising number of people use mappings like "imap jj &lt;ESC&gt;" for exiting insert mode.
    There was a long-standing defect where entering the first character 'j' while inside parentheses,
    and not completing the mapping, would jump the cursor in front of that first 'j'.  So if you had the 'jj'
    mapping and typed something innocuous like "for(int j=0;" you would end up with "for(int =0;j)".</p>
    
    <p> The root of the issue is with Eclipse helpfully inserting that closing ')' for you.  It somehow throws
    off Vrapper and offsets the cursor.  This means I can't unit test it because it needs Eclipse
    interjecting itself for the defect to occur.  Which means it's easy to miss if I re-introduce it.</p>
    
    <p>So anyway, defect re-introduced, defect re-fixed (I hope).</p>
</div>
<div class="newsbox">
    <div class="date">2012-11-18</div>
    <h4>0.26.0 Released</h4>
    <p>I made a couple more fixes and features since my last news post.  I feel pretty confident in those changes
    and I don't have anything else to work on so I'm releasing a new version of Vrapper.</p>
    <p>In addition to the file-opening operations described in the previous post, I've made the following changes:</p>
    <ul>
        <li>Added support for Ctrl+W (delete word) in command-line modes</li>
        <li>Added support for softtabstop setting</li>
        <li>Added support for gd and gD in CDT and JDT plugins</li>
        <ul>
            <li>Go to declaration, same as Ctrl+] in those plugins</li>
        </ul>
        <li>Added ability to map function keys (F1-F20)</li>
        <li>Display "X substitutions on Y lines" after a multi-line substitution</li>
        <li>Fixed backspace in replace mode</li>
        <li>Fixed single-character mappings in insert mode</li>
        <li>Fixed '' (return to previous position) after jumping to a declaration (CDT/JDT only)</li>
        <li>Fixed cursor position after yanking visually-selected text</li>
    </ul>
    
    <p>And now for some shameless self-congratulation.  Vrapper appears to be pretty popular.  We now have
    over 10,000 installs via the <a href="http://marketplace.eclipse.org/metrics/successful_installs/alltime">Eclipse Marketplace</a>!
    Also, our <a href="https://github.com/vrapper/vrapper">GitHub Project</a> has been "starred" by over 250 users and has 40 forks.
    Very impressive.  I take this as a sign that we're doing a good job.  I'm glad everyone continues to enjoy Vrapper!</p>
</div>
<div class="newsbox">
    <div class="date">2012-10-05</div>
    <h4>Updates to Unstable Update Site</h4>
    <p>I've updated the unstable update site with a few new features.  Someone made an innocent feature request
    for the 'gf' command and I ended up implementing a bunch of features related to opening files.  These features
    can be broken into two categories, the ':e' operations and the ':find' operations.</p>
    <ul>
        <li>:e operations</li>
        <ul>
            <li>:e &lt;filename&gt; - opens a file relative to current working directory</li>
            <li>:cd &lt;directory&gt; - changes current working directory</li>
            <li>:pwd - prints current working directory</li>
            <li>:set autochdir - automatically change working directory to the parent of whatever file is active</li>
        </ul>
        <li>:find operations</li>
        <ul>
            <li>:find &lt;filename&gt; - opens a file from a directory in the path</li>
            <li>:set path=&lt;comma-delimited list of dirs&gt; - list of directories to search in path</li>
            <li>gf - takes filename under cursor, finds it in path, and opens the file</li>
            <li>v_gf - similar to gf but takes the current visual selection as the filename</li>
        </ul>
    </ul>
    <p>There are a couple things to note about my implementation.  I see Vrapper as an Eclipse plugin and not a generic
    Vim replacement so each 'path' and 'current working directory' is rooted at the Eclipse Project root of whatever
    file is active.  For example, ':e src/main/java/Foo.java'.  So, if you have two files open from two different
    projects then the project directories searched will depend on which file is active when you enter command-line mode.</p>
    
    <p>I don't plan on ever searching absolute paths on the filesystem, but if people don't like being rooted at
    the Eclipse Project level I could be convinced to go up one level further so you could specify the Eclipse
    Project name as the first-level directory. For example, ':e MyProject/src/main/java/Foo.java'.</p>
    
    <p>Finally, ':e', ':find', and ':cd' all support tab-completion like in Vim.  I'm hoping to also add tab-completion
    for command-line command names but I haven't done that yet.</p>
</div>
<div class="newsbox">
    <div class="date">2012-09-15</div>
    <h4>0.24.0 Released</h4>
    <p>I don't like sitting on unreleased features when I'm not working on anything new.  If I have a list
    of things completed but none of them are major, there's nothing that says I can't release them into the world.
    So, I am now releasing version 0.24.0 of Vrapper.  This is mostly a minor bug fix release but since
    I have nothing pending right now, I might as well give you what I have.</p>
    <p>In addition to the miscellaneous bug fixes and minor features listed in my previous news post, I've added
     the following changes:</p>
     <ul>
        <li>Refactored Ctrl+u/Ctrl+d to scroll by half-screen, not fullscreen</li>
        <li>Add support for '?' on all command-line options, not just booleans.</li>
        <ul>
            <li>For example, ':set textwidth?' or ':set clipboard?' to see current value.</li>
        </ul>
        <li>In our optional Surround plugin, added support for 'yss' to surround entire line</li>
     </ul>
     <p>That's a pretty short list of changes in the month since my last news post.  It feels like things are
     slowing down here.  The only major features left pending are the ones I don't feel confident implementing
     myself (see the previous news post about my inadequacies with the Eclipse API).  I'll still do my best
     to fix any defects that may arise, but I have nothing new on the horizon.  Other than that, hurray! A new release!</p>
</div>
<div class="newsbox">
    <div class="date">2012-08-19</div>
    <h4>Current state of the Unstable update site</h4>
    <p>Things have been pretty slow here since releasing 0.22.0.  As far as I can tell, the only major features
    Vrapper has left to implement are visual block selection and split editor views.  Unfortunately, both those
    features rely heavily on poking and prodding the Eclipse API just right and I haven't yet found the magical
    incantation to do either one of them.  I'm afraid I'll need outside help if I'm ever going to get those features
    working.</p>
    
    <p>With that said, I have found time to fix/implement a couple things in between sessions of banging my head
    against the Eclipse API.  The unstable update site currently has the following changes from 0.22.0:</p>
    
    <ul>
        <li>Fixed multi-character mappings to &lt;ESC&gt; when inside parentheses</li>
        <ul>
			<li>Vrapper now displays pending characters when typing multi-character mappings</li>
			<li>This is more aligned with Vim behavior and appears to work in all cases now</li>
        </ul>
        <li>Fixed behavior of &gt;i{ to match Vim</li>
        <li>Preserve counts sent to '.' command for future '.' executions</li>
        <li>Fixed '*' and '#' when regexsearch is enabled.</li>
        <li>Added support for '+' and '-' line motions</li>
        <li>Fixed issue with exiting InsertMode at the end of a file</li>
        <li>Added support for 'gq' paragraph formatting</li>
        <li>Refactored 'gt' and 'gT' commands for Eclipse Juno</li>
        <ul>
			<li>'gt' and 'gT' should work correctly but '&lt;number&gt;gt' still doesn't work</li>
			<li>'gt' and 'gT' in Eclipse 3.x should still work but it won't wrap around the first and last tabs anymore</li>
        </ul>
        <li>Fixed issue with newline characters being shared between editors</li>
        <ul>
			<li>If you open both a Unix file and a Windows file Vrapper will keep the newlines straight</li>
        </ul>
        <li>Defined behavior for the &lt;END&gt; key</li>
        <ul>
			<li>You'll need to unbind &lt;END&gt; in Eclipse before using it in Vrapper operations</li>
        </ul>
    </ul>
    
    <p>I don't think any of those changes are very big so I'm not itching to release the next version of Vrapper.
    We'll see what else I can add in the near future.  If things remain slow and no defects arrive, I might release it.</p>
    
</div>
<div class="newsbox">
    <div class="date">2012-07-07</div>
    <h4>0.22.0 Released</h4>
    <p>Aside from a couple minor defect fixes, the code was idle for the last 2 weeks.
    So, I'm releasing 0.22.0.  There are still a couple defects I'd like fixed, but I want to get 0.22.0 out now
    for a couple reasons.  First, I want to get the substitution feature out to everyone that has been waiting
    so patiently for it.  Second (and probably more importantly), version 0.20.0 throws a nasty stack trace during startup
    in Eclipse Juno (4.2) which leaves Vrapper in a weird state.  I fixed that issue in 0.22.0 so Vrapper will start
    correctly in Ecipse Juno for anyone who has already upgraded.  However, I haven't yet gone through and verified
    all functionality against Eclipse Juno so there may be more problems.</p>
    
    <p>Here are a couple known defects that I wanted to fix but didn't want to delay 0.22.0 for:</p>
    
    <ul>
        <li>'u' undo operation doesn't work in some files (<a href="https://github.com/vrapper/vrapper/issues/86">#86</a>)</li>
        <ul>
            <li>This is consistently a problem in certain XML files like Maven pom.xml files but is otherwise difficult to reproduce.</li>
            <li>If you run into this issue, Eclipse's normal Ctrl+Z undo will still function</li>
        </ul>
        <li>'gt' has inconsistent behavior in Eclipse Juno</li>
        <ul>
            <li>I'm going to do my best to refactor this feature so it works in both Eclipse 3.x and 4.x
            but I'm not sure if that will be possible</li>
            <li>I don't want to break compatibility with the Ecipse 3.x series so quickly after 4.2 releasing</li>
        </ul>
    </ul>
    
    <p>Aside from those issues, everything is looking good.  See the previous news posts for all the features/fixes
    we've included since 0.20.0.  I think it's a pretty impressive list given that 0.20.0 was released less than 2 months ago.</p>
</div>
<div class="newsbox">
    <div class="date">2012-06-23</div>
    <h4>2 weeks later, no new release</h4>
    <p>2 weeks ago, I said that if the code sat idle for 2 weeks I'd release 0.22.0.  Well, it didn't sit idle.
    So I'll try again; if the code can sit idle for 2 weeks I'll release version 0.22.0.</p>
    
    <p>Changes since last news posting are:</p>
    
    <ul>
        <li>Added basic support for :g, :g!, :v</li>
        <ul>
            <li>Supports 'd', 's' and 'normal' operations</li>
            <li>:g/foo/d</li>
            <li>:g/foo/s/bar/baz/</li>
            <li>:g/foo/normal wwdw</li>
            <li>:'<,'>g/foo/d</li>
            <li>:help :g</li>
        </ul>
        <li>Added support for 'unnamed' clipboard</li>
        <ul>
            <li>Uses system clipboard</li>
            <li>:set clipboard=unnamed</li>
        </ul>
        <li>Added support for 't' text object</li>
        <ul>
            <li>dit = delete contents of XML tag the cursor is within</li>
            <li>dat = delete contents and the XML tag the cursor is within</li>
        </ul>
        <li>Removed H, M, and L mapping from JDT plugin</li>
        <ul>
            <li>I don't know why they were overriding the default H, M, and L operations</li>
        </ul>
        <li>Fixed inconsistency with mouse click not updating sticky column</li>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2012-06-09</div>
    <h4>Further updates to unstable update site</h4>
    <p>Work is still progressing quickly towards a 0.22.0 release.  If I can just let the code sit idle
    for 2 weeks I'll probably release it.  There's nothing else pending that I plan on adding but we've
    had some great contributions from others lately and I don't want to rush them.</p>
    
    <p>Changes since last news posting are:</p>
    
    <ul>
        <li>Added history for all command-line based modes</li>
        <ul>
            <li>Use up/down arrow</li>
        </ul>
        <li>Added cursor to all command-line based modes</li>
        <ul>
            <li>Use left/right arrow</li>
        </ul>
        <li>Added support for 'ZZ' (like :wq) and 'ZQ' (like :q!)</li>
        <li>Added support for r&lt;character&gt; in visual mode</li>
        <li>Added more tweaks to the new substitution command</li>
        <li>Fixed issue with '.' after performing a 'R' replace</li>
        <li>Fixed issue with '.' after performing a visual-mode change</li>
        <li>Fixed issue with '.' after performing a 'gt' or 'gT'</li>
        <li>Fixed issue with '%' in visual mode</li>
        <li>Fixed compatibility issue when joining a line that begins with ')'</li>
    </ul>
    
    <p>Looking back at that list, I only worked on the easy things.
    All the difficult pieces were submitted by contributors.  Thanks for all your help!</p>
</div>
<div class="newsbox">
    <div class="date">2012-05-28</div>
    <h4>Updates to unstable update site</h4>
    <p>Vrapper version 0.20.0 was released two weeks ago and I already feel like I'm ready to release 0.22.0.
    It's been a productive weekend for me with lots of new features.  I promise to wait a little while to make
    sure these features are stable but I think there is already enough here to be a new release.</p>
    <p>The unstable update site currently includes the following features:</p>
    
<ul>
<li>Added support for the much-requested substitution feature!</li>
  <ul>
  <li>Supports the 'g' and 'i' flags</li>
  <li>Supports Eclipse's flavor of regex</li>
  <ul>
  <li>:s/foo(.+)foo/bar$1bar/</li>
  </ul>
  <li>Supports '%' and any line range definition</li>
  <ul>
  <li>:s/foo/bar/g</li>
  <li>:%s/foo/bar/g</li>
  <li>:2,5s/foo/bar/g</li>
  </ul>
  <li>This feature will require lots of testing.  It's extremely complex and nuanced.
   I'd like to hope that this implementation can cover the majority of use cases though.</li>
  </ul>
<li>Added support for '&', 'g&', and ':s' for repeating last substitution</li>
<li>Added support for '+' and '-' without leading '.' in line range operations</li>
  <ul>
  <li>:-1,+1d == :.-1,.+1d</li>
  </ul>
<li>Added support for searches in line range operations</li>
  <ul>
  <li>?something? searches above current line, /something/ searches below current line</li>
  <li>:1,/foo/d</li>
  <li>:?something?,+4y</li>
  <li>:?something?,/foo/y</li>
  </ul>
<li>Added mapping for Ctrl+N and Ctrl+P to Eclipse's word completion operation</li>
  <ul>
  <li>Eclipse operation Alt+/</li>
  <li>Not quite content assist, but it's close</li>
  </ul>
<li>Refactored save operations so they play nicely with AnyEdit plugin</li>
<li>Fixed an issue in Vrapper when 100+ files were open in Ecilpse</li>
<li>Fixed issue with performing 'undo' after disabling Vrapper</li>
</ul>

Also, a few updates to the optional CDT plugin (JDT plugin already has these features):
<ul>
    <li>Added support for 'gR' to rename element</li>
    <li>Added support for 'gc' to comment/uncomment lines</li>
    <li>Added support for '=' to auto-indent lines</li>
</ul>
    
</div>
<div class="newsbox">
    <div class="date">2012-05-14</div>
    <h4>0.20.0 Released</h4>
    <p>I let the code sit idle for two weeks and no defects were filed.  Therefore, I'm releasing Vrapper version 0.20.0 as promised.
    Rather than listing the changes since 0.18.0, I'll let you look at the previous three news posts where I listed them as they were introduced.</p>
    <p>For anyone using the unstable update site, version 0.19.20120428 is the same as 0.20.0.</p>
    <p>Thanks to all the contributors and users of Vrapper!  According to the statistics on Eclipse's Marketplace, Vrapper is the #1 vim plugin for Eclipse! 
    <a href="http://marketplace.eclipse.org/metrics/installs">http://marketplace.eclipse.org/metrics/installs</a></p>
    <p>As of this writing, Vrapper is #56 out of the 1,340 total plugins installable from Eclipse Marketplace.  Emacs+ is #46 but I won't take that personally.
    There is of course the caveat that this only tracks installs through Eclipse Marketplace.
     This means I have no idea how we rank compared to eclim since they use their own installer.</p>
</div>
<div class="newsbox">
    <div class="date">2012-04-28</div>
    <h4>0.20.0 Release Candidate</h4>
    <p>Things have been pretty slow lately and I'm running out of defects/feature requests I'm able to work on. 
    That's typically a sign that we should be able to release soon.  I've updated the unstable update site
    with version 0.19.20120428.  I'm considering this a Release Candidate.  If this version can go 2 weeks
    without any major issues found, I'll consider it stable.</p>
    
    <p>There are only a few differences between this unstable version and the previous one:</p>
    <ul>
        <li>Prevent pageUp/pageDown from being stored in the '.' register</li>
        <li>Add support for '~' in VisualMode</li>
        <li>Add support for operations (yank, delete) on line ranges in CommandMode</li>
        	<ul>
        		<li>:3,5d</li>
				<li>:3,5y</li>
				<li>:.,$y</li>
				<li>:.+3,$-2y</li>
        	</ul>
        <li>Add support for i_Ctrl-w</li>
        	<ul>
        		<li>Delete previous word while in InsertMode</li>
				<li>You'll need to unbind Ctrl+W in Eclipse before this feature will work</li>
        	</ul>
    </ul>
</div>
<div class="newsbox">
    <div class="date">2012-03-30</div>
    <h4>More updates to Unstable update site</h4>
    <p>I've updated the unstable update site again.  I'm mostly just posting here so I can keep track of the
    changes since 0.18.0.  If people are actually playing with the unstable update site and want to know what
    to test, well, that'd just be an added bonus!</p>
    
    <ul>
        <li>Implemented [{, [(, ]}, and ])</li>
    	  <ul><li>Similar to '%' except the cursor doesn't need to be on the matching parenthesis/bracket</li></ul>
        <li>Add support for Ctrl+R while in InsertMode</li>
    	  <ul>
    	      <li>Pastes contents of a register without leaving Insert Mode</li>
    	      <li>see :help i_ctrl-r</li>
    	      <li>If you have Ctrl+R mapped in Eclipse, you'll need to unbind it for this feature to work</li>
    	  </ul>
        <li>Add support for Ctrl+A while in InsertMode</li>
    	  <ul>
    	      <li>Similar to Ctrl+R, but it pastes the previous insert register without prompting the user</li>
    	      <li>see :help i_ctrl-a</li>
    	      <li>You'll need to unbind Ctrl+A in Eclipse before this feature will work</li>
    	  </ul>
        <li>Add support for Ctrl+Y/Ctrl+E while in InsertMode</li>
    	  <ul>
    	      <li>Inserts the character above/below the cursor without leaving InsertMode</li>
    	      <li>see :help i_ctrl-e</li>
    	      <li>You'll need to unbind Ctrl+E/Ctrl+Y in Eclipse before this feature will work</li>
    	      <li>This feature has some quirks.  Vrapper doesn't always know which column the cursor is in so
    	      it could grab the wrong character.  If this happens to you, do a little 'hjkl' movement and
    	      Vrapper will figure out the column.</li>
    	  </ul>
        <li>Added support for _vrapperrc for Windows people who have difficulty creating the .vrapperrc file</li>
        <li>Fixed defect when opening files while Vrapper is disabled</li>
    </ul>
    <p>Please file issues on our <a href="https://github.com/vrapper/vrapper/issues">GitHub project</a> if you run into any problems.
     I'm hoping we can consider these feature "stable" as soon as possible.</p>
</div>
<div class="newsbox">
    <div class="date">2012-03-12</div>
    <h4>Current state of the Unstable update site</h4>
    <p>We released 0.18.0 less than two months ago and there are already plenty of changes happening on our unstable update site.
    I'm not sure how long I want to wait before releasing 0.20.0.  I don't have any huge changes pending so it might be pretty soon.
    For anyone playing with the unstable update site, here are the changes since 0.18.0:</p>
    
    <ul>
        <li>Added '|' (pipe) command to move to column</li>
        <li>Added '@@' command to redo last macro</li>
        <li>Added "_ the blackhole register</li>
        <li>Added ability to check value of boolean settings with :set &lt;property&gt;?</li>
        <li>Modifying search settings (e.g. noic, hlsearch) will now modify current search</li>
        <li>Display 'recording' while recording a macro</li>
        <li>Fixed issue where vw"ap would overwrite the contents of the 'a' register</li>
        <li>Fixed issue with multi-character mappings in files with Windows line-endings (^M)</li>
        <li>Fixed issue with :9999 when last line of file is empty</li>
    </ul>
    
    <p>Thanks to all the contributors for constantly improving Vrapper!</p>
</div>
<div class="newsbox">
    <div class="date">2012-02-12</div>
    <h4>What I'd like to see in Vrapper 0.20.0</h4>
    <p>Vrapper 0.18.0 is out, time to start looking towards the next release.  Of the defects/feature requests
    already filed, here's what I'd like to see in the next Vrapper release (0.20.0):</p>
    
    <ul>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/95">#95</a> imap ,e &lt;ESC&gt; works but cannot use comma anymore.</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/386">#386</a> Add support for '@@' to repeat last macro</li>
        <li><a href="http://sourceforge.net/apps/trac/vrapper/ticket/387">#387</a> Replacing in Visual mode overwrites active register</li>
        <li><a href="https://github.com/vrapper/vrapper/issues/34">#34</a> Incorrect cursor position after mouse select</li>
        <li><a href="https://github.com/vrapper/vrapper/issues/27">#27</a> Repeating last visual-mode substitute is incomplete</li>
    </ul>
    
    <p>I'm sure more will be filed as we work, but those are the items I'm looking at right now.</p>
</div>
<div class="newsbox">
    <div class="date">2012-01-23</div>
    <h4>0.18.0 Released</h4>
    <p>I let the unstable update site sit for 2 weeks and everything looks good.  That most recent unstable build has now been pushed to the stable update site.  Yay!  Another release!</p>
    
    <p>Here are the changes since 0.16.0:</p>
    <ul>
    	<li> Added :wa[ll] command to write all dirty editors</li>
    	<li> Fixed logic on deleting/yanking last line of a file</li>
    	<li> Fixed cursor location on yank</li>
    	  <ul><li>Removed "moveonyank" setting as it is now irrelevant</li></ul>
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
    <div class="date">2012-01-09</div>
    <h4>Another Update</h4>
    <p>I had every intention of releasing 0.18.0 this week but instead I ended up fixing another couple defects. 
    I have updated the unstable update site yet again.  This version contains two fixes since the last update:</p>
    
    <ul>
		<li>Fixed defect with 'c2w' command</li>
		<li>Implemented counts for the new v/ feature (v3/{search})</li>
    </ul>
    
    <p>I don't think there are any other defects that should hold up a release.
    I'll give this current build 2 weeks before I declare it to be the stable release 0.18.0.</p>
</div>
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