package comm;

import java.io.Serializable;
import java.util.ArrayList;

public class TimeVector implements Serializable {
	public int peerCount;
	public ArrayList<Integer> VT;

	public TimeVector(int peerCount) {
		this.peerCount = peerCount;
		VT = new ArrayList<Integer>(peerCount);
		for (int i = 0; i < peerCount; i++)
			VT.add(0);
	}

	public void updateVT(ArrayList<Integer> newVT) {
		this.VT = newVT;
	}
	
	public String toString() {
		String s = "[";
		
		for (int i = 0; i < peerCount; i++) {
			s += VT.get(i);
			if (i == peerCount - 1)
				break;
			s += ", ";
		}
		s += "]";
		return s;
	}
}
