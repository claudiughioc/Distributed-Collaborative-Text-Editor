package comm;

import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;

public class CBCASTNetworkManager extends NetworkManager {
	public GUIManager gui;
	private int peerIndex;

	private ArrayList<Sender> senders;
	private ReceiveManager receiver;
	public TimeVector timeVector;
	public CBCASTHandler commProto;

	public CBCASTNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;

		/* Create the time vector */
		timeVector = new TimeVector(Main.peerCount);
		
		/* Create the communication protocol */
		this.commProto = new CBCASTHandler(this);
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


	public void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);

		/* Update the time vector */
		timeVector.VT.set(peerIndex, timeVector.VT.get(peerIndex) + 1);

		/* Create the message and send it */
		TextMessage tm = new TextMessage(pos, c, TextMessage.INSERT, peerIndex, timeVector);
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}


	public void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);

		TextMessage tm = new TextMessage(pos, 'q', TextMessage.DELETE, peerIndex, timeVector);
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}
	
	public GUIManager getGUI() {
		return gui;
	}


	public void run() {
		System.out.println("CBCAST Network manager " + peerIndex + " starts the threads ");


		/* Start the receiver and sender sockets */
		receiver.start();
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}

	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
		initiateCommThreads();
	}
	

	public void updateVTDeliver(TextMessage message) {
		TimeVector msgVT = message.timeVector;

		synchronized (CBCASTNetworkManager.class) {
			for (int i = 0; i < Main.peerCount; i++)
				if (timeVector.VT.get(i) < msgVT.VT.get(i))
					timeVector.VT.set(i, msgVT.VT.get(i));
		}
		
		/* Notify the communication protocol that another message has arrived */
		commProto.notifyIncommingMessage();
	}

	public void onReceive(TextMessage tm) {
		commProto.messageReceived(tm);
	}

	/* Deliver message to GUI */
	public void deliverMessage(TextMessage request) {
		/* Update receiver's vector time */
		updateVTDeliver(request);

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
