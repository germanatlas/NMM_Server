package main;

import java.time.LocalTime;

import main.online.ClientManager;
import main.online.DataPackage;
import main.online.Room;
import main.online.Server;

public class Manager {
	
	private final int	TPS = 60,
						MAXUSERS = 2,
						MAXROOMS = MAXUSERS / 2;
	private final long TIMEPERTICK = 1000000000 / TPS;
	
	private int	totalUsers = 0,
				totalRooms = 0;
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
		
		//Connects new Clients when there's open places
		new Thread(() -> {
			
			while(active) {
				
				int i = findLowestFree(usedUsers);
				if(i == usedUsers.length + 1)
					continue;
				client[i] = new ClientManager(this);
				usedUsers[i] = true;
				totalUsers++;
				print(	"Connected Clients:\t" + 		totalUsers +
						"\n\t\tFree Client Spots:\t" + 	(MAXUSERS - totalUsers) +
						"\n\t\tUsed Rooms:\t\t" + 		totalRooms + 
						"\n\t\tFree Rooms:\t\t" + 		(MAXROOMS - totalRooms));
				
			}
			
		}).start();

		double delta = 0;
		double now, lastTime = System.nanoTime();
		
		while(true) {
			now = System.nanoTime();
			delta += (now - lastTime) / TIMEPERTICK;
			lastTime = now;
			//print("\tTest" + delta);
			
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
			print("Init Room");
			room[rID] = new Room(client[0], client[1]);
			isInRoom[0] = true;
			isInRoom[1] = true;
			usersInRooms[rID][0] = 0;
			usersInRooms[rID][0] = 1;
			usedRooms[rID] = true;
			totalRooms++;
			
		}
		
	}

	private void checkInactiveClients() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(usedUsers[i]) {
				
				if(!client[i].getIfActive()) {
					
					int rID = checkIfInRoom(i);
					if(rID >= 0) {
						
						//Closes both clients in a room
						closeRoom(rID);
						
					} else {
						
						//Closes just this client
						closeClient(i);
						
					}
					
					print(	"Connected Clients:\t" + 		totalUsers +
							"\n\t\tFree Client Spots:\t" + 	(MAXUSERS - totalUsers) +
							"\n\t\tUsed Rooms:\t\t" + 		totalRooms + 
							"\n\t\tFree Rooms:\t\t" + 		(MAXROOMS - totalRooms));
					
					
				}
				
			}
			
		}
		
	}

	private void closeClient(int i) {

		client[i].sendData(new DataPackage(98, ""));
		usedUsers[i] = false;
		client[i].close();
		client[i] = null;
		totalUsers--;
		print("Client " + i + " disconnected.");
		
	}

	private void closeRoom(int rID) {

		totalRooms--;
		usedRooms[rID] = false;
		if(room[rID].getComThread().isAlive())
			try {
				room[rID].getComThread().join();
			} catch (InterruptedException e) {
				print("Couldnt close Communication Thread of Room " + rID);
			}
	
		closeClient(usersInRooms[rID][0]);
		closeClient(usersInRooms[rID][1]);
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
	
	public void print(String msg) {
		
		System.out.println("[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond() + "]\t" + msg);
		
	}

	public Server getServer() {
		return server;
	}
	
	public ClientManager getClient(int id) {
		return client[id];
	}

}
