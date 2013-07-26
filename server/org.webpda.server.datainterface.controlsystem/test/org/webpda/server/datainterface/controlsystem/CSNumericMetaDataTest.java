package org.webpda.server.datainterface.controlsystem;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CSNumericMetaDataTest {

	@Test
	public void testToJson() {
		CSNumericMetaData data = new CSNumericMetaData(0, 123.12, 12.3, 60, 2.121, 78.23, 3, "count");
		String json = data.toJson();
		System.out.println(json);		
		assertEquals(json, 
				"{\"dl\":0.0,\"dh\":123.12,\"wl\":12.3,\"wh\":60.0,\"al\":2.121,\"ah\":78.23,\"prec\":3,\"units\":\"count\"}");

	}

}
