package engine;

import gui.GUIManager;

public class Main {

	public static void main (String [] args) {
		GUIManager gui = new GUIManager();
		String IP = args[0];
		String port = args[1];

		System.out.println("Starting the application, ip " + IP + " port " + port);
		gui.showGUI();
		System.out.println("Gui has started\n");
		while (true){}
	}
}
