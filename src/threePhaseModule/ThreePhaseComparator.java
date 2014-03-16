package threePhaseModule;

import java.sql.Timestamp;
import java.util.Comparator;

public class ThreePhaseComparator implements Comparator<ThreePhaseTextMessage> {

	@Override
	public int compare(ThreePhaseTextMessage o1, ThreePhaseTextMessage o2) {
		int res = 0;
		
		Timestamp tp0 = ((ThreePhaseTextMessage)o1).timestamp;
		Timestamp tp1 = ((ThreePhaseTextMessage)o2).timestamp;
		
		if (tp0.before(tp1))
			res = 1;
		if (tp0.after(tp1))
			res = -1;
		
		return res;
	}
}
