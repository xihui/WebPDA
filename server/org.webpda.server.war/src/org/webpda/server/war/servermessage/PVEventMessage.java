package org.webpda.server.war.servermessage;


public class PVEventMessage extends AbstractServerMessage{

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


	

	
	
	
	
	
}
