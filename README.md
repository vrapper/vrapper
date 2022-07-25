![vrapper](https://github.com/vrapper/vrapper/raw/master/website/img/vrapper_logo.png)

Vim-like editing in Eclipse  [![Join the chat at https://gitter.im/vrapper/vrapper](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/vrapper/vrapper?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
==========
Best of Both Worlds!
----------
Vrapper is an Eclipse plugin which acts as a wrapper for Eclipse text editors to provide a Vim-like input scheme for moving around and editing text.
Unlike other plugins which embed Vim in Eclipse, Vrapper imitates the behaviour of Vim while still using whatever editor you have opened in the workbench. 
The goal is to have the comfort and ease which comes with the different modes, complex commands and count/operator/motion combinations which are the key features behind editing with Vim, while preserving the powerful features of the different Eclipse text editors, like code generation and refactoring.

How it Works
----------
How it works
Instead of embedding Vim in Eclipse or creating a new text (Eclipse) editor with Vim functionality from scratch, Vrapper adds a layer on top of existing editors like the Java editor. The advantage of this approach is that all the features of the original editor are still available. For example it is still possible to use the refactoring capabilities of the Java editor.

While Vrapper is active, it adds a listener to every editor which is opened. Every keystroke send to the editor is then evaluated by Vrapper. In insert mode, keystrokes are simply passed to the underlying editor (unless they are remapped to something else). In other modes (e.g. visual or normal mode) most keystrokes will result in some action performed by Vrapper (e.g. delete, paste).

Targeted Users
-----------
Vrapper is neither able nor intended to be a replacement for Vim. It is meant to enhance the text editors of the Eclipse platform.

If you are a Vim user who is using Eclipse because of some specific features or want a faster way of editing text in Eclipse, Vrapper might be just what you are looking for. On the other hand, if you are a long-time Vim user and do not need any features of Eclipse editors, the alternatives below might suit you better.

Links
----------
For more details visit:

**Stable Update Site:** http://vrapper.sourceforge.net/update-site/stable

**Unstable Update Site:** http://vrapper.sourceforge.net/update-site/unstable

License: [GPL v3 or any later version](LICENSE.md)

Home Page: http://vrapper.sourceforge.net/home/

Documentation: http://vrapper.sourceforge.net/documentation/

Developer Wiki: https://github.com/vrapper/vrapper/wiki
