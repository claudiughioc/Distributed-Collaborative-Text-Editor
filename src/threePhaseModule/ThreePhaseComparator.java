package threePhaseModule;

import java.util.Comparator;

public class ThreePhaseComparator implements Comparator<ThreePhaseTextMessage> {

	@Override
	public int compare(ThreePhaseTextMessage o1, ThreePhaseTextMessage o2) {
		
		long tp0 = ((ThreePhaseTextMessage)o1).timestamp;
		long tp1 = ((ThreePhaseTextMessage)o2).timestamp;
		
		return (int)(tp1 - tp0);
	}
}
