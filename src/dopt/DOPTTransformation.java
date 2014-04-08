package dopt;

import communication.TextMessage;


public class DOPTTransformation {
	public static final int INSERT_INSERT = 1;
	public static final int DELETE_DELETE = 2;
	public static final int INSERT_DELETE = 3;
	public static final int DELETE_INSERT = 4;

	public static DOPTTextMessage transform_II(DOPTTextMessage tm1, DOPTTextMessage tm2) {
		System.out.println("Transforming " + tm1.hashCode() + ":" + tm1 + " with the help of " + tm2);
		if (tm1.pos < tm2.pos)
			return tm1;
		else if (tm1.pos > tm2.pos) {
			tm1.pos++;
			return tm1;
		} else {
			if (tm1.c == tm2.c)
				return null;
			else {
				if (tm1.priority > tm2.priority) {
					tm1.pos++;
					return tm1;
				} else {
					return tm1;
				}
			}
		}
	}

	public static DOPTTextMessage transform_DD(DOPTTextMessage tm1, DOPTTextMessage tm2) {
		if (tm1.pos  < tm2.pos)
			return tm1;
		else if (tm1.pos > tm2.pos) {
			tm1.pos--;
			return tm1;
		} else
			return null;
	}

	public static DOPTTextMessage transform_ID(DOPTTextMessage tm1, DOPTTextMessage tm2) {
		if (tm1.pos <= tm2.pos)
			return tm1;
		else {
			tm1.pos--;
			return tm1;
		}
	}

	public static DOPTTextMessage transform_DI(DOPTTextMessage tm1, DOPTTextMessage tm2) {
		if (tm1.pos < tm2.pos)
			return tm1;
		else {
			tm1.pos++;
			return tm1;
		}
	}

	public static DOPTTextMessage transform(DOPTTextMessage tm1, DOPTTextMessage tm2) {
		
		if (tm1.type == TextMessage.INSERT && tm2.type == TextMessage.INSERT)
			return transform_II(tm1, tm2);
		if (tm1.type == TextMessage.DELETE && tm2.type == TextMessage.DELETE)
			return transform_DD(tm1, tm2);
		if (tm1.type == TextMessage.INSERT && tm2.type == TextMessage.DELETE)
			return transform_ID(tm1, tm2);
		if (tm1.type == TextMessage.DELETE && tm2.type == TextMessage.INSERT)
			return transform_DI(tm1, tm2);
		
		System.out.println("No operation specified on transformation");
		return null;
	}
}
