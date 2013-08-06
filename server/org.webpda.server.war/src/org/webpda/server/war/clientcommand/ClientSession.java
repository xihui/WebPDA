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
	
	private Map<Integer, IPV> pvMap;
	private volatile boolean isClosed = false;

	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<Integer, IPV>();		
	}
	
	public synchronized void addPV(int id, IPV pv){
		if(!isClosed)
			pvMap.put(id, pv);
	}
	
	public synchronized IPV getPV(int id){
		return pvMap.get(id);
	}
	
	public synchronized void removePV(int id){
		IPV pv = pvMap.get(id);
		if(pv != null)
			pv.stop();
		pvMap.remove(id);
	}
	
	public synchronized String[] getAllPVs(){
		String[] result = new String[pvMap.size()];
		int i=0;
		for(IPV pv : pvMap.values()){
			result[i++] = pv.getName();
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
