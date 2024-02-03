<?php
$page="documentation";
$docs = array(
    "introduction",
    "basics",
    "commands",
    "search",
    "visual_mode",
    "configuration",
    "commandline_commands",
    "macros",
    "marks_and_registers",
    "optional_plugins"
);
if (isset($_GET['page']) && $_GET['page'] > 0 && $_GET['page'] <= count($docs)) {
    # Send "Moved permanently to new URL", hopefully user changes bookmarks.
    header("Location: index.php?topic=" . $docs[$_GET['page'] - 1], true, 301);
    exit;
} else if (isset($_GET['topic'])) {
    $subpageId = array_search($_GET['topic'], $docs);
    if ($subpageId === FALSE) {
        http_response_code(404);
        exit;
    }
} else {
    $subpageId = 0;
}
$title = "Documentation &mdash; " . ucwords(str_replace('_', ' ', $docs[$subpageId]));
include("../includes/header.html");
?>
<ol id="index">
<?php for($i = 0; $i < sizeof($docs); $i++) { ?>
    <li<?php if($i == $subpageId){echo ' class="selected" ';}?>><a href="?topic=<?php echo $docs[$i] ?>"><?php echo str_replace('_', ' ', $docs[$i]) ?></a></li>
<?php } ?>
</ol>
<div id="documentation">
<?php include($docs[$subpageId].".html"); ?>
</div>
<?php include("../includes/footer.html"); ?>
