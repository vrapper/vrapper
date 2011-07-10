<?php $page="features"; include("../includes/header.html"); ?>
<div id="features">
<h3 class="implemented">Implemented as of 0.16.0:</h3>
    <ul>
        <li>
            Operators (d, c, y, p, ...
            <a href="../documentation/?page=3#operators">full list</a>)
        </li>
        <li>
            Motions (h, j, w, e, ...
            <a href="../documentation/?page=3#motions">full list</a>)
        </li>
        <li>
            Text Objects (aw, iw, a", ...
            <a href="../documentation/?page=3#text_objects">full list</a>)
        </li>
        <li>
            Counts to repeat operators and motions
        </li>
        <li>
            The famous dot operator to repeat the last change
        </li>
        <li>
            Registers (unnamed, named (a-z), read-only ("." and "/"))
        </li>
        <li>
            Marks (named, "'", ".", "^")
        </li>
        <li>
            Basic search using /, ?, n, N, *, #, no regular expressions supported (yet)
        </li>
        <li>
            Incremental search
        </li>
        <li>
            Search highlighting
        </li>
        <li>
            Visual mode to highlight text
        </li>
        <li>
            Key mappings and macros
        </li>
        <li>
            Works with all editors based on the "AbstractTextEditor" class
        </li>
        <li>
            All features of the "wrapped" editor are still available
        </li>
    </ul>
<h3 class="planned">Planned for future versions:</h3>
<ul>
    <li>
        Search with support for regular expressions
    </li>
    <li>
        Search / Replace with ":%s/..." or similar
    </li>
    <li>
        Visual Block selection
    </li>
</ul>
</div>
<?php include("../includes/footer.html"); ?>
