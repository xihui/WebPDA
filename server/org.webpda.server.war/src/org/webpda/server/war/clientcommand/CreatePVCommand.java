/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

import java.util.LinkedHashMap;
import java.util.logging.Level;

import org.webpda.server.core.LoggerUtil;
import org.webpda.server.datainterface.IPV;
import org.webpda.server.datainterface.IPVListener;
import org.webpda.server.datainterface.PVFactory;
import org.webpda.server.war.servermessage.PVEventMessage;
import org.webpda.server.war.servermessage.PVEventType;

/**A client command to create a pv.
 * @author Xihui Chen
 * 
 */
public class CreatePVCommand extends AbstractPVCommand {

	private LinkedHashMap<String, Object> parameters;	

	private int id;
	
	@Override
	public void run() {				
		try {
			IPV pv =getClientSession().getPV(id);
			if(pv==null){
				pv= PVFactory.getInstance().createPV(getPvName(), parameters);
				getClientSession().addPV(id, pv);
				pv.start();
			}			
			pv.addListener(new IPVListener(){

				@Override
				public void connectionChanged(IPV pv) {
					send(new PVEventMessage(
							getId(), PVEventType.conn, pv.isConnected(), false));
				}

				@Override
				public void exceptionOccurred(IPV pv, Exception exception) {
					send(new PVEventMessage(
							getId(), PVEventType.error, exception.getMessage(), false));
				}

				@Override
				public void valueChanged(IPV pv) {
					send(new PVEventMessage(getId(),
							pv.isBufferingValues() ? PVEventType.bufVal
									: PVEventType.val, pv
									.getDeltaJsonString(), true));				
				}				

				@Override
				public void writeFinished(IPV pv, boolean isWriteSucceeded) {
					send(new PVEventMessage(getId(), PVEventType.writeFinished, isWriteSucceeded, false));
				}

				@Override
				public void writePermissionChanged(IPV pv) {
					send(new PVEventMessage(getId(), PVEventType.writePermission, pv.isWriteAllowed(), false));
				}
				
			});
		} catch (Exception e1) {
			LoggerUtil.getLogger().log(Level.SEVERE, e1.getMessage(), e1);
		}	
	}
	
	public void setParameters(LinkedHashMap<String, Object> parameters) {
		this.parameters = parameters;
	}
	@Override
	public boolean equals(Object obj) {		
		if(!super.equals(obj))
			return false;
		if(obj instanceof CreatePVCommand){
			CreatePVCommand target = (CreatePVCommand)obj;
			if(parameters.equals(target.parameters))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31*result + id;
		result = 31*result+parameters.hashCode();
		return result;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
