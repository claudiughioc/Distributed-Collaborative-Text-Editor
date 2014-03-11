package engine;

import gui.GUIManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import comm.NetworkManager;

public class Main {
	private static final String CONFIG_FILE = "peers";
	public static ArrayList<String> IPAdresses;
	public static ArrayList<Integer> ports;
	public static int peerCount;
	
	
	/* Read peer configuration file */
	public static void readConfiguration() {
		try {
			BufferedReader buff = new BufferedReader(new FileReader(CONFIG_FILE));
			
			String line = buff.readLine();
			Main.peerCount = Integer.parseInt(line);
			Main.IPAdresses = new ArrayList<String>(Main.peerCount);
			Main.ports = new ArrayList<Integer>(Main.peerCount);
			
			for (int i = 0; i < Main.peerCount; i++) {
				Main.IPAdresses.add(buff.readLine());
				Main.ports.add(Integer.parseInt(buff.readLine()));
				System.out.println("Peer " + i + " ip " + IPAdresses.get(i) + " port " + ports.get(i));
			}
			buff.close();
		} catch (Exception e) {
			System.out.println("Unable to read configuration file");
			e.printStackTrace();
		}
	}

	public static void main (String [] args) {
		int peerIndex = Integer.parseInt(args[0]);

		/* Read peer configuration file */
		readConfiguration();
		
		/* Start the network communicator */
		NetworkManager ncomm = new NetworkManager(peerIndex);
		
		/* Start and show the gui */
		GUIManager gui = new GUIManager(ncomm);
		ncomm.connectToGUI(gui);
		ncomm.start();
		
		gui.showGUI();
		
		
		/* Check if the test driver needs to run */
		if (args.length > 1 && args[1].equals("test")) {
			
			/* Wait for all the servers to start */
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}
			
			/* Run the test */
			TestDriver.test(peerIndex, ncomm);
		}
	}
}
