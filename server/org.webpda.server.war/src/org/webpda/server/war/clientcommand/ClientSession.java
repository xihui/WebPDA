package org.webpda.server.war.clientcommand;

import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.webpda.server.war.IPV;

/**A session on the client side.
 * 
 * @author Xihui Chen
 *
 */
public class ClientSession {
	
	private Session session;
	
	private Map<String, IPV> pvMap;

	public ClientSession(Session session) {
		this.session = session;
		pvMap = new HashMap<String, IPV>();		
	}
	
	public void addPV(String name, IPV pv){
		pvMap.put(name, pv);
	}
	
	public void removePV(String name){
		pvMap.remove(name);
	}
	
	public Session getSession() {
		return session;
	}
	
	public void close(){
		for(IPV pv: pvMap.values()){
			pv.stop();
		}
		pvMap.clear();
	}
	
}
