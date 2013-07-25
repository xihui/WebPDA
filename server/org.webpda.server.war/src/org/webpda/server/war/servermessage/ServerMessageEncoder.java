package org.webpda.server.war.servermessage;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

import com.fasterxml.jackson.core.JsonProcessingException;

public class ServerMessageEncoder implements Encoder.Text<AbstractServerMessage>{

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(EndpointConfig config) {
		
	}

	@Override
	public String encode(AbstractServerMessage object) throws EncodeException {	
		try {
			return object.toJson();
		} catch (JsonProcessingException e) {
			throw new EncodeException(object, "Failed to encode " + object, e);
		}		
	}

	
	
	
}
