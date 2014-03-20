package CBCASTModule;

import java.util.LinkedList;
import java.util.Queue;

import communication.TextMessage;
import communication.TimeVector;

import engine.Main;

public class CBCASTHandler {
	/* This is the waiting list of CBCAST */
	private Queue<TextMessage> waitingList;
	private CBCASTNetworkManager nm;
	
	public CBCASTHandler(CBCASTNetworkManager nm) {
		waitingList = new LinkedList<TextMessage>();
		this.nm = nm;
	}

	public void notifyIncommingMessage() {
		System.out.println("CBCAST notifier");
		/* Check all messages in the queue to see
		 * if any of them needs to be delivered
		 */
		synchronized (CBCASTHandler.class) {
			for (TextMessage tm : waitingList)
				if (!delayMessage(tm)) {
					waitingList.remove(tm);
					nm.deliverMessage(tm);
				}	
		}
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

	public void messageReceived(TextMessage tm) {
		if (delayMessage(tm))
			synchronized (CBCASTHandler.class) {
				System.out.println("                   ADDING TO QUEEUE");
				waitingList.add(tm);
			}
		else
			nm.deliverMessage(tm);
	}
}
