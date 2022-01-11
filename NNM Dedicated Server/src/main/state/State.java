package main.state;

public enum State {
	
	PLACE(0), MOVE(1), JUMPONE(3), JUMPTWO(4), JUMPBOTH(5), MILL(6), WIN(7), STALEMATE(8), 
	YOUMILL(20), NMYMILL(21), YOUWIN(23), NMYWIN(24),
	NOTALLOWED(50), ALLOWED(51),
	NEW(98), END(99);
	public int id;

	private State(int id) {
		this.id = id;
	}
}
