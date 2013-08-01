/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war.clientcommand;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.webpda.server.datainterface.IPV;

/**A session on the client side.
 * 
 * @author Xihui Chen
 *
 */
public class ClientSession {
	
	private Session session;
	
	private Map<CreatePVCommand, IPV> pvMap;
	private volatile boolean isClosed = false;

	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<CreatePVCommand, IPV>();		
	}
	
	public synchronized void addPV(CreatePVCommand command, IPV pv){
		if(!isClosed)
			pvMap.put(command, pv);
	}
	
	public synchronized IPV getPV(CreatePVCommand command){
		return pvMap.get(command);
	}
	
	public synchronized void removePV(CreatePVCommand command){
		IPV pv = pvMap.get(command);
		if(pv != null)
			pv.stop();
		pvMap.remove(command);
	}
	
	public synchronized String[] getAllPVs(){
		String[] result = new String[pvMap.size()];
		int i=0;
		for(CreatePVCommand command : pvMap.keySet()){
			result[i++] = command.getPvName();
		};
		return result;
	}
	
	public Session getSession() {
		return session;
	}
	
	public synchronized void close(){
		for(IPV pv: pvMap.values()){
			pv.stop();
		}
		pvMap.clear();
		isClosed = true;
	}
	
}
