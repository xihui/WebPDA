package org.webpda.server.war.clientcommand;

public abstract class AbstractPVCommand extends AbstractClientCommand{
	
	private String pvName;

	public String getPvName() {
		return pvName;
	}

	public void setPvName(String pvName) {
		this.pvName = pvName;
	}

	

}
