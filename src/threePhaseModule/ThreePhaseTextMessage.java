package threePhaseModule;

import java.sql.Timestamp;

import communication.TextMessage;
import communication.TimeVector;

public class ThreePhaseTextMessage extends TextMessage {
	private static final long serialVersionUID = 1L;
	public int tag, phase;
	public boolean deliverable;
	public Timestamp timestamp;
	
	public ThreePhaseTextMessage(int pos, char c, int type,
			int sender, TimeVector msgVT, int tag,
			Timestamp timestamp, int phase, boolean deliverable) {
		super(pos, c, type, sender, msgVT);
		
		this.tag = tag;
		this.timestamp = timestamp;
		this.phase = phase;
		this.deliverable = deliverable;
	}
}
