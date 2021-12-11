package main.online;

public class DataPackage {
	
	private int status;
	private String move;
	
	/*
	 * status:
	 * 0 - active game - placing
	 * 1 - active game
	 * 2 - draw
	 * 3 - white won
	 * 4 - black won
	 * 5 - white has mill
	 * 6 - black has mill
	 * 99 - game start
	 * 
	 * */
	
	public DataPackage(int status, String move) {
		
		this.status = status;
		this.move = move;
		
	}
	
	public int getStatus() {
		return status;	
	}
	
	public String getmove() {
		return move;
	}

}
