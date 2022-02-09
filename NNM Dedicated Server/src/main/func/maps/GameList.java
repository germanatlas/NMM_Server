package main.func.maps;

import java.util.concurrent.ConcurrentHashMap;

import main.func.Game;

public class GameList {

private ConcurrentHashMap<String, Game> games;
	
	public GameList() {
		
		this.games = new ConcurrentHashMap<>();
		
	}
	
	public boolean newGame(String username1, String username2, Game game) {
		
		if(this.games.get(username1 + "-" + username2) == null) {
			
			this.games.put(username1 + "-" + username2, game);
			return true;
			
		}
		
		return false;
		
	}
	
	public boolean removeGame(String username) {
		
		String[] n = getUsernames();
		
		for(int i = 0; i < games.size(); i++) {
			
			if(n == null) return false;
			
			if(n[i].contains(username)) {
				
				//If something was removed true, if not false
				if(i%2==0) {
					return (games.remove(username + "-" + getEnemyUsername(username)) != null);
				} else {
					return (games.remove(getEnemyUsername(username) + "-" + username) != null);
				}
				
			}
			
		}
		
		return false;
		
	}
	
	public String getEnemyUsername(String username) {
		
		String[] n = getUsernames();
		
		for(int i = 0; i < games.size(); i++) {
			
			if(n[i].contains(username)) {
				
				return (i%2==0)?n[i+1]:n[i-1];
				
			}
			
		}
		
		return "";
		
		
	}
	
	public Game getGame(String username) {
		
		String[] n = getUsernames();
		
		for(int i = 0; i < games.size(); i++) {
			
			if(n == null) return null;
			
			if(n[i].contains(username)) {
				
				//If something was removed true, if not false
				if(i%2==0) {
					return games.get(username + "-" + getEnemyUsername(username));
				} else {
					return games.get(getEnemyUsername(username) + "-" + username);
				}
				
			}
			
		}
		
		return null;
		
	}
	
	public ConcurrentHashMap<String, Game> getMap() {
		return this.games;
	}

	public String[] getUsernames() {
		
		String[] raw = games.keySet().toArray(new String[0]);
		String[] ret = new String[raw.length * 2];
		
		for(int i = 0; i < raw.length; i++) {
			
			ret[i*2] = raw[i].split("-")[0];
			ret[i*2 + 1] = raw[i].split("-")[1];
			
		}
		
		return null;
	}

	public Game[] getAllGames() {
		return games.values().toArray(new Game[0]);
	}
	
	public int length() {
		return games.size();
	}
	
}
