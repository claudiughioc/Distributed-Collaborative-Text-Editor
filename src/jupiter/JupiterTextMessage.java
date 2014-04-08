package jupiter;

import communication.TimeVector;

import dopt.DOPTTextMessage;

public class JupiterTextMessage extends DOPTTextMessage {

	private static final long serialVersionUID = 1L;
	public int myMessages, otherMessages;

	public JupiterTextMessage(int pos, char c, int type,
			int sender, TimeVector msgVT, int priority,
			int myMessages, int otherMessages) {
		super(pos, c, type, sender, null, priority, null);

		this.myMessages = myMessages;
		this.otherMessages = otherMessages;
	}
	
	public String toString() {
		return super.toString() + " my: " + myMessages + " other " + otherMessages;
	}


	public static JupiterTextMessage duplicate(JupiterTextMessage initial) {
		return new JupiterTextMessage(initial.pos, initial.c, 
				initial.type, initial.sender, initial.timeVector,
				initial.priority, initial.myMessages, initial.otherMessages);
	}
}
