package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

import main.local.LiteSQL;
import main.online.ClientManager;
import main.online.Server;
import main.online.packs.DataPackage;
import main.online.packs.LobbyPackage;
import main.online.packs.LoginPackage;
import main.state.Location;
import main.state.LoginState;

public class Manager {
	
	private final int	TIMEOUT = 5000,
						MAXUSERS = 100;
	
	private int index;
	
	private Server server;
	private ClientManager client[];
	private Game game[];
	private LiteSQL lSQL;
	
	private boolean active;

	private boolean[]	clientActive	= new boolean[MAXUSERS],
						inLobby			= new boolean[MAXUSERS],
						gameActive		= new boolean[MAXUSERS/2];
	
	private Thread receiver[];
	
	public Manager() {
		
		server = new Server();
		lSQL = new LiteSQL(this);
		lSQL.connect();
		initArray(clientActive);
		initArray(inLobby);
		initArray(gameActive);
		active = true;
		receiver = new Thread[MAXUSERS];
		client = new ClientManager[MAXUSERS];
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

	private void lobby() {
		
		while(active) {
			
			newGame();
			
			endGame();
			
		}
		
	}

	private void endGame() {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(clientActive[i] && !inLobby[i]) {
				
				if(client[i].getLocation() == Location.LOBBY) {
					
					int g = findGameFromUsername(client[i].getUsername());
					inLobby[getIDbyUsername(game[g].getClient(0).getUsername())] = true;
					inLobby[getIDbyUsername(game[g].getClient(1).getUsername())] = true;
					
					
				}
				
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
						
						inLobby[localindex] = false;
						int nem = getIDbyUsername(lp.getUser());
						if(nem == MAXUSERS || !inLobby[nem])
							return;
						
						inLobby[nem] = false;
						receiver[nem].stop();
						client[nem].sendData(new LobbyPackage(client[localindex].getUsername(), true));
						
						LobbyPackage lpnem = null;
						while((lpnem = (LobbyPackage) client[nem].receiveData()) == null);
						
						if(lpnem.getAccept()) {
							
							//TODO start new game
							int g = findLowestFree(gameActive);
							
							game[g] = new Game(this, client[localindex], client[nem]);
							gameActive[g] = true;
							
							
							
						} else {
							
							client[localindex].sendData(new LobbyPackage(lp.getUser(), false));
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

	private int getIDbyUsername(String user) {
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(client[i].getUsername().equals(user)) {
				
				return i;
				
			}
			
		}
		
		return MAXUSERS;
	}

	private void consoleListener() {
		
		String line = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		try {
			while((line = br.readLine()) != null) {
				
				String[] args = line.split(" ");
				
				if(args[0].equalsIgnoreCase("shutdown")) {
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
		
		for(int i = 0; i < MAXUSERS; i++) {
			
			if(clientActive[i]) {
				
				client[i].close();
				clientActive[i] = false;
				inLobby[i] = false;
				
			}
			
		}
		
	}

	private void accept() {
		
		while(active) {
			int n = findLowestFree(clientActive);
			if(n == MAXUSERS)
				continue;
			
			client[n] = new ClientManager(this);
			
			LoginPackage lp = (LoginPackage) client[n].receiveData();
			ResultSet rs = lSQL.search("SELECT * FROM userdata WHERE username = '" + lp.getUsername() + "'");
			
			try {
				
				if(lp.getIfNewAccount() == 1) { //New Account
					
					if(!rs.next()) {

						lSQL.update("INSERT INTO userdata(username, pass) VALUES('" + lp.getUsername() + "', " + lp.getPassHash() + ")");
						
						client[n].setUsername(lp.getUsername());
						client[n].setLocation(Location.LOBBY);
						clientActive[n] = true;
						inLobby[n] = true;
						client[n].sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						
						
					} else {
						
						client[n].sendData(new LoginPackage(lp.getUsername(), 0, LoginState.REG_USER_ALREADY_EXISTS.id));
						client[n].close();
						
					}
					
				} else {
					
					if(!rs.next()) { //Username is not in the db, kick client TODO
						client[n].sendData(new LoginPackage(lp.getUsername(), 0, LoginState.LOGIN_INVALID_USERNAME.id));
						client[n].close();
					}
					
					int hash = 0;
					hash = rs.getInt("pass");
					
					if(lp.getPassHash() != hash) { //Pass hash is not equal to db hash, kick client TODO

						client[n].sendData(new LoginPackage(lp.getUsername(), 0, LoginState.LOGIN_INVALID_PASSWORD.id));
						client[n].close();
						
					} else {
						
						client[n].setUsername(lp.getUsername());
						client[n].setLocation(Location.LOBBY);
						clientActive[n] = true;
						inLobby[n] = true;
						client[n].sendData(new LoginPackage(lp.getUsername(), 0, LoginState.ACCEPT.id));
						
					}
					
				}
				
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	private void getInactiveClients() {
		
		try {
			if(	clientActive[0] && clientActive[1] &&
				client[0].getSocket().getInetAddress().isReachable(TIMEOUT) &&
				client[1].getSocket().getInetAddress().isReachable(TIMEOUT)) {
				return;
			}
		} catch (IOException e) { }
		
		client[0].sendData(new DataPackage(99, 0, 0, 0, 0));
		client[1].sendData(new DataPackage(99, 0, 0, 0, 0));
		client[0].close(); clientActive[0] = false;
		client[1].close(); clientActive[1] = false;
		
	}
	
	private void initArray(boolean[] a) {
		
		for(int i = 0; i < a.length; i++)
			a[i] = false;
		
	}

	private int findLowestFree(boolean[] a) {
		
		for(int i = 0; i < a.length; i++) {
			
			if(!a[i]) {
				return i;
			}
			
		}
		
		return a.length;
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
	
	public boolean[] getActiveClientList() {
		return clientActive;
	}

}
