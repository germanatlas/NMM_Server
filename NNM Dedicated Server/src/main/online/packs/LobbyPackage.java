package main.online.packs;

import java.io.Serializable;

public class LobbyPackage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String user;
	private boolean accept;
	
	public LobbyPackage(String user, boolean accept) {
		
		this.user = user;
		this.accept = accept;
		
	}
	
	public String getUser() {
		return user;
	}
	
	public boolean getAccept() {
		return accept;
	}
	

}
