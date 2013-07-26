package org.webpda.server.war.servermessage;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;

import org.webpda.server.core.Constants;
import org.webpda.server.core.JsonUtil;
import org.webpda.server.core.LoggerUtil;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;


public class PVEventMessage implements IServerMessage{

	private String pv;
	
	private PVEventType evt;
	
	private Object data;

	public PVEventMessage(String pvName, PVEventType eventType, Object data) {
		this.setPv(pvName);
		this.setEvt(eventType);
		this.data = data;
	}


	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}


	public PVEventType getEvt() {
		return evt;
	}


	public void setEvt(PVEventType evt) {
		this.evt = evt;
	}


	public String getPv() {
		return pv;
	}


	public void setPv(String pv) {
		this.pv = pv;
	}

	@Override
	public String toJson() throws JsonProcessingException {
		try {
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			JsonGenerator jg = JsonUtil.jsonFactory.createGenerator(outputStream);
			jg.writeStartObject();
			jg.writeStringField("pv", pv);
			jg.writeStringField("evt", evt.name());
			jg.writeObjectField("data", data);				
			jg.writeEndObject();
			jg.close();
			String s = outputStream.toString(Constants.CHARSET);
			outputStream.close();
			return s;
		} catch (Exception e) {
			LoggerUtil.getLogger().log(Level.SEVERE, "Failed to create json.", e);
		}
		
		return null;
	}
	
	
}
