<?php

header("Content-type: text/plain");

require_once("data.php");

$cmd = getvar("cmd");
print "'$cmd'\n";

if ($cmd == "create") {
	$name = getvar("name");
	$password = getvar("password");
	
	$game = create_game_data($name, $password);

	$players = array();
	for ($i = 0; $i < 4; $i++) {
		$player = array();
		$player['number'] = $i;
		$player['name'] = "nobody";
		$player['wood'] = 0;
		$player['wool'] = 0;
		$player['wheat'] = 0;
		$player['brick'] = 0;
		$player['ore'] = 0;
		$players[$i] = $player;
	}

	save_players($game, $players);

	set_status($game, "waiting for host to join");

	print "server: settlers.neilisaac.ca\n";
	print "game id: $game\n";
	
	if ($password)
		print "password: $password\n";
	else
		print "no password\n";
	
	put_command($game, "$cmd");
}

else if ($cmd == "get") {
	$game = getvar("game");
	$index = getvar("index");
	foreach(read_commands($game, $index) as $command)
		print $command;
}

else if ($cmd == "status") {
	$game = getvar("game");
	print get_status($game);
	print "\n";
	print num_commands($game);
}

else {
	$game = getvar("game");
	$players = get_players($game);
	if (!players)
		die("invalid game id");
	
	switch ($cmd) {	
		case "connect":
			$name = getvar("name");
			$password = getvar("password");
			
			if ($name == "nobody")
				die("invalid name");
				
			$check = get_password($game);
			if ($check != "" && (!$password || $password != $check))
				die("incorrect password");
	
			$valid = false;
			for ($i = 0; $i < 4; $i++) {
				if ($players[$i]['name'] == 'nobody') {
					$players[$i]['name'] = $name;
					print "index $i\n";
					$valid = true;
					break;
				}
			}
			
			if (!$valid)
				die("game is full");
			
			save_players($game, $players);
			$cmd .= " $name";
			break;
		
		case "add":
			$player = getvar("player");
			$type = getvar("type");
			$number = getvar("number");
			$players[$player][$type] += $number;
			save_players($game, $players);
			$cmd .= " $player $type $number";
			break;
		
		case "use":
			$player = getvar("player");
			$type = getvar("type");
			$number = getvar("number");
			$players[$player][$type] -= $number;
			save_players($game, $players);
			$cmd .= " $player $type $number";
			break;
		
		case "tile":
			$index = getvar("index");
			$type = getvar("type");
			$cmd .= " $index $type";
			break;
		
		case "roll":
			$number = getvar("number");
			$cmd .= " $number";
			break;
		
		case "robber":
		case "settlement":
		case "city":
		case "road":
			$player = getvar("player");
			$index = getvar("index");
			$cmd .= " $player $index";
			break;
		
		case "victory":
			$player = getvar("player");
			$cmd .= " $player";
			break;
		
		case "progress":
			$player = getvar("player");
			$index1 = getvar("index1");
			$index2 = getvar("index2");
			$cmd .= " $player $index1 $index2";
			break;
	
		case "steal":
			$to = getvar("to");
			$from = getvar("from");
			$type = getvar("type");
			$cmd .= " $to $from $type";
			break;
		
		case "card":
			$player = getvar("player");
			$type = getvar("type");
			$cmd .= " $player $type";
			break;
	
		case "end":
			$player = getvar("player");
			$cmd .= " $player";
			break;
	}
	
	put_command($game, $cmd);
	print num_commands($game);
}

?>

