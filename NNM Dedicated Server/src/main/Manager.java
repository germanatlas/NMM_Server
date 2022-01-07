package main;

import java.time.LocalTime;
import java.util.Random;

import main.online.ClientManager;
import main.online.DataPackage;
import main.online.Server;

public class Manager {
	
	private final int	TPS = 60,
						MAXUSERS = 2;
						//MAXROOMS = MAXUSERS / 2;
	private final long TIMEPERTICK = 1000000000 / TPS;
	
	private int	totalUsers = 0;
	//			totalRooms = 0;
	//private int[][] usersInRooms = new int[MAXROOMS][2];
	
	private boolean[]	usedUsers = new boolean[MAXUSERS];
						//isInRoom = new boolean[MAXUSERS],
						//usedRooms = new boolean[MAXROOMS];
	private boolean active,
					activeUser,
					running;

	private Random random;
	
	private Server server;
	private ClientManager[] client = new ClientManager[MAXUSERS];
	//private Room[] room = new Room[MAXROOMS];
	
	private Thread t;
	
	public Manager() {
		
		server = new Server();
		active = true;
		running = false;
		
		//initiateUIR();
		
		//Connects new Clients when there's open places
		new Thread(() -> {
			
			while(active) {
				
				int i = findLowestFree(usedUsers);
				if(i == usedUsers.length + 1)
					continue;
				client[i] = new ClientManager(this);
				usedUsers[i] = true;
				totalUsers++;
				/*print(	"Connected Clients:\t" + 		totalUsers +
						"\n\t\tFree Client Spots:\t" + 	(MAXUSERS - totalUsers) +
						"\n\t\tUsed Rooms:\t\t" + 		totalRooms) + 
						"\n\t\tFree Rooms:\t\t" + 		(MAXROOMS - totalRooms));*/
				
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
	
	/*private void initiateUIR() {
		
		for(int i = 0; i < usersInRooms.length; i++) {
			
			usersInRooms[i][0] = -1;
			usersInRooms[i][1] = -1;
			
		}
		
	}*/

	private int findLowestFree(boolean[] list) {
		
		for(int i = 0; (i < list.length); i++) {
			
			if(!list[i]) {
				
				return i;
				
			}
			
		}
		return list.length + 1;
	}

	private void tick() {
		
		checkInactiveClients();
		if((usedUsers[0] && usedUsers[1])/* && (!isInRoom[0] && !isInRoom[1])*/) {
			
			/*int rID = findLowestFree(usedRooms);
			print("Init Room");
			room[rID] = new Room(client[0], client[1]);
			isInRoom[0] = true;
			isInRoom[1] = true;
			usersInRooms[rID][0] = 0;
			usersInRooms[rID][0] = 1;
			usedRooms[rID] = true;
			totalRooms++;*/

			
			
			if(!running) {

				t = new Thread(gameLoop());
				//t.run();
				
			}
		
		
		}
	}
		
	private Runnable gameLoop() {
		
		running = true;
		
		//Chooses random User to start
		random = new Random();
		activeUser = random.nextBoolean();
		
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
		if(activeUser) {
			client[0].sendData(new DataPackage(99, "1" + ((rdm)?1:0)));
			client[1].sendData(new DataPackage(99, "0" + ((rdm)?0:1)));
		} else {
			client[0].sendData(new DataPackage(99, "0" + ((rdm)?0:1)));
			client[1].sendData(new DataPackage(99, "1" + ((rdm)?1:0)));
		}
		
		
		while(client[0].getIfActive() && client[1].getIfActive()) {
			
			
			if(activeUser) {
				
				Object o = client[0].receiveData();
				
				if(o != null) {
					
					DataPackage dp = (DataPackage) o;
					
					if(dp.getStatus() <= 4) {
						
						client[1].sendData(o);
						activeUser = !activeUser;
						
					} else {
						
						client[1].sendData(o);
						
					}
					
				}
				
			} else {
				
				Object o = client[1].receiveData();
				
				if(o != null) {

					DataPackage dp = (DataPackage) o;
					
					if(dp.getStatus() <= 6) {
						
						client[0].sendData(o);
						activeUser = !activeUser;
						
					} else {
						
						client[0].sendData(o);
						
					}
					
				}
				
			}
			
		}
		
		return t;
		
	}

	private void checkInactiveClients() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(usedUsers[i]) {
				
				if(!client[i].getIfActive()) {
					
					closeClient(0);
					closeClient(1);
					
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

	/*private void closeRoom(int rID) {

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
		
	}*/

	/*private int checkIfInRoom(int i) {
		
		for (int rID = 0; rID < usersInRooms.length; rID++) {
			
			if(usersInRooms[rID][0] == i || usersInRooms[rID][1] ==  i) {
				
				return rID;
				
			}
			
		}
		
		return -1;
	}*/
	
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
