package org.webpda.server.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.webpda.server.core.clientcommand.ClientSession;

public class ClientSessionManager {
	private static Map<IPeer, ClientSession> sessionRegistry = Collections.synchronizedMap(
			new HashMap<IPeer, ClientSession>());
	
	public static ClientSession getClientSession(IPeer peer){
		return sessionRegistry.get(peer);		
	}

	public static void unRegisterSession(IPeer peer) {
		sessionRegistry.remove(peer);
	}

	public static void registerPeer(IPeer peer) {
		sessionRegistry.put(peer, new ClientSession(peer));
	}
	
	public static void unRegisterPeer(IPeer peer){
		sessionRegistry.remove(peer);
	}
	
	
}
