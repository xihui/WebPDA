package org.webpda.server.war.clientcommand;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EndpointConfig;

import org.webpda.server.core.ConfigurePropertyConstants;
import org.webpda.server.core.JsonUtil;

import com.fasterxml.jackson.databind.JsonNode;

public class ClientCommandDecoder implements Decoder.Text<AbstractClientCommand>{

	private String packageName = AbstractClientCommand.class.getPackage().getName();;

	@Override
	public void destroy() {
		
	}

	@Override
	public void init(EndpointConfig config) {
		
	}

	@Override
	public AbstractClientCommand decode(String arg0) throws DecodeException {
		try {
			JsonNode rootNode = JsonUtil.mapper.readValue(arg0, JsonNode.class);
			String commandName = rootNode.get("commandName").asText();
			Class<?> clazz = Class.forName(
					packageName + "." + commandName + "Command");
			AbstractClientCommand command = 
					(AbstractClientCommand) JsonUtil.mapper.readValue(arg0, clazz);
			String pvFactoryName = System
					.getProperty(ConfigurePropertyConstants.PV_FACTORY_PROPERTY);
			clazz = Class.forName(pvFactoryName);
			Object newInstance = clazz.newInstance();
			return command;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}

	@Override
	public boolean willDecode(String arg0) {
		JsonNode rootNode;
		try {
			rootNode = JsonUtil.mapper.readValue(arg0, JsonNode.class);
			String commandName = rootNode.get("commandName").asText();			
			Class<?> clazz = Class.forName(packageName + "." + commandName + "Command");
			if(clazz != null)
				return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}

	

}
