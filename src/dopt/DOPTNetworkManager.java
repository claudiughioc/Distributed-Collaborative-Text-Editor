package dopt;

import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;
import communication.TimeVector;

public class DOPTNetworkManager extends NetworkManager{
	public int peerIndex;
	public GUIManager gui;
	public ArrayList<Sender> senders;
	public ReceiveManager receiver;
	public TimeVector timeVector;
	public DOPTHandler handler;
	public ArrayList<Integer> stateVector;
	public Lock stateVectorLock = new ReentrantLock();
	
	public DOPTNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;
		handler = new DOPTHandler(this);

		/* Initialize the state vector */
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

		/* Broadcast message */
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);

		updateStateVector(peerIndex);
	}

	@Override
	public synchronized void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);
		
		DOPTTextMessage tm = new DOPTTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
				timeVector, peerIndex, getStateVector());

		/* Broadcast message */
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);
	
		updateStateVector(peerIndex);
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
	public synchronized void onReceive(TextMessage tm) {
		handler.messageReceived(tm);
	}

	@Override
	public void deliverMessage(TextMessage request) {
		System.out.println("Delivering at " + peerIndex + " msg " + request);
		/* Removing from message queue */
		handler.removeMessage(request);

		/* Perform action */
		switch (request.type) {
		case TextMessage.DELETE:
			gui.deleteChar(request.pos);
			break;

		case TextMessage.INSERT:
			gui.insertChar(request.pos, request.c);
			break;
		}

		/* Update the state vector */
		this.handler.pushLog(request);
		updateStateVector(request.sender);
	}

	@Override
	public GUIManager getGUI() {
		return gui;
	}

	public synchronized ArrayList<Integer> getStateVector() {
		return this.stateVector;
	}

	public void updateStateVector(int sender) {
		stateVectorLock.lock();
		stateVector.set(sender, stateVector.get(sender) + 1);
		stateVectorLock.unlock();
	}
}
