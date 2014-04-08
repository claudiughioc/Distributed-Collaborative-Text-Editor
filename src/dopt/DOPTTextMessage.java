package dopt;

import java.util.ArrayList;

import communication.TextMessage;
import communication.TimeVector;

public class DOPTTextMessage extends TextMessage{

	private static final long serialVersionUID = 1L;
	public int priority;
	public ArrayList<Integer> stateVector;

	public DOPTTextMessage(int pos, char c, int type,
			int sender, TimeVector msgVT, int priority,
			ArrayList<Integer> stateVector) {
		super(pos, c, type, sender, null);

		this.priority = priority;
		this.stateVector = stateVector;
	}

	public String toString() {
		String s = super.toString() + " prio " + priority + " state: ";
		if (stateVector != null) {
			for (int i = 0; i < stateVector.size(); i++)
				s += stateVector.get(i) + " ";
		}
		return s;
	}

}
