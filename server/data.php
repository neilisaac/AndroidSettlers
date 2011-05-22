<?php

umask(0);

date_default_timezone_set('America/Toronto');

function getvar($id) {
	if (!isset($_REQUEST[$id]))
		return null;
	return $_REQUEST[$id];
}

/* http://gist.github.com/385876 */
function csv_to_array($filename, $delimiter = ',')
{
    if(!file_exists($filename) || !is_readable($filename))
        return FALSE;

    $header = NULL;
    $data = array();
    if (($handle = fopen($filename, 'r')) != FALSE)
    {
        while (($row = fgetcsv($handle, 1000, $delimiter)) != FALSE)
        {
            if(!$header)
                $header = $row;
            else
                $data[] = array_combine($header, $row);
        }
        fclose($handle);
    }
    
    return $data;
}

function write_file($name, $content) {
	$fh = fopen($name, 'w') or die("can't open file $name");
	fwrite($fh, $content);
	fclose($fh);
}

function get_players($game) {
	if (!file_exists("games/$game/players"))
		return null;
		
	return csv_to_array("games/$game/players");
}

// FIXME: don't keep tacking on ,
function save_players($game, $players) {
	$content = "";
	$header = false;
	
	foreach ($players as $player) {
		if (!$header) {
			foreach (array_keys($player) as $key)
				$content .= "$key,";
			$content .= "\n";
			$header = true;
		}
		
		foreach($player as $value)
			$content .= "$value,";
		$content .= "\n";
	}
	
	write_file("games/$game/players", $content);
}

function put_command($game, $command) {
	$fh = fopen("games/$game/commands", 'a') or die("can't open game file $name");
	fwrite($fh, "$command\n");
	fclose($fh);
}

function read_commands($game, $index = 0) {
	$lines = file("games/$game/commands");
	if (!$lines) die("can't read game file for $game");
	
	if ($index > 0)
		return array_slice($lines, $index);
	
	return $lines;
}

function num_commands($game) {
	return count(file("games/$game/commands"));
}

function set_status($game, $status) {
	write_file("games/$game/status", $status);
}

function get_status($game) {
	return file_get_contents("games/$game/status");
}

function get_password($game) {
	if (!file_exists("games/$game/password"))
		return null;
	
	return file_get_contents("games/$game/password");
}

function get_name($game) {
	return file_get_contents("games/$game/name");
}

function get_start($game) {
	return file_get_contents("games/$game/date");
}

function create_game_data($name, $password = null) {
	$game = 0;
	while (true) {
		$game = mt_rand(100000, 999999);
		if (!file_exists("games/$game/commands"))
			break;
	}
	
	mkdir("games/$game", 0777, true);
	if (!touch("games/$game/commands") || ! touch("games/$game/players"))
		die("can't create game $game");
	
	if ($password && $password != "")
		write_file("games/$game/password", $password);
	
	write_file("games/$game/date", date("H:i D j M Y"));
	
	write_file("games/$game/name", $name);
	
	return $game;
}

?>

