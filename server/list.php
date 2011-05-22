<?php

require_once("data.php");

$folders = glob("games/*");

$count = count($folders);

if ($count > 0) {
	print "<table>\n";
	print "<tr><th>game id</th><th>players</th><th>status</th>";
	print "<th>start time</th>";
	print "<th>password</th><th>actions</th></tr>\n";
	
	foreach ($folders as $folder) {
		$game = basename($folder);
		$name = get_name($game);
		$players = get_players($game);
		$name1 = $players[0]['name'];
		$name2 = $players[1]['name'];
		$name3 = $players[2]['name'];
		$name4 = $players[3]['name'];
		$status = get_status($game);
		$date = get_start($game);
		$password = get_password($game) ? "yes" : "no";
		$join = "<a href=\"join.php?game=$game\">join</a>";
		
		print "<tr>\n";
		print "<td>$game $name</td>";
		print "<td>$name1, $name2, $name3, $name4</td>\n";
		print "<td>$status</td>\n";
		print "<td>$date</td>\n";
		print "<td>$password</td>\n";
		print "<th>$join</th>\n";
		print "</tr>\n";
	}
	
	print "</table>\n";
}

?>

