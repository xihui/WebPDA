package org.webpda.server.datainterface.controlsystem;


import org.epics.util.time.Timestamp;
import org.epics.vtype.AlarmSeverity;

import static org.junit.Assert.*;

import org.junit.Test;

public class CSDoubleTest {

	@Test
	public void testToJson() {
		CSNumber csDouble = new CSNumber(Timestamp.of(123456, 12333333), 123.34, AlarmSeverity.MAJOR, "HIHI");
		String json = csDouble.toJson();
		System.out.println(json);
		assertEquals(json, 
				"{\"t\":{\"s\":123456,\"ns\":12333333},\"sev\":\"MAJOR\",\"an\":\"HIHI\",\"v\":123.34}");

	}

}
