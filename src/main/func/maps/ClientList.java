package main.func.maps;

import java.util.concurrent.ConcurrentHashMap;

import main.online.ClientManager;

public class ClientList {
	
	private ConcurrentHashMap<String, ClientManager> clients;
	
	public ClientList() {
		
		this.clients = new ConcurrentHashMap<>();
		
	}
	
	public boolean addUser(String username, ClientManager client) {
		
		if(this.clients.get(username) == null) {
			
			this.clients.put(username, client);
			return true;
			
		}
		
		return false;
		
	}
	
	public boolean removeUser(String username) {
		return (this.clients.remove(username) != null);
	}
	
	public ClientManager getClient(String username) {
		return this.clients.get(username);
	}
	
	public ConcurrentHashMap<String, ClientManager> getMap() {
		return this.clients;
	}

	public String[] getUsernames() {
		return clients.keySet().toArray(new String[0]);
	}

	public ClientManager[] getAllClients() {
		return clients.values().toArray(new ClientManager[0]);
	}
	
	public int length() {
		return clients.size();
	}
	
	

}
