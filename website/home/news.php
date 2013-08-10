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

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>