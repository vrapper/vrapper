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

<a href="old_index.php">Older Posts &gt;&gt;</a><br/>