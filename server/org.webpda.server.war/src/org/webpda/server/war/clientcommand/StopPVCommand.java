package org.webpda.server.war.clientcommand;

public class StopPVCommand extends AbstractPVCommand {

	@Override
	public void run() {
		getClientSession().removePV(getPvName());		
	}

}
