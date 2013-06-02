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
if (isset($_GET['page'])) {
    $subpage = $_GET['page'];
} else {
    $subpage = 1;
}
include("../includes/header.html");
?>
<ol id="index">
<?php for($i = 1; $i <= sizeof($docs); $i++) { ?>
    <li<?php if($i == $subpage){echo ' class="selected" ';}?>><a href="?page=<?php echo $i ?>"><?php echo str_replace('_', ' ', $docs[$i-1]) ?></a></li>
<?php } ?>
</ol>
<div id="documentation">
<?php include($docs[$subpage-1].".html"); ?>
</div>
<?php include("../includes/footer.html"); ?>
