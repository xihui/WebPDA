/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.servermessage;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.webpda.server.core.Constants;
import org.webpda.server.core.util.JsonUtil;
import org.webpda.server.core.util.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;


/**A server message that represents a PV related event.
 * @author Xihui Chen
 *
 */
public class PVEventMessage extends AbstractTextMessage{

	private static final String DATA = "d";

	private static final String EVENT = "e";

	private static final String PVNAME = "pv";

	//pv id.
	private int id;
	
	private PVEventType evt;
	
	private Object data;

	private boolean isRawJson;

	public PVEventMessage(int pvId, PVEventType eventType, Object data, boolean isRawJson) {
		this.id = pvId;
		this.evt = eventType;
		this.data = data;
		this.isRawJson =isRawJson;
	}


	
	@Override
	public String createJson() throws JsonProcessingException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(outputStream);
			jg.writeStartObject();
			jg.writeNumberField(PVNAME, id);
			jg.writeStringField(EVENT, evt.name());
			jg.writeFieldName(DATA);
			if(isRawJson)
				jg.writeRaw(":"+data);
			else
				jg.writeObject(data);
			jg.writeEndObject();
			jg.close();
			String s = outputStream.toString(Constants.CHARSET);
//			System.out.println(s.length() + s);
			outputStream.close();
			return s;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create json.", e);
		}
		
		return null;
	}
	
	
}
