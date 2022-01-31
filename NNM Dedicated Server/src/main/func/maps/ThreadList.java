package main.func.maps;

import java.util.concurrent.ConcurrentHashMap;

public class ThreadList {
	
	private ConcurrentHashMap<String, Thread> threads;
	
	public ThreadList() {
		
		this.threads = new ConcurrentHashMap<>();
		
	}
	
	public Thread getThread(String username) {
		return threads.get(username);
	}
	
	public Thread[] getAllThreads() {
		return threads.values().toArray(new Thread[0]);
	}
	
	public boolean remove(String username) {
		
		return (threads.remove(username) != null);
		
	}
	
	public boolean add(String username, Thread thread) {
		
		if(this.threads.get(username) == null) {
			
			this.threads.put(username, thread);
			return true;
			
		}
		
		return false;
		
	}
	
	public int length() {
		return threads.size();
	}

}
