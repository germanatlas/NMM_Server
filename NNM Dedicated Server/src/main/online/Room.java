package main.online;

import java.util.Random;

public class Room {
	
	private ClientManager	c1,
							c2;
	private Random random;
	private boolean	startUser,
					activeUser;
	
	private Thread t;
	
	public Room(ClientManager c1, ClientManager c2) {
		
		System.out.println("Init Room");
		this.c1 = c1;
		this.c2 = c2;
		this.random = new Random();
		
		//Chooses random User to start
		this.startUser = random.nextBoolean();
		activeUser = startUser;
		
		//assigns random Color for Users
		boolean rdm = random.nextBoolean();
		
		/*
		 * 99: Code for new Game
		 * First 1/0:
		 * 		1: you start
		 * 		0: other person starts
		 * Second 1/0:
		 * 		1: youre Black
		 * 		0: youre White
		 * 
		 * */
		if(startUser) {
			c1.sendData(new DataPackage(99, "1" + ((rdm)?1:0)));
			c2.sendData(new DataPackage(99, "0" + ((rdm)?0:1)));
		} else {
			c1.sendData(new DataPackage(99, "0" + ((rdm)?0:1)));
			c2.sendData(new DataPackage(99, "1" + ((rdm)?1:0)));
		}
		
		t = new Thread(() -> {
			
			if(activeUser) {
				
				Object o = c1.receiveData();
				
				if(o != null) {
					
					DataPackage dp = (DataPackage) o;
					
					if(dp.getStatus() <= 4) {
						
						c2.sendData(o);
						activeUser = !activeUser;
						
					} else {
						
						c2.sendData(o);
						
					}
					
				}
				
				
				
			} else {
				
				Object o = c2.receiveData();
				
				if(o != null) {

					DataPackage dp = (DataPackage) o;
					
					if(dp.getStatus() <= 6) {
						
						c1.sendData(o);
						activeUser = !activeUser;
						
					} else {
						
						c1.sendData(o);
						
					}
					
				}
				
			}
			
		});
		
	}
	
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

	public void tick() {
		
		if(!t.isAlive() && c1.getIfActive() && c2.getIfActive()) {
			
			t.start();
			
		}
		
	}

}
