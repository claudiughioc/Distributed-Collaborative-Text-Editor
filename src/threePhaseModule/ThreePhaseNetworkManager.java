package threePhaseModule;

import engine.Main;
import gui.GUIManager;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

import communication.NetworkManager;
import communication.ReceiveManager;
import communication.Sender;
import communication.TextMessage;

public class ThreePhaseNetworkManager extends NetworkManager {
	public static final int PHASE_1 = 1;
	public static final int PHASE_2 = 2;
	public static final int PHASE_3 = 3;

	public int peerIndex, mtag;
	private HashMap<Integer, Sender> senders;
	private ReceiveManager receiver;
	public GUIManager gui;
	public long clock, priority;

	public PriorityQueue<ThreePhaseTextMessage> tempQueue, deliverableQueue;
	public HashMap<Integer, Status> tempTSS;

	public ThreePhaseNetworkManager(int peerIndex) {
		this.peerIndex = peerIndex;

		/* Initialize the clock */
		this.clock = 0;
		this.mtag = 0;
		this.priority = 0;

		/* Initialize the timestamp sorted queues */
		ThreePhaseComparator tpc = new ThreePhaseComparator();
		tempQueue = new PriorityQueue<ThreePhaseTextMessage>(10000, tpc);
		deliverableQueue = new PriorityQueue<ThreePhaseTextMessage>(10000, tpc);

		/* Initialize the Hashmap of temporary timestamps */
		tempTSS = new HashMap<Integer, Status>();
	}

	@Override
	public synchronized void insert(int pos, char c) {
		System.out.println("I am going to broadcast an insertion " + c + " at " + pos);
		clock++;

		/* Create the message and send it */
		ThreePhaseTextMessage tm = new ThreePhaseTextMessage(pos, c, TextMessage.INSERT, peerIndex, null, 
				mtag, clock, PHASE_1, false);
		for (int i = 0; i < Main.peerCount; i++) {
			if (i == peerIndex)
				continue;
			senders.get(i).send(tm);
		}

		/* Save the initial temporary timestamp for the message */
		tempTSS.put(mtag, new Status(0, new Long(0)));
		mtag++;
	}

	@Override
	public synchronized void delete(int pos) {
		System.out.println("I am going to broadcast a deletion at " + pos);
		clock++;

		/* Create the message and send it */
		ThreePhaseTextMessage tm = new ThreePhaseTextMessage(pos, 'q', TextMessage.DELETE, peerIndex, null,
				mtag, clock, PHASE_1, false);
		for (int i = 0; i < Main.peerCount; i++) {
			if (i == peerIndex)
				continue;
			senders.get(i).send(tm);
		}

		/* Save the initial temporary timestamp for the message */
		tempTSS.put(mtag, new Status(0, new Long(0)));
		mtag++;
	}


	/* Create the sender and receiver threads */
	public void initiateCommThreads() {
		System.out.println("3 Phase Network Manager creates the threads");

		/* Create listener and sender threads */
		senders = new HashMap<Integer, Sender>(Main.peerCount - 1);
		try {
			/* Create the listener socket */
			ServerSocket listener = new ServerSocket(Main.ports.get(peerIndex));
			receiver = new ReceiveManager(listener, this);


			/* Create the sender threads */
			for (int i = 0; i < Main.peerCount; i++) {
				if (i == peerIndex)
					continue;

				senders.put(i, new Sender(Main.IPAdresses.get(i), Main.ports.get(i)));
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
		for (int i = 0; i < Main.peerCount; i++) {
			if (i == peerIndex)
				continue;
			senders.get(i).start();
		}
	}
	
	
	private void handlePhase1(TextMessage tm) {
		ThreePhaseTextMessage tptm = (ThreePhaseTextMessage)tm;
		
		/* Update the priority */
		priority = (priority + 1) > tptm.timestamp ? priority + 1 : tptm.timestamp;
		tptm.timestamp = priority;
		
		/* Insert the message in the temporary queue */
		tempQueue.add(tptm);
		
		/* Send Phase 2 message back to the sender */
		tptm.phase = PHASE_2;
		senders.get(tm.sender).send(tptm);
	}

	
	private void handlePhase2(TextMessage tm) {
		ThreePhaseTextMessage tptm = (ThreePhaseTextMessage)tm;
		System.out.println("Handling Phase 2 for " + tptm);
		Status status = tempTSS.get(new Integer(tptm.tag));
		long tempTS = status.tempTS;

		/* Update the temporary timestamp */
		status.tempTS = tempTS > tptm.timestamp ? tempTS : tptm.timestamp;
		status.count++;
		tempTSS.put(tptm.tag, status);

		/* Check if we got response from all the other peers */
		if (status.count == Main.connectedPeers) {
			System.out.println("Got all the Phase 2 messages, going to send Phase 3");
			tempTSS.remove(tptm.tag);

			/* Send Phase 3 message */
			tptm.timestamp = status.tempTS;
			tptm.phase = PHASE_3;
			for (int i = 0; i < Main.peerCount; i++) {
				if (i == peerIndex)
					continue;
				senders.get(i).send(tm);
			}
			
			/* Update the clock */
			clock = clock > status.tempTS ? clock : status.tempTS;
		}
	}


	private void handlePhase3(TextMessage tm) {
		ThreePhaseTextMessage tptm = (ThreePhaseTextMessage)tm;
		ThreePhaseTextMessage initial = null;
		
		/* Identify the phase 2 entry in the temporary queue */
		for (ThreePhaseTextMessage entry:tempQueue)
			if (entry.tag == tptm.tag) {
				initial = entry;
				tempQueue.remove(entry);
				break;
			}
		
		/* Mark the initial message deliverable */
		if (initial == null) {
			System.out.println("ERROR[Phase 3]: no initial Phase 2 message in temporary queue");
			return;
		}
		initial.deliverable = true;
		
		/* Update the message timestamp and add it back to the temp queue */
		initial.timestamp = tptm.timestamp;
		tempQueue.add(initial);
		
		/* Check for deliverable elements */
		if (tempQueue.peek().tag == initial.tag) {
			deliverMessage(tm);
			tempQueue.poll();
			clock = clock > tptm.timestamp ? clock : tptm.timestamp + 1;
			
			while (!tempQueue.isEmpty() && tempQueue.peek().deliverable) {
				ThreePhaseTextMessage msg = tempQueue.poll();
				deliverMessage(msg);
				clock = clock > msg.timestamp ? clock : msg.timestamp + 1;
			}
		}
	}

	@Override
	public synchronized void onReceive(TextMessage tm) {
		ThreePhaseTextMessage tptm = (ThreePhaseTextMessage)tm;

		switch (tptm.phase) {
		case PHASE_1:
			/* Handle Phase 1 responses */
			handlePhase1(tm);
			break;

		case PHASE_2:
			/* Handle Phase 2 responses */
			handlePhase2(tm);
			break;

		case PHASE_3:
			/* Handle Phase 3 responses */
			handlePhase3(tm);
			break;
		}

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
