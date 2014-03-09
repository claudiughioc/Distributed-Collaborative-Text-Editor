package comm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import engine.Utils;

public class Sender extends Thread {
	public static final int CONN_TIMEOUT = 3000;
	private String IP;
	private int port;
	private boolean connected;
	private Socket socket;
	
	public Sender(String IP, int port) {
		this.IP = IP;
		this.port = port;
		connected = false;
	}
	
	public void tryToConnect() {
		
		while (true) {
			try {
				Thread.sleep(CONN_TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (connected)
				continue;

			/* Try to connect to one other peer */
			System.out.println("Tryng to connect to " + IP + " port " + port);
			try {
				socket = new Socket(IP, port);
				connected = true;
				System.out.println("Connected to " + IP + " port " + port);
			} catch (Exception e1) {
				connected = false;
			}
		}
	}

	public void run() {
		tryToConnect();
	}
	
	
	/* Sends an event message to the peer the sender is connected to */
	public void send(TextMessage tm) {
		if (!connected)
			return;
		
		System.out.println("Sending " + tm + " to " + port);
		try {
			byte[] serialized = Utils.serialize(tm);
			int size = serialized.length;
			DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
			dos.writeInt(size);
			dos.write(serialized);
			System.out.println("A plecat mesajul: " + tm + " catre " + port);
		} catch (IOException e) {
			connected = false;
		}
	}

}
