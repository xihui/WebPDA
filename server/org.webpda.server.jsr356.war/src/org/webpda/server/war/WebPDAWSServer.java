/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.webpda.server.war;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.webpda.server.core.ClientSessionManager;
import org.webpda.server.core.IPeer;
import org.webpda.server.core.clientcommand.AbstractClientCommand;
import org.webpda.server.core.clientcommand.ClientSession;
import org.webpda.server.core.servermessage.ErrorMessage;
import org.webpda.server.core.util.LoggerUtil;

/**The WebSocket server of WebPDA.
 * @author Xihui Chen
 *
 */
@ServerEndpoint(value="/webpda", subprotocols={"org.webpda"}, encoders={ServerMessageEncoder.class}, decoders={ClientCommandDecoder.class})
public class WebPDAWSServer {	
	
	private static final int DEFAULT_BUFFER_SIZE = 10240;
	private static Map<Session, IPeer> sessionRegistry = Collections.synchronizedMap(
			new HashMap<Session, IPeer>());
	@OnMessage
	public void executeCommand(AbstractClientCommand command, Session session) throws IOException, EncodeException{
		command.setClientSession(ClientSessionManager.getClientSession(sessionRegistry.get(session)));
		if(command.isPermitted())
			command.run();		
		else{
			command.getClientSession().send(new ErrorMessage("Faild", "No permission to execute command: " + command));
		}
	}	
	
	
	@OnOpen
	public void onOpen(Session session){
		LoggerUtil.getLogger().log(Level.INFO, "Joined: " + session.toString());
		session.getContainer().setDefaultMaxTextMessageBufferSize(DEFAULT_BUFFER_SIZE);
		session.getContainer().setAsyncSendTimeout(60000);
		JSR356Peer peer = new JSR356Peer(session);
		sessionRegistry.put(session, peer);
		ClientSessionManager.registerPeer(peer);	
	}
	
	@OnClose
	public void onClose(Session session){		
		ClientSession clientSession = ClientSessionManager.getClientSession(
				sessionRegistry.get(session));
		if(clientSession !=null){
			LoggerUtil.getLogger().log(Level.INFO,"Closing: " + session);
			clientSession.close();
		}
		sessionRegistry.remove(session);
	}
	@OnError
	public void onError(Session peer, Throwable error){
		LoggerUtil.getLogger().log(Level.SEVERE, "Error on Websocket", error);
	}	


}
