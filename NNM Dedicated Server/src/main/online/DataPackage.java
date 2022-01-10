package main.online;

import java.io.Serializable;

public class DataPackage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	private int status;
	private String move;
	
	/*
	 * status:
	 * 1 - active game
	 * 2 - draw
	 * 3 - white won
	 * 4 - black won
	 * 5 - mill
	 * 6 - mill removal
	 * 98 - ended game
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
