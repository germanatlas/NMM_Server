package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.concurrent.ConcurrentHashMap;

import main.func.Game;
import main.func.ClientList;
import main.local.LiteSQL;
import main.online.ClientManager;
import main.online.Server;
import main.online.packs.LobbyPackage;
import main.online.packs.LoginPackage;
import main.state.LobbyPackState;
import main.state.Location;
import main.state.LoginState;

public class Manager {
	
	private final int	TIMEOUT = 5000,
						MAXUSERS = 50;
	
	private int index,
				totalOnline;
	
	private Server server;
	private Game[] game;
	private LiteSQL lSQL;
	
	private boolean active;

	private boolean[]	inLobby			= new boolean[MAXUSERS],
						gameActive		= new boolean[MAXUSERS/2];
	
	private String[] playerList = new String[MAXUSERS];
	
	private Thread[] receiver;

	private ClientList list;
	
	public Manager() {
		
		server = new Server();
		lSQL = new LiteSQL(this);
		lSQL.connect();
		active = true;
		receiver = new Thread[MAXUSERS];
		initReceiver();
		game = new Game[MAXUSERS/2];
		
		Thread accepter = new Thread(() -> {
			
			accept();
			
		});
		
		accepter.start();
		
		Thread consoleListener = new Thread(() -> {
			
			consoleListener();
			
		});
		
		consoleListener.start();
		
		lobby();
		
	}

	private void initList(String[] list) {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			list[i] = "";
			
		}
		
	}

	private void initReceiver() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			index = i;
			
			receiver[index] = new Thread(() -> {
				int localindex = index;
				LobbyPackage lp = null;
				while((lp = (LobbyPackage) client[localindex].receiveData()) == null);
				
				inLobby[localindex] = false;
				int nem = getIDbyUsername(lp.getUser()[0]);
				if(nem == MAXUSERS || !inLobby[nem])
					return;
				
				inLobby[nem] = false;
				receiver[nem].stop();
				String[] tmp = {client[localindex].getUsername()};
				client[nem].sendData(new LobbyPackage(tmp, LobbyPackState.CHALLENGE.id));
				
				LobbyPackage lpnem = null;
				while((lpnem = (LobbyPackage) client[nem].receiveData()) == null);
				
				if(lpnem.getStatus() == LobbyPackState.ACCEPT.id) {
					
					//TODO start new game
					int g = findLowestFree(gameActive);
					
					game[g] = new Game(this, client[localindex], client[nem]);
					gameActive[g] = true;
					client[localindex].setLocation(Location.GAME);
					client[nem].setLocation(Location.GAME);
					inLobby[localindex] = false;
					inLobby[nem] = false;
					
					
					
					
				} else {
					
					client[localindex].sendData(new LobbyPackage(lp.getUser(), LobbyPackState.DENY.id));
					
				}
				
				
			});
			
		}
		
	}

	private void lobby() {
		
		while(active) {
			
			newGame();
			
			endGame();
			
		}
		
	}

	private void endGame() {
		
		for(ClientManager cm : list.getAllClients()) {
			
			if(cm.getLocation() == Location.RETURN_TO_LOBBY) {
				
				//TODO
				
				cm.setLocation(Location.LOBBY);
				
			}
			
		}
		
	}

	private int findGameFromUsername(String username) {
		
		for(int i = 0; i < MAXUSERS/2; i++) {
			
			if(	game[i].getClient(0).getUsername() == username ||
				game[i].getClient(1).getUsername() == username ) {
				
				return i;
				
			}
			
		}
		
		return MAXUSERS / 2;
	}

	private void newGame() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(inLobby[i]) {
				
				index = i;
				if(!receiver[i].isAlive()) {
					
					receiver[index] = new Thread(() -> {
						int localindex = index;
						LobbyPackage lp = null;
						while((lp = (LobbyPackage) client[localindex].receiveData()) == null);
						
						//inLobby[localindex] = false;
						int nem = getIDbyUsername(lp.getUser()[0]);
						if(nem == MAXUSERS || !inLobby[nem])
							return;
						
						//inLobby[nem] = false;
						receiver[nem].stop();
						String[] tmp = {client[localindex].getUsername()};
						client[nem].sendData(new LobbyPackage(tmp, LobbyPackState.CHALLENGE.id));
						
						LobbyPackage lpnem = null;
						while((lpnem = (LobbyPackage) client[nem].receiveData()) == null);
						
						if(lpnem.getStatus() == LobbyPackState.ACCEPT.id) {
							
							//TODO start new game
							int g = findLowestFree(gameActive);
							
							game[g] = new Game(this, client[localindex], client[nem]);
							gameActive[g] = true;
							
							
							
							
						} else {
							
							client[localindex].sendData(new LobbyPackage(lp.getUser(), LobbyPackState.DENY.id));
							client[localindex].setLocation(Location.GAME);
							client[nem].setLocation(Location.GAME);
							inLobby[localindex] = true;
							inLobby[nem] = true;
							
						}
						
						
					});
					
					receiver[i].start();
					
				}
				
			}
			
		}
		
	}

	private void consoleListener() {
		
		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			while((line = br.readLine()) != null) {
				
				String[] args = line.split(" ");
				
				if(args[0].equalsIgnoreCase("shutdown") || args[0].equalsIgnoreCase("stop")) {
					print("Shutting down...");
					active = false;
					closeAllClients();
					lSQL.disconnect();
					server.stopServer();
					print("Server successfully shut down.");
					System.exit(0);
					
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void closeAllClients() {
		
		print("Closing all Clients");
		
		while(list.getMap().size() > 0) {
			
			list.getClient(list.getUsernames()[0]).close(); //TODO
			list.removeUser(list.getUsernames()[0]);
			
		}
		
	}

	private void accept() {
		
		acceptloop:
		while(active) {
			if(totalOnline >= MAXUSERS)
				continue;
			
			ClientManager c = new ClientManager(this);
			
			LoginPackage lp = (LoginPackage) c.receiveData();
			char[] split = lp.getUsername().toCharArray();
			
			if(split.length > 12) {
				
				c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.USERNAME_TOO_LONG.id));
				c.close();
				continue acceptloop;
				
			} else if(split.length < 3) {
				
				c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.USERNAME_TOO_SHORT.id));
				c.close();
				continue acceptloop;
				
			}
			
			for(char ch : split) {
				
				if(!Character.isLetterOrDigit(ch)) {

					c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.CONTAINS_ILLEGAL_CHAR.id));
					c.close();
					continue acceptloop;
					
				}
				
			}
			
			if(list.getClient(lp.getUsername()) != null) {
				
				c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.IS_ALREADY_ONLINE.id));
				c.close();
				
			}
			
			
			
			ResultSet rs = lSQL.search("SELECT * FROM userdata WHERE username = '" + lp.getUsername() + "'");
			
			try {
				
				if(lp.getIfNewAccount() == LoginState.REGISTER.id) { //New Account
					
					if(!rs.next()) {

						lSQL.update("INSERT INTO userdata(username, pass) VALUES('" + lp.getUsername() + "', " + lp.getPassHash() + ")");
						
						String[] cliList = list.getUsernames();
						c.setUsername(lp.getUsername());
						c.setLocation(Location.LOBBY);
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						c.sendData(new LobbyPackage(cliList, LobbyPackState.INIT.id));
						list.addUser(lp.getUsername(), c);
						totalOnline++;
						
						for(int i = 0; i < cliList.length; i++) { //UPDATE TO ALL CLIENTS
							
							ClientManager tmpCli;
							
							if((tmpCli = list.getClient(cliList[i])) != null) {
								
								String[] tmp = {lp.getUsername()};
								tmpCli.sendData(new LobbyPackage(tmp, LobbyPackState.ADD.id));
								
							}
							
						}

						print(lp.getUsername() + " joined the Server for the first time\tCount: " + totalOnline);
						
					} else {
						
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.REG_USER_ALREADY_EXISTS.id));
						c.close();
						
					}
					
				} else {
					
					if(!rs.next()) { //Username is not in the db, kick client
						
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.LOGIN_INVALID_USERNAME.id));
						c.close();
						
					} else if(lp.getPassHash() != rs.getInt("pass")) { //Pass hash is not equal to db hash, kick client

						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.LOGIN_INVALID_PASSWORD.id));
						c.close();
						
					} else {
						
						String[] cliList = list.getUsernames();
						c.setUsername(lp.getUsername());
						c.setLocation(Location.LOBBY);
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						c.sendData(new LobbyPackage(cliList, LobbyPackState.INIT.id));
						list.addUser(lp.getUsername(), c);
						totalOnline++;
						
						for(int i = 0; i < cliList.length; i++) { //UPDATE TO ALL CLIENTS
							
							ClientManager tmpCli;
							
							if((tmpCli = list.getClient(cliList[i])) != null) {
								
								String[] tmp = {lp.getUsername()};
								tmpCli.sendData(new LobbyPackage(tmp, LobbyPackState.ADD.id));
								
							}
							
						}
						
						print(lp.getUsername() + " joined the Server\tCount: " + totalOnline);
						
					}
					
					
					
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	private void getInactiveClients() {
		
		//TODO
		
	}
	
	private void initArray(boolean[] a) {
		
		for(int i = 0; i < a.length; i++)
			a[i] = false;
		
	}
	
	public void print(String msg) {
		
		System.out.println("[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond() + "]\t" + msg);
		
	}

	public Server getServer() {
		return server;
	}
	
	public ClientList getList() {
		return list;
	}

}
