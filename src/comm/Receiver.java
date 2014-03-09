package comm;

import gui.GUIManager;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Receiver extends Thread {
	private ServerSocket serverSocket;
	private GUIManager gui;

	public Receiver(ServerSocket serverSocket, GUIManager gui) {
		this.serverSocket = serverSocket;
		this.gui = gui;
	}
	
	public void run() {
		System.out.println("Listenting for messages ...");
		
		/* Listen for messages */
		try {
            while (true) {
                Socket socket = serverSocket.accept();
                try {
                    PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
                } finally {
                    socket.close();
                }
            }
        }
		catch (Exception e) {
			System.out.println("Error on receiving message");
		}
		
		
		/* Close the socket at the end */
        finally {
        	System.out.println("Closing listener socket");
        	try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}

}
