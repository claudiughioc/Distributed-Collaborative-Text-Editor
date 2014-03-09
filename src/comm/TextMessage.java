package comm;

import java.io.Serializable;

public class TextMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final int INSERT	= 1;
	public static final int DELETE	= 2;
	
	public int pos, type;
	public char c;
	
	public TextMessage(int pos, char c, int type) {
		this.pos = pos;
		this.c = c;
		this.type = type;
	}
}
