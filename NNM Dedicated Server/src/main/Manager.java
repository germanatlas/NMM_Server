package main;

import java.io.IOException;
import java.time.LocalTime;
import java.util.Random;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.TitlePaneLayout;

import main.online.ClientManager;
import main.online.DataPackage;
import main.online.Server;

public class Manager {
	
	private final int TIMEOUT = 5000;
	
	private Server server;
	private ClientManager client[] = new ClientManager[2];
	private boolean clientActive[] = {false, false};
	private Game game;
	private boolean playGame = false;
	
	public Manager() {

		server = new Server();
		
		while(true) {
			
			print("Waiting for new Clients");
			client[0] = new ClientManager(this);
			clientActive[0] = true;
			client[1] = new ClientManager(this);
			clientActive[1] = true;
			
			game = new Game(this);
			playGame = true;
			
			while(playGame) {
				tick();
			}
			
		}
		
		
	}
	
	private void tick() {
		
		getInactiveClients();
		game.tick();
		
	}
	
	private void getInactiveClients() {
		
		int activeCTheo = 0, activeCPract = 0;
		for(int i = 0; i < client.length; i++) {
			
			if(clientActive[i]) {
				
				activeCTheo++;
				try {
					if(client[i].getSocket().getInetAddress().isReachable(TIMEOUT)) {
						activeCPract++;
					}
				} catch (IOException e) { }
				
				
			}
			
		}
		
		if(activeCPract < activeCTheo) {
			
			client[0].sendData(new DataPackage(99, 0, 0, 0, 0));
			client[1].sendData(new DataPackage(99, 0, 0, 0, 0));
			client[0].close();
			client[1].close();
			clientActive[0] = false;
			clientActive[1] = false;
			playGame = false;
			game = null;
			
		}
		
	}
	
	public void setGameStatus(boolean b) {
		this.playGame = b;
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
