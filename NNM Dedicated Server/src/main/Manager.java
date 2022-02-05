package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import main.func.Game;
import main.func.maps.ClientList;
import main.func.maps.GameList;
import main.func.maps.ThreadList;
import main.local.LiteSQL;
import main.online.ClientManager;
import main.online.Server;
import main.online.packs.DataPackage;
import main.online.packs.LobbyPackage;
import main.online.packs.LoginPackage;
import main.state.LobbyPackState;
import main.state.Location;
import main.state.LoginState;

public class Manager {
	
	private final int MAXUSERS = 50;
	
	private int totalOnline;
	
	private Server server;
	private LiteSQL lSQL;
	
	private boolean active;

	private ClientList clientList;
	private GameList gameList;
	private ThreadList threadList;
	
	public Manager() {
		
		server = new Server();
		
		lSQL = new LiteSQL(this);
		lSQL.connect();
		
		clientList = new ClientList();
		gameList = new GameList();
		threadList = new ThreadList();
		
		active = true;
		
		
		Thread accepter = new Thread(() -> {
			
			accept();
			
		});
		accepter.start();
		
		Thread consoleListener = new Thread(() -> {
			
			consoleListener();
			
		});
		consoleListener.start();
		
		Thread feeder = new Thread(() -> {
			
			feed();
			
		});
		
		feeder.start();
		
		
		lobby();
		
	}

	private void lobby() {
		
		while(active) {
			
			lobbyListener();
			
			endgameListener();
			
			checkInactiveClients();
			
		}
		
	}
	
	private void feed() {
		
		double delta = 0;
		long now, lastTime = System.nanoTime();
		
		while(active) {
			now = System.nanoTime();
			delta += (now - lastTime) / (double)(1000000000);
			//print(delta + " " + now + " " + lastTime + " " + (now - lastTime));
			lastTime = now;
			if(delta >= 1) {
				
				//print("Tick");
				
				String[] list = getUsersinLobby();
				
				for(String s : list) {
					
					ClientManager tmp = clientList.getClient(s);
					
					tmp.sendData(new LobbyPackage(list, LobbyPackState.UPDATE.id));
					
				}
				
				delta--;
			} 

		}
		
	}

	private void lobbyListener() {
		
		ClientManager[] cml = clientList.getAllClients();
		
		for(ClientManager cm : cml) {
			
			if(threadList.getThread(cm.getUsername()) == null) {
				
				if(cm.getLocation() == Location.LOBBY) {
					
					Thread t = new Thread(() -> {
						
						while(cm.getLocation() == Location.LOBBY) {
							
							LobbyPackage lp = null;
							
							while((lp = (LobbyPackage) cm.receiveData()) == null);
							
							if(lp.getStatus() == LobbyPackState.CHALLENGE.id) {
								
								ClientManager nem = clientList.getClient(lp.getUser()[0]);
								
								String[] tmp = {cm.getUsername()};
								nem.sendData(new LobbyPackage(tmp, LobbyPackState.CHALLENGE.id));
								
								
							} else if(lp.getStatus() == LobbyPackState.ACCEPT.id) {
								
								ClientManager nem = clientList.getClient(lp.getUser()[0]);
								
								String[] tmp = {cm.getUsername()};
								nem.sendData(new LobbyPackage(tmp, LobbyPackState.ACCEPT.id));

								cm.setLocation(Location.GAME);
								nem.setLocation(Location.GAME);
								
								Thread nemt = threadList.getThread(nem.getUsername());
								
								if(nemt != null) {
									nemt.stop();
								}
								
								threadList.remove(cm.getUsername());
								threadList.remove(nem.getUsername());
								
								Game g = new Game(this, cm, nem);
								Thread gt = new Thread(g);
								gt.start();
								gameList.newGame(cm.getUsername(), nem.getUsername(), g);
								
							} else if(lp.getStatus() == LobbyPackState.QUIT.id) {
								//TODO
								
							}
							
						}
						
					});
					
					threadList.add(cm.getUsername(), t);
					t.start();
					
				}
				
			}
			
		}
		
	}

	private void endgameListener() {
		
		for(ClientManager cm : clientList.getAllClients()) {
			
			if(cm.getLocation() == Location.RETURN_TO_LOBBY || cm.getLocation() == Location.OFFLINE) {
				
				if(cm.getLocation() == Location.RETURN_TO_LOBBY) {
					
					cm.sendData(new LobbyPackage(getUsersinLobby(), LobbyPackState.INIT.id));
					cm.setLocation(Location.LOBBY);
					
				}
				
				gameList.removeGame(cm.getUsername());
				
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
					
				} else if(args[0].equalsIgnoreCase("list") || args[0].equalsIgnoreCase("playerlist")) {
					
					String out = "";
					
					for(String u : clientList.getUsernames()) {
						
						out += u + " ";
						
					}
					
					print("Currently active users: " + out);
					
				} else if(args[0].equalsIgnoreCase("send")) {
					
					if(args[1] != null) {
						
						clientList.getClient(args[1]).sendData(new DataPackage(0, 0, 0, 0, 0));
						
					}
					
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private void closeAllClients() {
		
		print("Closing all Clients");
		
		while(clientList.getMap().size() > 0) {
			
			clientList.getClient(clientList.getUsernames()[0]).close(); //TODO
			clientList.removeUser(clientList.getUsernames()[0]);
			
		}
		
	}

	private String[] getUsersinLobby() {
		
		ClientManager[] cmiL = getCMinLobby();
		
		String[] ret = new String[cmiL.length];
		
		for(int i = 0; i < cmiL.length; i++) {
			
			ret[i] = cmiL[i].getUsername();
			
		}
		
		return ret;
		
	}
	
	private ClientManager[] getCMinLobby() {
		
		int count = 0;
		ClientManager[] cml = clientList.getAllClients();
		for(int i = 0; i < cml.length; i++) {
			
			if(cml[i].getLocation() == Location.LOBBY) {
				
				count++;
				
			}
			
		}
		
		ClientManager[] ret = new ClientManager[count];
		
		for(int i = 0, j = 0; i < cml.length; i++) {
			
			if(cml[i].getLocation() == Location.LOBBY) {
				
				ret[j] = cml[i];
				j++;
				
			}
			
		}
		
		return ret;
		
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
			
			if(clientList.getClient(lp.getUsername()) != null) {
				
				c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.IS_ALREADY_ONLINE.id));
				c.close();
				continue acceptloop;
				
			}
			
			
			
			ResultSet rs = lSQL.query("SELECT * FROM userdata WHERE username = '" + lp.getUsername() + "'");
			
			try {
				
				if(lp.getIfNewAccount() == LoginState.REGISTER.id) { //New Account
					
					if(!rs.next()) {

						lSQL.update("INSERT INTO userdata(username, pass) VALUES('" + lp.getUsername() + "', " + lp.getPassHash() + ")");
						
						String[] cliList = getUsersinLobby();
						c.setUsername(lp.getUsername());
						c.setLocation(Location.LOBBY);
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						c.sendData(new LobbyPackage(cliList, LobbyPackState.INIT.id));
						clientList.addUser(lp.getUsername(), c);
						totalOnline++;

						print(c.getUsername() + " joined the Server for the first time\tCount: " + totalOnline);
						
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
						
						String[] cliList = getUsersinLobby();
						c.setUsername(lp.getUsername());
						c.setLocation(Location.LOBBY);
						c.sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						c.sendData(new LobbyPackage(cliList, LobbyPackState.INIT.id));
						clientList.addUser(lp.getUsername(), c);
						totalOnline++;
						
						print(c.getUsername() + " joined the Server\tCount: " + totalOnline);
						
					}
					
					
					
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
	}

	private void checkInactiveClients() {
		
		ClientManager[] cma = clientList.getAllClients();
		
		for(ClientManager cm : cma) {
			
			if(cm.getLocation() == Location.OFFLINE) {
				
				print(cm.getUsername() + " left the Server");
				gameList.removeGame(cm.getUsername());
				clientList.removeUser(cm.getUsername());
				
			}
			
		}
		
		
	}
	
	public void print(String msg) {
		
		System.out.println("[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond() + "]\t" + msg);
		
	}

	public Server getServer() {
		return server;
	}
	
	public ClientList getList() {
		return clientList;
	}

	public LiteSQL getSQL() {
		return lSQL;
	}

}
