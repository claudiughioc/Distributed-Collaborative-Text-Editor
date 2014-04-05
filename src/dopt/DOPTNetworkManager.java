package dopt;

import java.net.ServerSocket;
import java.util.ArrayList;

import gui.GUIManager;
import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;
import communication.TimeVector;
import engine.Main;

public class DOPTNetworkManager extends NetworkManager{
	public int peerIndex;
	public GUIManager gui;
	public ArrayList<Sender> senders;
	public ReceiveManager receiver;
	public TimeVector timeVector;
	public DOPTHandler handler;
	public ArrayList<Integer> stateVector;
	
	public DOPTNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;
		handler = new DOPTHandler(this);

		/* Initialize the state cevtor */
		stateVector = new ArrayList<Integer>();
		for (int i = 0; i < Main.peerCount; i++)
			stateVector.add(0);
	}

	@Override
	public synchronized void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);

		/* Create the message and send it */
		DOPTTextMessage tm = new DOPTTextMessage(pos, c, TextMessage.INSERT, peerIndex,
				timeVector, peerIndex, getStateVector());

		/* Add message to queue */
		handler.addMessage(tm);

		/* Broadcast message */
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}

	@Override
	public synchronized void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);

		DOPTTextMessage tm = new DOPTTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
				timeVector, peerIndex, getStateVector());

		/* Add message to queue */
		handler.addMessage(tm);
		
		/* Broadcast message */
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}

	/* Create the sender and receiver threads */
	public void initiateCommThreads() {
		System.out.println("Network Manager creates the threads");

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

	public void run() {
		System.out.println("DOPT Network manager " + peerIndex + " starts the threads ");

		/* Start the receiver and sender sockets */
		receiver.start();
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}

	@Override
	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
		initiateCommThreads();
	}

	@Override
	public void onReceive(TextMessage tm) {
		//handler.messageReceived(tm);
		deliverMessage(tm);
	}

	@Override
	public void deliverMessage(TextMessage request) {
		/* Update the state vector */

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

	@Override
	public GUIManager getGUI() {
		return null;
	}

	public synchronized ArrayList<Integer> getStateVector() {
		return this.stateVector;
	}
}
