/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

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

	private boolean readOnly = true;
	private long minUpdatePeriodInMs = 10;
	private boolean bufferAllValues=false;	

	public boolean isReadOnly() {
		return readOnly;
	}

	public void setReadOnly(boolean readOnly) {
		this.readOnly = readOnly;
	}

	public long getMinUpdatePeriodInMs() {
		return minUpdatePeriodInMs;
	}

	public void setMinUpdatePeriodInMs(long minUpdatePeriodInMs) {
		this.minUpdatePeriodInMs = minUpdatePeriodInMs;
	}

	public boolean isBufferAllValues() {
		return bufferAllValues;
	}

	public void setBufferAllValues(boolean bufferAllValues) {
		this.bufferAllValues = bufferAllValues;
	}

	@Override
	public void run() {				
		try {
			IPV pv =getClientSession().getPV(this);
			if(pv==null){
				pv= PVFactory.getInstance().createPV(getPvName(), isReadOnly(),
					getMinUpdatePeriodInMs(), isBufferAllValues());
				getClientSession().addPV(this, pv);
				pv.start();
			}			
			pv.addListener(new IPVListener(){

				@Override
				public void connectionChanged(IPV pv) {
					send(new PVEventMessage(
							getPvName(), PVEventType.conn, pv.isConnected(), false));
				}

				@Override
				public void exceptionOccurred(IPV pv, Exception exception) {
					send(new PVEventMessage(
							getPvName(), PVEventType.error, exception.getMessage(), false));
				}

				@Override
				public void valueChanged(IPV pv) {
					send(new PVEventMessage(getPvName(),
							pv.isBufferingValues() ? PVEventType.bufVal
									: PVEventType.val, pv
									.getDeltaJsonString(), true));				
				}				

				@Override
				public void writeFinished(IPV pv, boolean isWriteSucceeded) {
					send(new PVEventMessage(getPvName(), PVEventType.writeFinished, isWriteSucceeded, false));
				}

				@Override
				public void writePermissionChanged(IPV pv) {
					send(new PVEventMessage(getPvName(), PVEventType.writePermission, pv.isWriteAllowed(), false));
				}
				
			});
		} catch (Exception e1) {
			LoggerUtil.getLogger().log(Level.SEVERE, e1.getMessage(), e1);
		}	
	}
	
	@Override
	public boolean equals(Object obj) {		
		if(!super.equals(obj))
			return false;
		if(obj instanceof CreatePVCommand){
			CreatePVCommand target = (CreatePVCommand)obj;
			if(bufferAllValues == target.bufferAllValues && 
					readOnly == target.readOnly && 
					minUpdatePeriodInMs == target.minUpdatePeriodInMs)
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31*result+(readOnly?1:0);
		result = 31*result+(bufferAllValues?1:0);
		result = 31*result+(int)(minUpdatePeriodInMs^(minUpdatePeriodInMs>>>32));
		return result;
	}

}
