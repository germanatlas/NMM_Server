package main.online.packs;

import java.io.Serializable;

public class DataPackage implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int status, fromX, fromY, toX, toY;
	
	//Status is the same as server state enums
	
	public DataPackage(int status, int fromX, int fromY, int toX, int toY) {
		
		this.status = status;
		this.fromX = fromX;
		this.toX = toX;
		this.fromY = fromY;
		this.toY = toY;
		
		
	}
	
	public int getStatus() {
		return status;	
	}
	
	public int getToX() {
		return toX;	
	}
	
	public int getToY() {
		return toY;	
	}
	
	public int getFromX() {
		return fromX;	
	}
	
	public int getFromY() {
		return fromY;	
	}

}
