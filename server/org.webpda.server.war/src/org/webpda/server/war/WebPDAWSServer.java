package org.webpda.server.war;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.webpda.server.core.LoggerUtil;
import org.webpda.server.war.clientcommand.AbstractClientCommand;
import org.webpda.server.war.clientcommand.ClientCommandDecoder;
import org.webpda.server.war.clientcommand.ClientSession;
import org.webpda.server.war.servermessage.ServerMessageEncoder;

@ServerEndpoint(value="/webpda", encoders={ServerMessageEncoder.class}, decoders={ClientCommandDecoder.class})
public class WebPDAWSServer {
	
	private static Map<Session, ClientSession> sessionRegistry = Collections.synchronizedMap(
			new HashMap<Session, ClientSession>());
	

	@OnMessage
	public void executeCommand(AbstractClientCommand command, Session session) throws IOException, EncodeException{
		LoggerUtil.getLogger().log(Level.INFO, "executeCommand: " + command);
		command.setClientSession(sessionRegistry.get(session));
		command.run();
	}	
	
	
	@OnOpen
	public void onOpen(Session peer){
		LoggerUtil.getLogger().log(Level.INFO, "Joined: " + peer.toString());
		sessionRegistry.put(peer, new ClientSession(peer));	
	}
	
	@OnClose
	public void onClose(Session peer){
		LoggerUtil.getLogger().log(Level.INFO,"Closing: " + peer);
		sessionRegistry.get(peer).close();
		sessionRegistry.remove(peer);
	}

}
