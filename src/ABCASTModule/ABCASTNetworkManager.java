package ABCASTModule;

import java.util.LinkedList;

import communication.TextMessage;

import engine.Main;
import CBCASTModule.CBCASTNetworkManager;

public class ABCASTNetworkManager extends CBCASTNetworkManager {
	public boolean hasToken;
	public int uid;
	public ABCASTHandler commProto;

	public ABCASTNetworkManager(int peerIndex) {
		super(peerIndex);
		uid = peerIndex * 10000;

		commProto = new ABCASTHandler(this);

		/* Initialize the token holder as being the peer 0 */
		if (peerIndex == 0)
			setToken(true);
	}

	public synchronized void insert(int pos, char c) {
		System.out.println("ABCAST MANAGER is going to broadcast an insertion " + c + " at " + pos);
		int newUID = uid++;
		ABCASTTextMessage tm;

		/* Update the time vector */
		timeVector.VT.set(peerIndex, timeVector.VT.get(peerIndex) + 1);

		/* Create the message and send it */
		if (hasToken) {
			tm = new ABCASTTextMessage(pos, c, TextMessage.INSERT, peerIndex,
					timeVector, newUID, ABCASTTextMessage.ABCAST_MSG, true);
		} else {
			tm = new ABCASTTextMessage(pos, c, TextMessage.INSERT, peerIndex,
					timeVector, newUID, ABCASTTextMessage.ABCAST_MSG, false);
		}

		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);

		/* Send set Order message */
		if (hasToken) {
			LinkedList<Integer> uidList = new LinkedList<Integer>();
			uidList.add(newUID);
			setOrder(uidList);
		}
	}

	public void delete(int pos) {
		int newUID = uid++;
		ABCASTTextMessage tm;
		System.out.println("ABCAST MANAGER is going to broadcast a deletion at " + pos);

		/* Update the time vector */
		timeVector.VT.set(peerIndex, timeVector.VT.get(peerIndex) + 1);

		/* Create the message and send it */
		if (hasToken) {
			tm = new ABCASTTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
					timeVector, newUID, ABCASTTextMessage.ABCAST_MSG, true);
		} else {
			tm = new ABCASTTextMessage(pos, 'q', TextMessage.DELETE, peerIndex,
					timeVector, newUID, ABCASTTextMessage.ABCAST_MSG, false);
		}

		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(tm);

		/* Send set Order message */
		if (hasToken) {
			LinkedList<Integer> uidList = new LinkedList<Integer>();
			uidList.add(newUID);
			setOrder(uidList);
		}
	}

	public void setOrder(LinkedList<Integer> uidList) {
		System.out.println("ABCAST MANAGER is going to broadcast a set order");

		/* Update the time vector */
		timeVector.VT.set(peerIndex, timeVector.VT.get(peerIndex) + 1);

		ABCASTTextMessage atm = new ABCASTTextMessage(uidList, timeVector);
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).send(atm);
	}

	public void run() {
		System.out.println("ABCAST Network manager " + peerIndex + " starts the threads ");


		/* Start the receiver and sender sockets */
		receiver.start();
		for (int i = 0; i < Main.peerCount - 1; i++)
			senders.get(i).start();
	}

	public void onReceive(TextMessage tm) {
		this.commProto.messageReceived(tm);
	}

	public synchronized void setToken(boolean value) {
		this.hasToken = value;
	}
}
