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
	
	private Map<String, IPV> pvMap;
	private volatile boolean isClosed = false;

	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<String, IPV>();		
	}
	
	public synchronized void addPV(String name, IPV pv){
		if(!isClosed)
			pvMap.put(name, pv);
	}
	
	public synchronized void removePV(String name){
		IPV pv = pvMap.get(name);
		if(pv != null)
			pv.stop();
		pvMap.remove(name);
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
