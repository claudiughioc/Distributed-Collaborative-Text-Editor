package comm;

import gui.GUIManager;

public class NetworkMessenger extends Thread implements Messenger {
	private GUIManager gui;
	private int peerIndex;
	
	public NetworkMessenger(int peerIndex) {
		this.peerIndex = peerIndex;
	}

	@Override
	public void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);
	}

	@Override
	public void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);
	}
	
	public void run() {
		System.out.println("The peer network " + peerIndex + " is running");
	}

	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
	}
}
