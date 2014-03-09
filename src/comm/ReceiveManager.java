package comm;

import gui.GUIManager;

import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveManager extends Thread {
	private ServerSocket serverSocket;
	private GUIManager gui;

	public ReceiveManager(ServerSocket serverSocket, GUIManager gui) {
		this.serverSocket = serverSocket;
		this.gui = gui;
	}
	
	public void run() {
		System.out.println("Listenting for messages ...");
		
		/* Listen for connections */
		try {
            while (true) {
                Socket socket = serverSocket.accept();
                ReceiverThread receiver = new ReceiverThread(socket, gui);
                receiver.start();
            }
        }
		catch (Exception e) {
			System.out.println("Error on accepting connection");
		}
	}
}
