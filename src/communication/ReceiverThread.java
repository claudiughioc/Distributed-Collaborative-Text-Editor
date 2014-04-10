package communication;

import java.io.DataInputStream;
import java.net.Socket;

import engine.Main;
import engine.Utils;

public class ReceiverThread extends Thread {
	private Socket s;
	private NetworkManager nm;
	
	public ReceiverThread(Socket s, NetworkManager nm) {
		this.s = s;
		this.nm = nm;
	}
	
	public void run() {
		System.out.println("[TCPServerThread] " + this.hashCode() + " has accepted someone");
		try {
			DataInputStream din = new DataInputStream(this.s.getInputStream());
			
			
			/* Wait for messages from the other peer */
			while(true) {
				int size = din.readInt();
				Main.receivedBytes += size;
				byte[] bytes = new byte[size];
				din.readFully(bytes);
				TextMessage request = (TextMessage)Utils.deserialize(bytes);
				
				System.out.println("[TCPServerThread] " + this.hashCode() + " got request " + request);
				nm.onReceive(request);
			}
		} catch (Exception e) {
			System.err.println("TCPServerThread-" + this.hashCode() + " exception: " + e);
			e.printStackTrace();
		}
	}
}
