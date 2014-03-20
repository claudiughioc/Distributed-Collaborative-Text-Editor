package ABCASTModule;

import java.util.LinkedList;

import communication.TextMessage;
import communication.TimeVector;

public class ABCASTTextMessage extends TextMessage {
	public static final int CBAST_MSG = 1;
	public static final int ABCAST_MSG = 2;
	public static final int SET_ORDER = 3;

	private static final long serialVersionUID = 1L;
	public boolean deliverable;
	public int uid, phase;
	public LinkedList<Integer> uidList;

	public ABCASTTextMessage(LinkedList<Integer> uidList, TimeVector msgVT) {
		this.uidList = uidList;
		this.timeVector = msgVT;
		phase = SET_ORDER;
	}

	public ABCASTTextMessage(int pos, char c, int type,
			int sender, TimeVector msgVT, int uid,
			int phase, boolean deliverable) {
		super(pos, c, type, sender, msgVT);
		
		this.uid = uid;
		this.phase = phase;
		this.deliverable = deliverable;
	}

	@Override
	public String toString() {
		String s = super.toString();
		s += " phase " + phase + " del " + deliverable;
		if (phase == SET_ORDER) {
			s += "[";
			for (int i = 0; i < uidList.size(); i++)
				s += uidList.get(i) + " ";
			s += "]";
		}
		return s;
	}
}
