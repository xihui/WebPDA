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
import org.webpda.server.core.util.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * A server message that has normal information for the client.
 * 
 * @author Xihui Chen
 * 
 */
public class InfoMessage extends NonPVEventMessage {


	private String title, details;
	public InfoMessage(String title, String details) {
		this.title = title;
		this.details = details;
	}

	@Override
	public String createJson() throws JsonProcessingException {
		try {
			JsonGenerator jg = createJsonGenerator();
			jg.writeStringField("title",title);
			jg.writeStringField("details", details);			
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
