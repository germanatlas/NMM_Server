package main.online;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalTime;

import main.Manager;

public class ClientManager {
	
	private Server server;
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	private boolean active;
	
	public ClientManager(Manager man) {
		
		active = false;
		this.server = man.getServer();
		this.socket = server.allowUser();
		print("Starting Streams...");
		try {
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			active = true;
			print("Finished creating Socket");
		} catch (IOException e) {
			print("An Error occured while creating streams...");
		}
		
		
	}
	
	public DataPackage receiveData() {
		
		if(getIfActive()) {
			
			try {
				byte[] buffer = new byte[2048];
				int count = in.read(buffer);
				if(count < 0) {
					return null;
				}
				print(count + " Bytes Received.");
				
				byte[] pack = new byte[count];
				pack = shorten(buffer, count);
				Object obj = toObject(pack);
				
				return (DataPackage) obj;
			} catch (IOException | ClassNotFoundException e) {
				print("An Error occured while receiving data from client. Closing Client.");
				active = false;
				close();
				return null;
			}
			
		}
		
		return null;
		
	}
	
	public boolean sendData(DataPackage dp) {
		
		if(!socket.isClosed() && active) {
			
			try {
				byte[] bytes = toBytes(dp);
				out.write(bytes);
				out.flush();
				print(bytes.length + " Bytes Sent. " + dp.getStatus() + " " + dp.getFromX() + " " + dp.getFromY() + " " + dp.getToX() + " " + dp.getToY());
				
				return true;
			} catch (IOException e) {
				print("An Error occured while sending data to client. Closing Client.");
				active = false;
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
			active = false;
		} catch (IOException e) {
			print("An Error occured while closing the Socket to Client.");
		}
		
	}
	
	public void print(String msg) {
		
		System.out.println("[" + LocalTime.now().getHour() + ":" + LocalTime.now().getMinute() + ":" + LocalTime.now().getSecond() + "]\t" + msg);
		
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
	
	public boolean getIfActive() {
		return !socket.isClosed() && active;
	}
	
	

}
