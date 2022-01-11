package main;

import java.time.LocalTime;
import java.util.Random;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.TitlePaneLayout;

import main.online.ClientManager;
import main.online.DataPackage;
import main.online.Server;

public class Manager {
	
	private Server server;
	private ClientManager client[] = new ClientManager[2];;
	private boolean clientActive[] = {false, false};
	private Game game;
	private boolean playGame = true;
	
	public Manager() {
		
		server = new Server();
		client[0] = new ClientManager(this);
		clientActive[0] = true;
		client[1] = new ClientManager(this);
		clientActive[1] = true;
		
		game = new Game(this);
		
		while(playGame) {
			tick();
		}
		
		
	}
	
	private void tick() {
		
		game.tick();
		
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
