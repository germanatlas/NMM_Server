package main.state;

public enum LobbyPackState {
	
	DENY(0), ACCEPT(1), CHALLENGE(2),
	UPDATE(3),
	INIT(5),
	QUIT(6),
	RESEND(7),
	ACK(100);
	
	public int id;
	private LobbyPackState(int id) {
		this.id = id;
	}

}
