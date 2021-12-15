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

public class ClientManager {
	
	private static final int TIMEOUT = 1000;
	private Server server;
	private Socket socket;
	private BufferedInputStream in;
	private BufferedOutputStream out;
	
	public ClientManager(Manager man) {
		
		this.server = man.getServer();
		this.socket = server.allowUser();
		System.out.println("Starting Streams...");
		try {
			in = new BufferedInputStream(socket.getInputStream());
			out = new BufferedOutputStream(socket.getOutputStream());
			System.out.println("Finished creating Socket");
		} catch (IOException e) {
			System.out.println("An Error occured while creating streams...");
		}
		
		
	}
	
	public Object receiveData() {
		
		try {
			byte[] buffer = new byte[2048];
			int count = in.read(buffer);
			if(count < 0) {
				return null;
			}
			System.out.println(count + " Bytes Received.");
			
			byte[] pack = new byte[count];
			pack = shorten(buffer, count);
			Object obj = toObject(pack);
			//TODO
			
			return obj;
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("An Error occured while receiving data from client.");
			return null;
		}
		
	}
	
	public boolean sendData(Object data) {
		//TODO
		
		try {
			byte[] bytes = toBytes(data);
			out.write(bytes);
			out.flush();
			System.out.println(bytes.length + " Bytes Sent.");
			
			return true;
		} catch (IOException e) {
			System.out.println("An Error occured while sending data to client." + e);
			return false;
		}
		
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
		} catch (IOException e) {
			System.out.println("An Error occured while closing the Socket to Client.");
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
	
	public boolean getIfActive() {
		try {
			return !socket.isClosed() || socket.getInetAddress().isReachable(TIMEOUT);
		} catch (IOException e) {
			return false;
		}
	}

}
