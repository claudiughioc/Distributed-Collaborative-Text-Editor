package ABCASTModule;

import java.util.LinkedList;
import java.util.Queue;

import CBCASTModule.CBCASTHandler;

import communication.TextMessage;
import communication.TimeVector;

import engine.Main;

public class ABCASTHandler extends CBCASTHandler {
	/* This is the waiting list of CBCAST */
	private Queue<TextMessage> waitingList;
	private Queue<ABCASTTextMessage> ABCASTList;
	private LinkedList<Integer> uidList;
	private Queue<TextMessage> setOrderList;
	private ABCASTNetworkManager nm;

	public ABCASTHandler(ABCASTNetworkManager nm) {
		super(nm);
		this.nm = nm;

		System.out.println("HANDLER Constructor nm = " + nm);
		this.ABCASTList = new LinkedList<ABCASTTextMessage>();
		this.setOrderList = new LinkedList<TextMessage>();
		this.waitingList = new LinkedList<TextMessage>();
	}

	public void startUIDList(TextMessage tm) {
		ABCASTTextMessage atm = (ABCASTTextMessage) tm;
		uidList = new LinkedList<Integer>();
		uidList.add(atm.uid);
	}

	/* Send set order message after all the messages have been delivered */
	public void finishUIDList() {
		System.out.println("Token holder " + nm.peerIndex + " sends set order message ");
		nm.setOrder(uidList);
	}


	public void notifyIncommingMessage() {
		System.out.println("ABCAST notifier");
		/* Check all messages in the queue to see
		 * if any of them needs to be delivered
		 */
		synchronized (waitingList) {
			for (TextMessage tm : waitingList)
				if (!delayMessage(tm)) {
					uidList.add(((ABCASTTextMessage)tm).uid);
					nm.deliverMessage(tm);
					waitingList.remove(tm);
				}
		}
		if (nm.hasToken)
			finishUIDList();
	}


	/* Check if a message needs to be delivered or put in the queue */
	private boolean delayMessage(TextMessage tm) {
		boolean delay = false;
		TimeVector msgTimeVector = tm.timeVector;
		TimeVector myTimeVector = nm.timeVector;


		for (int i = 0; i < Main.peerCount; i++) {
			if (i == tm.sender) {
				if (msgTimeVector.VT.get(i) != myTimeVector.VT.get(i) + 1)
					delay = true;
			} else {
				if (msgTimeVector.VT.get(i) > myTimeVector.VT.get(i))
					delay = true;
			}
		}

		if (delay)
			System.out.println("Delaying " + tm + " my vector is " + myTimeVector);

		return delay;
	}


	/* Determine if a message is before a second one */
	private boolean before(TextMessage first, TextMessage second) {
		TimeVector oneTimeVector = first.timeVector;
		TimeVector twoTimeVector = second.timeVector;
		boolean before = true;

		for (int i = 0; i < Main.peerCount; i++) {
			if (oneTimeVector.VT.get(i) > twoTimeVector.VT.get(i))
				before = false;
		}

		return before;
	}

	/* Deliver all the messages before a set order message */
	public void deliverSetOrderList(TextMessage tm) {
		ABCASTTextMessage atm = (ABCASTTextMessage)tm;
		ABCASTTextMessage ref = null;

		for (Integer uid : atm.uidList) {
			for (ABCASTTextMessage abm : ABCASTList) {
				if (abm.uid == uid) {
					ref = abm;
					ABCASTList.remove(abm);
				}
			}

			if (ref == null)
				continue;

			/* Deliver all CBAST and ABCAST messages which precede the refference */
			for (TextMessage abm : waitingList) {
				if (((ABCASTTextMessage)abm).uid == ref.uid)
					continue;

				if (before(abm, ref)) {
					waitingList.remove(abm);
					System.out.println("Delivering normal messages");
					nm.deliverMessage(abm);
				} else
					break;
			}
			System.out.println("Delivering the ref");
			waitingList.remove(ref);
			nm.deliverMessage(ref);
		}

		/* Remove the set order id message */
		setOrderList.remove(tm);
	}


	/* Check if all the ABCAST messages have arrived for a set order message */
	public void checkForDelivery() {
		boolean allPresent, exists;
		ABCASTTextMessage atm;


		for (TextMessage tm : setOrderList) {
			atm = (ABCASTTextMessage) tm;
			allPresent = true;
			for (Integer uid : atm.uidList) {
				exists = false;
				for (ABCASTTextMessage abm : ABCASTList)
					if (abm.uid == uid)
						exists = true;
				if (exists == false) {
					allPresent = false;
					break;
				}
			}
			if (allPresent)
				synchronized (waitingList) {
					deliverSetOrderList(tm);
				}
		}
	}

	/* Define the behaviour of a non token peer then
	 * it receives a message
	 */
	public void nonToken(TextMessage tm) {
		ABCASTTextMessage atm = (ABCASTTextMessage)tm;
		System.out.println("NON TOKEN HANDLER\n");

		/* Add the message to a specific queue */
		switch (atm.phase) {
		case ABCASTTextMessage.ABCAST_MSG:
			ABCASTList.add(atm);
			waitingList.add(tm);
			System.out.println("                   ADDING TO ABCAST QUEEUE");
			break;

		case ABCASTTextMessage.CBAST_MSG:
			System.out.println("                   ADDING TO CBCAST QUEEUE");
			waitingList.add(tm);
			break;

		case ABCASTTextMessage.SET_ORDER:
			System.out.println("                   ADDING TO SET-ORDER QUEEUE");
			setOrderList.add(tm);
			break;
		}

		checkForDelivery();
	}

	public synchronized void messageReceived(TextMessage tm) {

		/* The token holder treats any message normally, as CBAST message */
		if (nm.hasToken) {
			System.out.println("TOOOKKEEEEN");
			if (delayMessage(tm)) {
				System.out.println("                   ADDING TO QUEEUE");
				waitingList.add(tm);
			} else {
				startUIDList(tm);
				nm.deliverMessage(tm);
			}
		} else
			nonToken(tm);
	}
}
