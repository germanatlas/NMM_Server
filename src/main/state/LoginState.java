package main.state;

public enum LoginState {
	
	LOGIN(0), REGISTER(1),
	ACCEPT(2), 
	REG_USER_ALREADY_EXISTS(3), 
	LOGIN_INVALID_USERNAME(4), LOGIN_INVALID_PASSWORD(5),
	USERNAME_TOO_LONG(6), USERNAME_TOO_SHORT(7),
	CONTAINS_ILLEGAL_CHAR(8),
	IS_ALREADY_ONLINE(9);
	
	public int id;
	private LoginState(int id) {
		this.id = id;
	}

}
