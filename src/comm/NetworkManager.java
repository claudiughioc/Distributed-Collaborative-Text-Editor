package comm;

import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;

public class NetworkManager extends Thread implements Messenger {
	private GUIManager gui;
	private int peerIndex;
	
	private ArrayList<Sender> senders;
	private ReceiveManager receiver;
	
	public NetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;
	}

	
	/* Create the sender and receiver threads */
	public void initiateCommThreads() {
		System.out.println("Network Manager creates the threads");
		
		/* Create listener and sender threads */
		senders = new ArrayList<Sender>(Main.peerCount - 1);
		try {
			/* Create the listener socket */
			ServerSocket listener = new ServerSocket(Main.ports.get(peerIndex));
			receiver = new ReceiveManager(listener, gui);
			
			
			/* Create the sender threads */
			for (int i = 0; i < Main.peerCount; i++) {
				if (i == peerIndex)
					continue;
				
				senders.add(new Sender(Main.IPAdresses.get(i), Main.ports.get(i)));
			}
		} catch (Exception e) {
			System.out.println("Unable to start sockets");
			e.printStackTrace();
		}
	}

	
	public void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);

		TextMessage tm = new TextMessage(pos, c, TextMessage.INSERT);
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}

	
	public void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);
		
		TextMessage tm = new TextMessage(pos, 'q', TextMessage.DELETE);
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}
	
	
	public void run() {
		System.out.println("Network manager " + peerIndex + " starts the threads ");
		
		
		/* Start the receiver and sender sockets */
		receiver.start();
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}

	
	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
		initiateCommThreads();
	}
}
