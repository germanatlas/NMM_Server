package main.online;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import main.Manager;
import main.state.Location;

public class ClientManager {
	
	private Server server;
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private String username;
	@SuppressWarnings("unused")
	private Manager man;
	private Location LOCATION;
	
	public ClientManager(Manager man) {
		
		this.man = man;
		this.server = man.getServer();
		this.socket = server.allowUser();
		//man.print("Starting Streams...");
		try {
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			LOCATION = Location.LOGIN;
			//man.print("Finished creating Socket");
		} catch (IOException e) {
			LOCATION = Location.OFFLINE;
			man.print("An Error occured while creating streams...");
		}
		
		
	}
	
	public Object receiveData() {
		
		if(LOCATION != Location.OFFLINE) {
			
			try {
				byte[] buffer = new byte[2048];
				int count = in.read(buffer);
				if(count < 0) {
					return null;
				}
				//print(count + " Bytes Received.");
				
				byte[] pack = new byte[count];
				pack = shorten(buffer, count);
				Object obj = toObject(pack);
				
				return obj;
			} catch (IOException | ClassNotFoundException e) {
				//print("An Error occured while receiving data from client. Closing Client.");
				close();
				return null;
			}
			
		}
		
		return null;
		
	}
	
	public boolean sendData(Object o) {
		
		if(LOCATION != Location.OFFLINE) {
			
			try {
				byte[] bytes = toBytes(o);
				out.write(bytes);
				out.flush();
				//man.print("Sending " + o.getClass() + " to " + username);
				
				return true;
			} catch (IOException e) {
				//print("An Error occured while sending data to client. Closing Client.");
				close();
				return false;
			}
			
		}
		
		return false;
		
	}
	
	
	private byte[] toBytes(Object obj) throws IOException {
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		
		oos.writeObject(obj);
		oos.flush();
		
		byte[] bytes = bos.toByteArray();
		
		bos.close();
		
		return bytes;
		
	}

	private Object toObject(byte[] bytes) throws IOException, ClassNotFoundException {
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bis);
		Object obj = ois.readObject();
		ois.close();
		return obj;
		
	}

	private byte[] shorten(byte[] srcmat, int limit) {
		
		byte[] b = new byte[limit];
	
		for(int i = 0; i < limit; i++) {
		
			b[i] = srcmat[i];
		
		}
	
		return b;
	}
	
	public void close() {
		
		try {
			socket.close();
			LOCATION = Location.OFFLINE;
		} catch (IOException e) {
			//print("An Error occured while closing the Socket to Client.");
		}
		
	}

	public BufferedInputStream getInputStream() {
		return in;
	}

	public BufferedOutputStream getOutputStream() {
		return out;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	public void setUsername(String username) {
		this.username = username;	
	}
	
	public String getUsername() {
		return username;
	}
	
	public Location getLocation() {
		return LOCATION;
	}
	
	public void setLocation(Location LOCATION) {
		this.LOCATION = LOCATION;
	}
	

}
