package main.state;

public enum Location {
	
	LOGIN(0), LOBBY(1), GAME(2), RETURN_TO_LOBBY(3), OFFLINE(4);
	
	public int id;
	private Location(int id) {
		this.id = id;
	}

}
