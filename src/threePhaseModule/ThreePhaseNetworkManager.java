package threePhaseModule;

import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.PriorityQueue;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;

public class ThreePhaseNetworkManager extends NetworkManager {
	public int peerIndex;
	private ArrayList<Sender> senders;
	private ReceiveManager receiver;
	public GUIManager gui;
	public Timestamp clock;
	
	public PriorityQueue<ThreePhaseTextMessage> tempQueue, deliverableQueue;
	
	public ThreePhaseNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;
		
		/* Initialize the clock */
		java.util.Date date = new java.util.Date();
		this.clock = new Timestamp(date.getTime());
		
		/* Initialize the timestamp sorted queues */
		ThreePhaseComparator tpc = new ThreePhaseComparator();
		tempQueue = new PriorityQueue<ThreePhaseTextMessage>(10000, tpc);
		deliverableQueue = new PriorityQueue<ThreePhaseTextMessage>(10000, tpc);
	}

	@Override
	public void insert(int pos, char c) {
		
	}

	@Override
	public void delete(int pos) {
		
	}

	
	/* Create the sender and receiver threads */
	public void initiateCommThreads() {
		System.out.println("3 Phase Network Manager creates the threads");

		/* Create listener and sender threads */
		senders = new ArrayList<Sender>(Main.peerCount - 1);
		try {
			/* Create the listener socket */
			ServerSocket listener = new ServerSocket(Main.ports.get(peerIndex));
			receiver = new ReceiveManager(listener, this);


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
	
	
	@Override
	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
		initiateCommThreads();
	}
	
	public void run() {
		System.out.println("3 Phase Network manager " + peerIndex + " starts the threads ");


		/* Start the receiver and sender sockets */
		receiver.start();
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}

	@Override
	public void onReceive(TextMessage tm) {
	}

	@Override
	public GUIManager getGUI() {
		return gui;
	}

	/* Deliver message to GUI */
	public void deliverMessage(TextMessage request) {

		/* Perform action */
		switch (request.type) {
		case TextMessage.DELETE:
			gui.deleteChar(request.pos);
			break;

		case TextMessage.INSERT:
			gui.insertChar(request.pos, request.c);
			break;
		}
	}
}
