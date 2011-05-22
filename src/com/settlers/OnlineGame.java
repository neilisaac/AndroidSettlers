/*
 * This class was written while brainstorming a method of using a centralised php website 
 * to coordinate online games which could be played by multiple users connecting via 3G or wifi.
 * 
 * Sorry, it's pretty much completely unimplemented.
 */

package com.settlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URLConnection;
import java.net.URL;
import java.util.Vector;

import android.util.Log;

public class OnlineGame {

	private enum Command {
		CONNECT, GET, STATUS, TILE, ROBBER, TOWN, CITY, ROAD, VICTORY,
		PROGRESS, STEAL, USE, ADD, ROLL, END, INDEX
	}

	private static final String APP = "/game.php";
	private String request, status;
	private int index, player;

	public OnlineGame(String server, int gameId) {
		request = "http://" + server + APP + "?game=" + gameId;
		index = 0;
		status = "disconnected";
	}

	public boolean connect(String name, String password) {
		String connect = request + "&cmd=connect&name=" + name;
		if (password != null && password != "")
			connect += "&password=" + password;

		Vector<String> result = send(connect);
		if (result == null)
			return false;

		String player = get(result, "player");
		if (player == null)
			return false;

		String index = get(result, "index");
		if (index == null)
			return false;

		this.player = new Integer(player);
		this.index = new Integer(index);
		return true;
	}

	public boolean updateStatus() {
		Vector<String> result = send(request + "&cmd=status");

		String status = get(result, "status");
		if (status == null)
			return false;

		String index = get(result, "index");
		if (index == null)
			return false;

		this.index = new Integer(index);
		return true;
	}

	public boolean updateGame(Board board) {
		Vector<String> result = send(request + "&cmd=get&index=" + index);

		try {
			for (int i = 0; i < result.size(); i++) {
				String[] args = result.get(i).split(" ");
				Command command = getCommand(getString(args, 0));
				if (command == null)
					continue;

				Player player = null;
				Hexagon hexagon = null;
				Hexagon.Type type = null;
				int number = 0, value = 0;

				switch (command) {
				case CONNECT:
					player = board.getPlayer(getInt(args, 1));
					String name = getString(args, 2);
					player.setName(name);
					break;

				case ADD:
					player = board.getPlayer(getInt(args, 1));
					type = Hexagon.getType(args[2]);
					number = getInt(args, 3);
					player.addResources(type, number);
					break;

				case USE:
					player = board.getPlayer(getInt(args, 1));
					type = Hexagon.getType(getString(args, 2));
					number = getInt(args, 3);
					player.addResources(type, -number);
					break;

				case TILE:
					number = getInt(args, 1);
					type = Hexagon.getType(getString(args, 2));
					value = getInt(args, 3);
					hexagon = board.getHexagon(number);
					hexagon.setType(type);
					hexagon.setRoll(value);
					break;

				case ROLL:
					board.roll(getInt(args, 1));
					break;

				case ROBBER:
					board.setRobber(getInt(args, 1));
					break;

				case TOWN:
					player = board.getPlayer(getInt(args, 1));
					number = getInt(args, 2);
					player.build(board.getVertex(number), Vertex.TOWN);
					break;

				case CITY:
					player = board.getPlayer(getInt(args, 1));
					number = getInt(args, 2);
					player.build(board.getVertex(number), Vertex.CITY);
					break;

				case ROAD:
					player = board.getPlayer(getInt(args, 1));
					number = getInt(args, 2);
					player.build(board.getEdge(number));
					break;

				case VICTORY:
					player = board.getPlayer(getInt(args, 1));

					break;

				case PROGRESS:
					player = board.getPlayer(getInt(args, 1));
					Edge edge1 = board.getEdge(getInt(args, 2));
					Edge edge2 = board.getEdge(getInt(args, 3));
					player.build(edge1);
					player.build(edge2);
					break;

				case STEAL:
					Player to = board.getPlayer(getInt(args, 1));
					Player from = board.getPlayer(getInt(args, 2));
					type = Hexagon.getType(getString(args, 3));
					to.addResources(type, 1);
					from.useResources(type, 1);
					break;

				case END:
					board.nextPhase();
					break;
				}
			}

		} catch (NumberFormatException e) {
			Log.e(this.getClass().getName(), "bad number format format");
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.e(this.getClass().getName(), "array out of bounds");
		}

		return true;
	}

	public int getPlayerIndex() {
		return player;
	}

	public String getStatus() {
		return status;
	}

	private Vector<String> send(String command) {
		Log.d(this.getClass().getName(), "command: " + command);

		Vector<String> result = new Vector<String>();

		try {
			URL url = new URL(command);
			URLConnection connection = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String line;
			while ((line = in.readLine()) != null) {
				result.add(line);
				Log.d(this.getClass().getName(), "read: " + line);
			}

			in.close();

		} catch (MalformedURLException e) {
			Log.e(this.getClass().getName(), "malformed url");
			return null;
		} catch (IOException e) {
			Log.e(this.getClass().getName(), "http io error");
			return null;
		}

		return result;
	}

	private static String get(Vector<String> result, String key) {
		for (int i = 0; i < result.size(); i++) {
			String[] parts = result.get(i).split(" ", 2);

			if (parts == null || parts[0] == null || parts[0] == "")
				continue;

			if (parts[0] == key)
				return parts[1];
		}

		return null;
	}

	private static Command getCommand(String string) {
		Command[] commands = Command.values();
		for (int i = 0; i < commands.length; i++) {
			if (string == commands[i].toString().toLowerCase())
				return commands[i];
		}

		return null;
	}

	private static String getString(String[] args, int index)
			throws ArrayIndexOutOfBoundsException {
		if (index >= args.length)
			throw new ArrayIndexOutOfBoundsException();

		return args[index];
	}

	private static int getInt(String[] args, int index)
			throws ArrayIndexOutOfBoundsException, NumberFormatException {
		if (index >= args.length)
			throw new ArrayIndexOutOfBoundsException();

		return new Integer(args[index]);
	}
}
