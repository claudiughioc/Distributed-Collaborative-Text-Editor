package jupiter;

import dopt.DOPTTransformation;
import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;

public class JupiterNetworkManager extends NetworkManager {
	public GUIManager gui;
	public int peerIndex;
	private ArrayList<Sender> senders;
	private ArrayList<JupiterTextMessage> outgoing;
	private Sender peerSender;
	private ReceiveManager receiver;
	private int myMessages = 0, otherMessages = 0;

	public JupiterNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;

		this.outgoing = new ArrayList<JupiterTextMessage>();
	}

	public void initiateCommThreads() {
		System.out.println("Jupiter Network Manager creates the threads");

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
		int i;
		JupiterTextMessage rtm = (JupiterTextMessage)tm, initial;

		/* The root peer broadcasts the message */
		if (peerIndex == Main.rootPeer)
		for (i = 0; i < Main.peerCount - 1; i++) {
			if (i == rtm.sender - 1)
				continue;
			senders.get(i).send(rtm);
		}
		
		/* Clear the unnecessary messages */
		System.out.println("MyMessages is " + myMessages + " other is " + otherMessages);
		System.out.println("Initial outgoing size " + outgoing.size());
		Iterator<JupiterTextMessage> it = outgoing.iterator();
		synchronized (outgoing) {
			while (it.hasNext()) {
				JupiterTextMessage jtm = it.next();
				if (jtm == null) {
					System.out.println("Obiect null in lista");
					it.remove();
					continue;
				}
				if (jtm.myMessages < rtm.otherMessages)
					it.remove();
			}
		}

		/* Assert 
		if (rtm.myMessages != this.otherMessages) {
			System.out.println("Asertul pica, mesajul are my = " + rtm.myMessages + " eu am " + otherMessages);
			System.exit(1);
		}*/

		/* Transform the message */
		System.out.println("After outgoing size " + outgoing.size());
		for (i = 0; i < outgoing.size(); i++) {
			JupiterTextMessage elem = outgoing.get(i);
			if (elem == null)
				continue;

			initial = JupiterTextMessage.duplicate(rtm);
			rtm = (JupiterTextMessage)DOPTTransformation.transform(rtm, elem);
			elem = (JupiterTextMessage)DOPTTransformation.transform(elem, initial);
			synchronized ((outgoing)) {
				if (initial != null)
					outgoing.set(i, elem);
				else outgoing.remove(i);	
			}
			if (rtm == null || elem == null)
				break;
		}

		deliverMessage(rtm);
		if (rtm != null)
			otherMessages++;
		System.out.println("[End]MyMessages is " + myMessages + " other is " + otherMessages);
		System.out.println();
	}

	/* Deliver message to GUI */
	public void deliverMessage(TextMessage request) {
		System.out.println("Delivering " + request);
		if (request == null)
			return;

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
		System.out.println("Jupiter Network manager " + peerIndex + " starts the threads ");

		/* Start the receiver and sender sockets */
		receiver.start();

		if (peerIndex != Main.rootPeer) {
			peerSender.start();
			return;
		}
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}
	@Override
	public synchronized void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);

		/* Create the message and send it */
		JupiterTextMessage tm = new JupiterTextMessage(pos, c, TextMessage.INSERT, peerIndex,
				null, peerIndex, myMessages, otherMessages);

		/* Broadcast message */
		if (peerIndex != Main.rootPeer) {
			peerSender.send(tm);
		} else {
			for (int i = 0; i < Main.peerCount - 1; i++)
				senders.get(i).send(tm);
		}

		/* Add the message to the list of outgoing */
		synchronized (outgoing) {
			outgoing.add(tm);	
		}
		myMessages++;
	}

	@Override
	public synchronized void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);

		JupiterTextMessage tm = new JupiterTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
				null, peerIndex, myMessages, otherMessages);

		/* Broadcast message */
		if (peerIndex != Main.rootPeer) {
			peerSender.send(tm);
		} else {
			for (int i = 0; i < Main.peerCount - 1; i++)
				senders.get(i).send(tm);
		}

		/* Add the message to the list of outgoing */
		synchronized (outgoing) {
			outgoing.add(tm);	
		}
		myMessages++;
	}
}
