<?php $page="features"; include("../includes/header.html"); ?>
<div id="features">
<h3 class="implemented">Implemented as of 0.26.0:</h3>
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
            Basic search using /, ?, n, N, *, #
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
        <li>
            Search with support for regular expressions
        </li>
        <li>
            Search / Replace with ":%s/..."
        </li>
    </ul>
    <h3 class="considered">Considered for future versions:</h3>
    If I can ever figure out how to perform these operations with the Eclipse API I'll add these features.
    Until that time, I'm hoping someone else might come along with a solution for me.
    <ul>
        <li>
            Visual Block Selection
        </li>
        <li>
            Split Editor Views
        </li>
    </ul>
</div>
<?php include("../includes/footer.html"); ?>
