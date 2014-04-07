package dopt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import communication.TextMessage;

public class DOPTHandler {
	public DOPTNetworkManager dnm;
	public LinkedList<DOPTTextMessage> queue, log;
	public Lock logLock = new ReentrantLock();
	public Lock queueLock = new ReentrantLock();

	public DOPTHandler(DOPTNetworkManager dnm) {
		this.dnm = dnm;

		queue = new LinkedList<DOPTTextMessage>();
		log = new LinkedList<DOPTTextMessage>();
	}

	/* Called when the network manager received a message */
	public void messageReceived(DOPTTextMessage tm) {
		/* Add message to the queue */
		addMessage(tm);

		/* Check for execution */
		checkForDelivery();
	}

	/* Insert a message in the message queue */
	public void addMessage(DOPTTextMessage tm) {
		System.out.println("Adding message to queue " + tm.hashCode());
		queue.add(tm);
	}

	public void removeMessage(DOPTTextMessage tm) {
		System.out.println("Removing from queue, before " + queue.size() + " object " + tm.hashCode());
		System.out.println("Index is " + queue.indexOf(tm));
		queue.remove(tm);
		System.out.println("After " +queue.size());
	}

	/* Check if any of the queued message can be delivered */
	public void checkForDelivery() {
		boolean found = true;
		DOPTTextMessage mostRecent, initialRef;
		System.out.print("CHECKING DELIVERY at " + dnm.peerIndex);
		for (int j = 0; j < dnm.stateVector.size(); j++)
			System.out.print(" " + dnm.stateVector.get(j));
		System.out.println();
		printQueue(queue, "message queue");
		printQueue(log, "log");
		while (found) {
			found = false;
			for (TextMessage tm : queue) {
				System.out.println("Checking ");
				DOPTTextMessage dtm = (DOPTTextMessage)tm;
				initialRef = dtm;
				int comp = compareStateVectors(dtm.stateVector, dnm.getStateVector());

				switch (comp) {
				case 1:
					/* The request cannot be executed at the current time */
					continue;

				case 0:
					/* Immediately deliver the message */
					dnm.deliverMessage(tm);
					continue;

				case -1:
					System.out.println("Intru la probleme");
					mostRecent = getMostRecent(dtm.stateVector, null);

					while (mostRecent != null && dtm != null) {
						System.out.println("Most recent is " + mostRecent);
						if (dtm.stateVector.get(mostRecent.sender) <=
								mostRecent.stateVector.get(mostRecent.sender)) {
							dtm = DOPTTransformation.transform(dtm, mostRecent);
						}
						if (dtm != null)
							mostRecent = getMostRecent(dtm.stateVector, mostRecent);
						else
							removeMessage(initialRef);
					}
					if (dtm != null) {
						dnm.deliverMessage(dtm);
						found = true;
					}
				}
			}
		}
	}

	public synchronized void pushLog(DOPTTextMessage tm) {
		log.add(tm);
	}

	public int compareStateVectors(ArrayList<Integer> s1, ArrayList<Integer> s2) {
		int first, second;
		boolean equal = true, smaller = true, greater = true;

		for (int i = 0; i < s1.size(); i++) {
			first = s1.get(i);
			second = s2.get(i);

			if (first != second)
				equal = false;

			if (first > second)
				smaller = false;
		}

		if (equal) return 0;
		if (smaller) return -1;
		if (greater) return 1;

		System.out.println("Inconsistent state vectors");
		System.exit(-1);
		return 0;
	}

	public DOPTTextMessage getMostRecent(ArrayList<Integer> stateVector, DOPTTextMessage start) {
		boolean consider = false;
		DOPTTextMessage result = null;

		if (start == null)
			consider = true;
		System.out.println("Get most recent, start = " + start);

		logLock.lock();

		Iterator<DOPTTextMessage> it = log.descendingIterator();
		while (it.hasNext()) {
			System.out.println("Intru in bucla");
			TextMessage tm = it.next();
			DOPTTextMessage current = (DOPTTextMessage)tm;

			if (consider) {
				int comp = compareStateVectors(current.stateVector, stateVector);
				System.out.println("Comp dintr " + current + " si al meu e " + comp);

				if (comp <= 0) {
					System.out.println("Am gasit " + current);
					logLock.unlock();
					return current;
				}
			} else if (start.equals(tm)) {
				System.out.println("Found where I left, continuing");
				consider = true;
			}
		}
		System.out.println("Dupa bucla");

		logLock.unlock();
		return result;
	}

	public void printQueue(LinkedList<DOPTTextMessage> queue, String message) {
		System.out.println("Printing the " + message);
		for (TextMessage tm : queue)
			System.out.println("------>" + tm);
	}
}
