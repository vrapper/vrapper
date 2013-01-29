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

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>