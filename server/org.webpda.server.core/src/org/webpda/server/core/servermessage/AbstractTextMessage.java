/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.core.servermessage;

import java.nio.ByteBuffer;

import com.fasterxml.jackson.core.JsonProcessingException;

/**Abstract server message in text format.
 * @author Xihui Chen
 *
 */
public abstract class AbstractTextMessage implements IServerMessage{

	protected String json;
	
	@Override
	public String toJson() throws JsonProcessingException {
		if(json == null)
			this.json = createJson();
		return json;
	}
	
	@Override
	public int getMessageSizeInBytes() throws JsonProcessingException {
		if(json == null)
			this.json = createJson();
		return json.length()*2;
	}
	
	protected abstract String createJson() throws JsonProcessingException;
	
	@Override
	public boolean isBinary() {
		return false;
	}
	
	@Override
	public ByteBuffer toByteBuffer() {
		return null;
	}
}
