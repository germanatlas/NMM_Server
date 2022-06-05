package main.online.packs;

import java.io.Serializable;

public class LoginPackage implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String username;
	private int passHash;
	private int newAccount;
	
	public LoginPackage(String username, int passHash, int newAccount) {
		
		this.username = username;
		this.passHash = passHash;
		this.newAccount = newAccount;
		
	}
	
	public String getUsername() {
		return username;
	}
	
	public int getPassHash() {
		return passHash;
	}
	
	public int getIfNewAccount() {
		return newAccount;
	}

}
