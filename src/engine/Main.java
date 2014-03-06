package engine;

import comm.NetworkMessenger;

import gui.GUIManager;

public class Main {

	public static void main (String [] args) {
		String IP = args[0];
		String port = args[1];
		
		/* Start the network communicator */
		NetworkMessenger ncomm = new NetworkMessenger(IP, port);
		
		/* Start and show the gui */
		GUIManager gui = new GUIManager(ncomm);
		gui.showGUI();
	}
}
