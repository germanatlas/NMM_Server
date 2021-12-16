package main.online;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;

public class Server {
	
	private ServerSocket server;
	private int port;
	
	public Server() {
		
		print("Starting Dedicated Server...");
		port = 42069;
		try {
			
			server = new ServerSocket(port);
			print("Complete.");
			
		} catch (IOException e) {
			print("An Error Occured while starting the Server.");
		}
		
	}
	
	public Socket allowUser() {
		
		Socket s = null;
		print("Waiting for User...");
		
		try {
			s = server.accept();
			print("User Connected.");
		} catch (IOException e) {
			print("An Error Occured while waiting for User.");
		}
		
		return s;
		
	}
	
	public void stopServer() {
		
		if(server.isClosed()) {
			
			try {
				server.close();
			} catch (IOException e) {
				print("An Error Occured while closing Server.");
			}
			
		}
		
	}
	
	public ServerSocket getServer() {
		return server;
	}
	
	public void print(String msg) {
		
		System.out.println("[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond() + "]\t" + msg);
		
	}

}
