package main;

import main.online.ClientManager;
import main.online.Room;
import main.online.Server;

public class Manager {
	
	private static final int	TPS = 10,
								MAXUSERS = 2,
								MAXROOMS = MAXUSERS / 2;
	private static final long TIMEPERTICK = 1000000000 / TPS;
	private int[][] usersInRooms = new int[MAXROOMS][2];
	private boolean[]	usedUsers = new boolean[MAXUSERS],
						isInRoom = new boolean[MAXUSERS],
						usedRooms = new boolean[MAXROOMS];
	private boolean active;
	private Server server;
	private ClientManager[] client = new ClientManager[MAXUSERS];
	private Room[] room = new Room[MAXROOMS];
	
	public Manager() {
		
		server = new Server();
		active = true;
		
		initiateUIR();
		
		Thread clientWait = new Thread(waitForClient());
		clientWait.start();

		double delta = 0;
		long now, lastTime = System.nanoTime();
		
		while(true) {
			now = System.nanoTime();
			delta += (now - lastTime) / TIMEPERTICK;
			lastTime = now;
			
			if(delta >= 1) {
				tick();
				delta--;
			} 

		}
		
		
		
	}
	
	private void initiateUIR() {
		
		for(int i = 0; i < usersInRooms.length; i++) {
			
			usersInRooms[i][0] = -1;
			usersInRooms[i][1] = -1;
			
		}
		
	}

	private Runnable waitForClient() {
		
		while(active) {
			
			int i = findLowestFree(usedUsers);
			if(i == usedUsers.length + 1)
				continue;

			client[i] = new ClientManager(this);
			usedUsers[i] = true;
			
		}
		return null;
		
	}

	private int findLowestFree(boolean[] list) {
		
		int nofree = list.length + 1;
		for(int i = 0; (i < list.length); i++) {
			
			if(!list[i]) {
				
				return i;
				
			}
			
		}
		return nofree;
	}

	private void tick() {
		
		checkInactiveClients();
		
		if((usedUsers[0] && usedUsers[1]) && (!isInRoom[0] && !isInRoom[1])) {
			
			int rID = findLowestFree(usedRooms);
			room[rID] = new Room(client[0], client[1]);
			isInRoom[0] = true;
			isInRoom[1] = true;
			usersInRooms[rID][0] = 0;
			usersInRooms[rID][0] = 1;
			
		}
		
		tickAllRooms();
		
		
	}

	private void tickAllRooms() {
		
		for(int i = 0; i < MAXROOMS; i++) {
			
			if(usedRooms[i]) {
				
				room[i].tick();
				
			}
			
		}
		
	}

	private void checkInactiveClients() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(usedUsers[i]) {
				
				if(!client[i].getIfActive()) {
					
					closeClient(i);
					
					int rID = checkIfInRoom(i);
					if(rID >= 0) {
						
						closeRoom(rID);
						usersInRooms[rID][0] = -1;
						usersInRooms[rID][1] = -1;
						
					}
					
				}
				
			}
			
		}
		
	}

	private void closeClient(int i) {

		usedUsers[i] = false;
		client[i] = null;
		
	}

	private void closeRoom(int rID) {

		usedRooms[rID] = false;
		room[rID] = null;
		
	}

	private int checkIfInRoom(int i) {
		
		for (int rID = 0; rID < usersInRooms.length; rID++) {
			
			if(usersInRooms[rID][0] == i || usersInRooms[rID][1] ==  i) {
				
				return rID;
				
			}
			
		}
		
		return -1;
	}

	public Server getServer() {
		return server;
	}
	
	public ClientManager getClient(int id) {
		return client[id];
	}

}
