package comm;

import java.net.Socket;

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
		
		while (!connected) {
			System.out.println("Tryng to connect to " + IP + " port " + port);
			try {
				Thread.sleep(CONN_TIMEOUT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			/* Try to connect to one other peer */
			try {
				socket = new Socket(IP, port);
				connected = true;
			} catch (Exception e1) {
				connected = false;
			}
		}
	}

	public void run() {
		tryToConnect();
		System.out.println("Connected to " + IP + " port " + port);
	} 
	
	public void send(TextMessage tm) {
		if (!connected)
			return;
	}

}
