<?php $page="home"; include("../includes/header.html");?>
            <div id="right">
                  <div id="download">
                      <h3 class="download">Download</h3>
                      <h4>Update Site</h4>
                    <p>
                      Stable: <a href="http://vrapper.sourceforge.net/update-site/stable">http://vrapper.sourceforge.net/update-site/stable</a><br />
                      Unstable: <a href="http://vrapper.sourceforge.net/update-site/unstable">http://vrapper.sourceforge.net/update-site/unstable</a><br />
                      <a href='http://marketplace.eclipse.org/marketplace-client-intro?mpc_install=881' title='Drag and drop into a running Eclipse workspace to install Vrapper'> 
                         <img src='http://marketplace.eclipse.org/misc/installbutton.png'/>
                      </a>
                    </p>
                      <h4>File Releases</h4>
                    <p>
                      <a href="http://sourceforge.net/projects/vrapper/files/latest/download?source=files">Latest Version</a><br />
                      <a href="https://sourceforge.net/projects/vrapper/files">Older Versions</a>
                    </p>
                  </div>
              <div id="about">
                      <h3 class="information">About</h3>
                      <p>Vrapper is an Eclipse plugin which acts as a wrapper for Eclipse text editors to provide a Vim-like input scheme for moving around and editing text.</p>
                      <p>Unlike other plugins which embed Vim in Eclipse, Vrapper imitates the behaviour of Vim while still using whatever editor you have opened in the workbench. The goal is to have the comfort and ease which comes with the different modes, complex commands and count/operator/motion combinations which are the key features behind editing with Vim, while preserving the powerful features of the different Eclipse text editors, like code generation and refactoring.</p>
                      <p>Vrapper tries to offer Eclipse users the best of both worlds.</p>
              </div>
              <div id="about">
                      <a class="float-right-noclear" href="https://github.com/vrapper/vrapper/commits/master.atom">dev rss</a>
                      <h3 class="information">Development</h3>
                      <p>Development of Vrapper has moved to our GitHub project at:<br/><a href="https://github.com/vrapper/vrapper">https://github.com/vrapper/vrapper</a></p>
                      <p>GitHub makes it much easier for contributors to submit code changes, bug fixes, and new features.  If you'd like to contribute to Vrapper just initiate a Pull Request on our GitHub project.</p>
                      <p>The SourceForge project will remain active because it hosts our website, bug tracker, wiki, forums, direct file downloads, and Eclipse update site.  The Git repository on SourceForge will only contain the state of the code for the most recent stable release.  Basically, everything other than actively developed code is still on SourceForge.</p>
                      <p>If you'd like to contribute to Vrapper but need some help, here are a couple Developer Wiki pages we've put together:<br/>
                      <a href="https://sourceforge.net/apps/trac/vrapper/wiki">Developer Wiki</a></p>
              </div>
              <script type="text/javascript" src="http://www.openhub.net/p/318170/widgets/project_basic_stats.js"></script>
        </div>
        <div>
          <div id="left">
              <a class="float-right-noclear" href="http://sourceforge.net/export/rss2_projnews.php?group_id=249013">news rss</a>
              <h3 class="news">News</h3>
<?php include("news.php"); ?>
          </div>
        </div>
<?php include("../includes/footer.html"); ?>
