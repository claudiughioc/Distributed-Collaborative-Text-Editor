package engine;

import gui.GUIManager;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import threePhaseModule.ThreePhaseNetworkManager;
import ABCASTModule.ABCASTNetworkManager;
import CBCASTModule.CBCASTNetworkManager;
import centralizedModule.CentralizedNetworkManager;

import communication.NetworkManager;
import dopt.DOPTNetworkManager;

public class Main {
	private static final String CONFIG_FILE 	= "peers";
	public static final String CBCAST_ALGO 		= "CBCAST";
	public static final String ABCAST_ALGO 		= "ABCAST";
	public static final String TOTAL_ORDER_ALGO = "TOTAL_ORDER";
	public static final String THREE_PHASE_ALGO = "THREE_PHASE";
	public static final String DOPT_ALGO		= "DOPT";

	public static ArrayList<String> IPAdresses;
	public static ArrayList<Integer> ports;
	public static int peerCount, rootPeer, connectedPeers;
	public static String algorithm;


	/* Read peer configuration file */
	public static void readConfiguration() {
		Main.connectedPeers = 0;
		try {
			BufferedReader buff = new BufferedReader(new FileReader(CONFIG_FILE));

			String line = buff.readLine();
			Main.peerCount = Integer.parseInt(line);
			Main.IPAdresses = new ArrayList<String>(Main.peerCount);
			Main.ports = new ArrayList<Integer>(Main.peerCount);

			/* Read peers' IP addresses and ports */
			for (int i = 0; i < Main.peerCount; i++) {
				Main.IPAdresses.add(buff.readLine());
				Main.ports.add(Integer.parseInt(buff.readLine()));
				System.out.println("Peer " + i + " ip " + IPAdresses.get(i) + " port " + ports.get(i));
			}

			/* Read the algorithm to use */
			Main.algorithm = buff.readLine();
			System.out.println("Using the " + Main.algorithm + " algorithm");
			if (Main.algorithm.equals(TOTAL_ORDER_ALGO)) {
				Main.rootPeer = Integer.parseInt(buff.readLine());
				System.out.println("Root peer " + Main.rootPeer);
			}

			buff.close();
		} catch (Exception e) {
			System.out.println("Unable to read configuration file");
			e.printStackTrace();
		}
	}

	public static void main (String [] args) {
		int peerIndex = Integer.parseInt(args[0]);
		NetworkManager nManag = null;

		/* Read peer configuration file */
		readConfiguration();

		/* Start the network communicator */
		if (Main.algorithm.equals(CBCAST_ALGO))
			nManag = new CBCASTNetworkManager(peerIndex);
		if (Main.algorithm.equals(TOTAL_ORDER_ALGO))
			nManag = new CentralizedNetworkManager(peerIndex);
		if (Main.algorithm.equals(THREE_PHASE_ALGO))
			nManag = new ThreePhaseNetworkManager(peerIndex);
		if (Main.algorithm.equals(ABCAST_ALGO))
			nManag = new ABCASTNetworkManager(peerIndex);
		if (Main.algorithm.equals(DOPT_ALGO))
			nManag = new DOPTNetworkManager(peerIndex);

		/* Start and show the gui */
		GUIManager gui = new GUIManager(nManag, peerIndex);
		nManag.connectToGUI(gui);
		nManag.start();

		gui.showGUI();


		/* Check if the test driver needs to run */
		if (args.length > 1 && args[1].equals("test")) {

			/* Wait for all the servers to start */
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {}

			/* Run the test */
			TestDriver.test(peerIndex, nManag);
		}
	}
}
