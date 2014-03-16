package communication;

import java.net.ServerSocket;
import java.net.Socket;

public class ReceiveManager extends Thread {
	private ServerSocket serverSocket;
	private NetworkManager nm;

	public ReceiveManager(ServerSocket serverSocket, NetworkManager nm) {
		this.serverSocket = serverSocket;
		this.nm = nm;
	}
	
	public void run() {
		System.out.println("Listenting for messages ...");
		
		/* Listen for connections */
		try {
            while (true) {
                Socket socket = serverSocket.accept();
                ReceiverThread receiver = new ReceiverThread(socket, nm);
                receiver.start();
            }
        }
		catch (Exception e) {
			System.out.println("Error on accepting connection");
		}
	}
}
