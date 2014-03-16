package centralizedModule;

import java.net.ServerSocket;
import java.util.ArrayList;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;

import engine.Main;

import gui.GUIManager;

public class CentralizedNetworkManager extends NetworkManager {
	public GUIManager gui;
	public int peerIndex;
	private ArrayList<Sender> senders;
	private Sender peerSender;
	private ReceiveManager receiver;

	public CentralizedNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;
	}

	@Override
	public void insert(int pos, char c) {
		System.out.println("I am going to send an insertion " + c + " at " + pos);

		/* Create the message and send it */
		TextMessage tm = new TextMessage(pos, c, TextMessage.INSERT, peerIndex, null);
		if (peerIndex != Main.rootPeer) {
			peerSender.send(tm);
			return;
		}
		
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}

	@Override
	public void delete(int pos) {
		System.out.println("I am going to send a deletion at " + pos);

		TextMessage tm = new TextMessage(pos, 'q', TextMessage.DELETE, peerIndex, null);
		if (peerIndex != Main.rootPeer) {
			peerSender.send(tm);
			return;
		}
		
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	}

	public void initiateCommThreads() {
		System.out.println("Network Manager creates the threads");

		try {
			/* Create the listener socket */
			ServerSocket listener = new ServerSocket(Main.ports.get(peerIndex));
			receiver = new ReceiveManager(listener, this);

			/* The normal peers won't create other senders */
			if (peerIndex != Main.rootPeer)
				peerSender = new Sender(Main.IPAdresses.get(Main.rootPeer),
						Main.ports.get(Main.rootPeer));
			else {
				senders = new ArrayList<Sender>(Main.peerCount - 1);
				
				/* Create the sender threads */
				for (int i = 0; i < Main.peerCount; i++) {
					if (i == peerIndex)
						continue;

					senders.add(new Sender(Main.IPAdresses.get(i), Main.ports.get(i)));
				}
			}
		} catch (Exception e) {
			System.out.println("Unable to start sockets");
			e.printStackTrace();
		}
	}
	
	public synchronized void onReceive(TextMessage tm) {
		deliverMessage(tm);
		
		if (peerIndex != Main.rootPeer)
			return;
		
		/* The root peer broadcasts the message */
		for (int i = 0; i < Main.peerCount - 1; i++) {
			if (i == tm.sender - 1)
				continue;
			senders.get(i).send(tm);
		}
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


	public void connectToGUI(GUIManager gui) {
		this.gui = gui;
		initiateCommThreads();
	}


	public GUIManager getGUI() {
		return gui;
	}

	
	public void run() {
		System.out.println("Total Order Network manager " + peerIndex + " starts the threads ");

		/* Start the receiver and sender sockets */
		receiver.start();
		
		if (peerIndex != Main.rootPeer) {
			peerSender.start();
			return;
		}
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}
}
