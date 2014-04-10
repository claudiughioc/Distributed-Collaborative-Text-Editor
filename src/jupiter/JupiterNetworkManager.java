package jupiter;

import dopt.DOPTTransformation;
import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;

public class JupiterNetworkManager extends NetworkManager {
	public GUIManager gui;
	public int peerIndex;
	private ArrayList<Sender> senders;
	public HashMap<Integer, ArrayList<JupiterTextMessage>> serverOutgoing;
	private ArrayList<JupiterTextMessage> outgoing;
	private Sender peerSender;
	private ReceiveManager receiver;
	private int myMessages = 0, otherMessages = 0;
	public ArrayList<Integer> servMessages, serverOther;
	public static Object lock = new Object();

	public JupiterNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;

		this.outgoing = new ArrayList<JupiterTextMessage>();

		/* Initialize server specific variables */
		if (peerIndex == Main.rootPeer) {
			serverOutgoing = new HashMap<Integer, ArrayList<JupiterTextMessage>>();
			serverOther = new ArrayList<Integer>();
			servMessages = new ArrayList<Integer>();

			for (int i = 0; i < Main.peerCount - 1; i++) {
				serverOutgoing.put(i, new ArrayList<JupiterTextMessage>());
				serverOther.add(0);
				servMessages.add(0);
			}
		}
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

	public synchronized void printParams() {
		if (peerIndex == Main.rootPeer) {
			System.out.print("Server messages: ");
			for (int i = 0; i < Main.peerCount - 1; i++) {
				System.out.print(" " + servMessages.get(i));
			}
			System.out.println();
			System.out.print("Server others:");
			for (int i = 0; i < Main.peerCount - 1; i++) {
				System.out.print(" " + serverOther.get(i));
			}

			System.out.println();
		}else {
			System.out.println("MyMessages is " + myMessages + " other is " + otherMessages);

		}
	}

	/* Clear the unnecessary messages */
	public synchronized void clearOutgoing(JupiterTextMessage rtm) {
		if (peerIndex == Main.rootPeer)
			outgoing = serverOutgoing.get(new Integer(rtm.sender - 1));

		Iterator<JupiterTextMessage> it = outgoing.iterator();
		System.out.println("Initial outgoing size " + outgoing.size());
		while (it.hasNext()) {
			JupiterTextMessage jtm = it.next();
			if (jtm == null) {
				System.out.println("Obiect null in lista");
				it.remove();
				continue;
			}
			System.out.println("[Queue] " + jtm);
			if (jtm.myMessages < rtm.otherMessages)
				it.remove();
		}
		if (peerIndex == Main.rootPeer) {
			System.out.println("In clear pun coada la " + (rtm.sender - 1));
			serverOutgoing.put(new Integer(rtm.sender - 1), outgoing);
		}
	}

	public synchronized void onReceive(TextMessage tm) {
		int i;
		JupiterTextMessage rtm = (JupiterTextMessage)tm,
				initial = JupiterTextMessage.duplicate(rtm);
		printParams();


		/* Clear the unnecessary messages */
		clearOutgoing(rtm);


		/* Transform the message */
		if (peerIndex == Main.rootPeer)
			outgoing = serverOutgoing.get(new Integer(rtm.sender - 1));
		System.out.println("After outgoing size " + outgoing.size());
		for (i = 0; i < outgoing.size(); i++) {
			JupiterTextMessage elem = outgoing.get(i);
			if (elem == null)
				continue;

			initial = JupiterTextMessage.duplicate(rtm);
			rtm = (JupiterTextMessage)DOPTTransformation.transform(rtm, elem);
			elem = (JupiterTextMessage)DOPTTransformation.transform(elem, initial);
			if (initial != null)
				outgoing.set(i, elem);
			else outgoing.remove(i);	
			if (rtm == null || elem == null)
				break;
		}

		/* All the peers apply the message transformed */
		deliverMessage(rtm);
		System.out.println("All delivered");

		/* They also update the otherMessages field */
		if (peerIndex == Main.rootPeer) {
			serverOther.set(initial.sender - 1, serverOther.get(initial.sender - 1) + 1);
			System.out.println("Pun coada inapoi la " + (initial.sender - 1));
			serverOutgoing.put(new Integer(initial.sender - 1), outgoing);
		} else
			otherMessages++;

		/* The client peers return */
		if (peerIndex != Main.rootPeer || rtm == null) {
			System.out.println("Inainte de print params");
			printParams();
			System.out.println("----------------------------");
			System.out.println();
			return;
		}

		/* The root peer broadcasts the message as if it was his*/
		rtm.priority = this.peerIndex;
		prepareSendMessage(rtm, true);
		printParams();
		System.out.println("----------------------------");
		System.out.println();
	}

	/* Deliver message to GUI */
	public synchronized void deliverMessage(TextMessage request) {
		System.out.println("Delivering " + request);
		if (request == null) {
			System.out.println("The message to be delivered is null");
			return;
		}

		/* Perform action */
		synchronized (JupiterNetworkManager.lock) {
			switch (request.type) {
			case TextMessage.DELETE:
				gui.deleteChar(request.pos);
				break;

			case TextMessage.INSERT:
				gui.insertChar(request.pos, request.c);
				break;
			}
		}
		System.out.println("Am iesit din deliver");
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

	public synchronized void prepareSendMessage(JupiterTextMessage tm, boolean ignoreSender) {
		JupiterTextMessage forQueue;
		/* Send message to the server */
		if (peerIndex != Main.rootPeer) {
			peerSender.send(tm);
			myMessages++;
			outgoing.add(tm);
		} else {
			/* Broadcast message */
			for (int i = 0; i < Main.peerCount - 1; i++) {
				if (ignoreSender && (i == tm.sender - 1))
					continue;
				forQueue = JupiterTextMessage.duplicate(tm);
				forQueue.otherMessages = serverOther.get(i).intValue();
				forQueue.myMessages = servMessages.get(i).intValue();
				senders.get(i).send(forQueue);

				/* Add the message to the list of outgoing */
				outgoing = serverOutgoing.get(new Integer(i));
				outgoing.add(forQueue);
				serverOutgoing.put(new Integer(i), outgoing);
				servMessages.set(i, servMessages.get(i) + 1);
			}
		}
		System.out.println("Am iesit din functia de trimies");
	}

	@Override
	public synchronized void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);

		/* Create the message and send it */
		JupiterTextMessage tm = new JupiterTextMessage(pos, c, TextMessage.INSERT, peerIndex,
				null, peerIndex, myMessages, otherMessages);

		prepareSendMessage(tm, false);
	}

	@Override
	public synchronized void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);

		JupiterTextMessage tm = new JupiterTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
				null, peerIndex, myMessages, otherMessages);

		prepareSendMessage(tm, false);
	}
}
