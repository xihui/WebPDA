/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.servermessage;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.webpda.server.core.Constants;
import org.webpda.server.core.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A server message that list all pvs on a client.
 * 
 * @author Xihui Chen
 * 
 */
public class ListAllPVsMessage extends NonPVEventMessage {

	private static final String PVS = "pvs";

	private String[] pvNames;

	public ListAllPVsMessage(String[] pvNames) {
		this.pvNames = pvNames;
	}

	@Override
	public String toJson() throws JsonProcessingException {
		try {
			JsonGenerator jg = createJsonGenerator();
			jg.writeArrayFieldStart(PVS);
			for (String s : pvNames)
				jg.writeString(s);
			jg.writeEndArray();
			jg.writeEndObject();
			jg.close();
			ByteArrayOutputStream outputStream = (ByteArrayOutputStream) jg
					.getOutputTarget();
			String s = outputStream.toString(Constants.CHARSET);
			outputStream.close();
			return s;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create json.",
					e);
		}

		return null;
	}

}