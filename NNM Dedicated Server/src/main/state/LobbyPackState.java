package main.state;

public enum LobbyPackState {
	
	DENY(0), ACCEPT(1), CHALLENGE(2),
	ADD(3), REMOVE(4),
	INIT(5);
	
	public int id;
	private LobbyPackState(int id) {
		this.id = id;
	}

}
