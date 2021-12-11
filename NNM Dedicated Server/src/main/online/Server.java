package main.online;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
	
	private ServerSocket server;
	private int port;
	
	public Server() {
		
		System.out.println("Starting Dedicated Server...");
		port = 42069;
		try {
			
			server = new ServerSocket(port);
			System.out.println("Complete.");
			
		} catch (IOException e) {
			System.out.println("An Error Occured while starting the Server.");
		}
		
	}
	
	public Socket allowUser() {
		
		Socket s = null;
		System.out.println("Waiting for User...");
		
		try {
			s = server.accept();
			System.out.println("User Connected.");
		} catch (IOException e) {
			System.out.println("An Error Occured while waiting for User.");
		}
		
		return s;
		
	}
	
	public void stopServer() {
		
		if(server.isClosed()) {
			
			try {
				server.close();
			} catch (IOException e) {
				System.out.println("An Error Occured while closing Server.");
			}
			
		}
		
	}
	
	public ServerSocket getServer() {
		return server;
	}

}
