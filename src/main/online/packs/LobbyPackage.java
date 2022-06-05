package main.online.packs;

import java.io.Serializable;

public class LobbyPackage implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String[] user;
	private int status;
	
	public LobbyPackage(String[] user, int status) {
		
		this.user = user;
		this.status = status;
		
	}
	
	public String[] getUser() {
		return user;
	}
	
	public int getStatus() {
		return status;
	}
	

}
