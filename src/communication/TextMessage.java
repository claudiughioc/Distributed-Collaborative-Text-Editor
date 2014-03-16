package communication;

import java.io.Serializable;

public class TextMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int INSERT	= 1;
	public static final int DELETE	= 2;
	
	public int pos, type, sender;
	public char c;
	public TimeVector timeVector;
	
	public TextMessage(int pos, char c, int type,
			int sender, TimeVector msgVT) {
		this.pos = pos;
		this.c = c;
		this.type = type;
		this.timeVector = msgVT;
		this.sender = sender;
	}

	public String toString() {
		String s = "From " + sender + ", position " + pos + " char " + c + " type " + type + " vt: " + timeVector;
		return s;
	}
}
