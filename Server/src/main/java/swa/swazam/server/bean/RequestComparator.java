package swa.swazam.server.bean;

import java.util.Comparator;

import swa.swazam.server.entity.Request;

public class RequestComparator implements Comparator<Request>{

	@Override
	public int compare(Request o1, Request o2) {
		if(o1.getDate().before(o2.getDate()))
			return 1;
		else if(o1.getDate().after(o2.getDate()))
			return -1;
		return 0;
	}
}
