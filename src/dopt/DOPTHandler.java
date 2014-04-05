package dopt;

import java.util.LinkedList;

import communication.TextMessage;

public class DOPTHandler {
	public DOPTNetworkManager dnm;
	public LinkedList<TextMessage> queue, log;

	public DOPTHandler(DOPTNetworkManager dnm) {
		this.dnm = dnm;

		queue = new LinkedList<TextMessage>();
		log = new LinkedList<TextMessage>();
	}

	/* Called when the network manager received a message */
	public void messageReceived(TextMessage tm) {
		/* Add message to the queue */
		addMessage(tm);

		/* Check for execution */
	}

	/* Insert a message in the message queue */
	public synchronized void addMessage(TextMessage tm) {
		System.out.println("Adding message to queue");
		queue.add(tm);
	}

	/* Check if any of the queued message can be delivered */
	public void checkForDelivery() {
		
	}
} 
