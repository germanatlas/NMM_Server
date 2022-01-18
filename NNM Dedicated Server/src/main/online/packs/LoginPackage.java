package main.online.packs;

import java.io.Serializable;

public class LoginPackage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int passHash;
	
	public LoginPackage(String username, int passHash) {
		
		this.username = username;
		this.passHash = passHash;
		
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getPassHash() {
		return passHash;
	}

}
