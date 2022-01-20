package main.state;

public enum LoginState {
	
	ACCEPT(2), 
	REG_USER_ALREADY_EXISTS(3), 
	LOGIN_INVALID_USERNAME(4), LOGIN_INVALID_PASSWORD(5);
	
	public int id;
	private LoginState(int id) {
		this.id = id;
	}

}
