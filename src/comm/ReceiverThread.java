package comm;

import engine.Utils;
import gui.GUIManager;

import java.io.DataInputStream;
import java.net.Socket;

public class ReceiverThread extends Thread {
	private Socket s;
	private GUIManager gui;
	
	public ReceiverThread(Socket s, GUIManager gui) {
		this.s = s;
		this.gui = gui;
	}
	
	public void run() {
		System.out.println("[TCPServerThread] " + this.hashCode() + " has accepted someone");
		try {
			DataInputStream din = new DataInputStream(this.s.getInputStream());
			
			/* Wait for messages from the other peer */
			while(true) {
				int size = din.readInt();
				System.out.println("[TCPServerThread] TCP size received " + size);
				byte[] bytes = new byte[size];
				din.readFully(bytes);
				TextMessage request = (TextMessage)Utils.deserialize(bytes);
				
				System.out.println("[TCPServerThread] " + this.hashCode() + " got request " + request);
				
				
				/* Send request to GUI */
				switch (request.type) {
				case TextMessage.DELETE:
					gui.deleteChar(request.pos);
					break;
					
				case TextMessage.INSERT:
					gui.insertChar(request.pos, request.c);
					break;
				}
			}
		} catch (Exception e) {
			System.err.println("TCPServerThread-" + this.hashCode() + " exception: " + e);
			e.printStackTrace();
		}
	}
}
